import multiprocessing
import time
from datetime import datetime, timedelta, date

import numpy as np
import pandas as pd
import sqlalchemy
from scipy.stats import norm
from sqlalchemy.exc import NoSuchColumnError, DatabaseError

from dags.conf import DBConfig
from dags.conf.settings import BCTServerConfig
from dags.dbo import ROTCPosition
from dags.dbo.db_model import create_db_session as Session
from dags.utils import server_utils
from terminal.dbo import BaseModel
from terminal.dto import VolSurfaceStrikeType, VolSurfaceInstanceType, \
    VolSurfaceDTO, VolSurfaceModelInfoDTO, VolGridItemDTO, VolItemDTO, VolSurfaceUnderlyerDTO, VolSurfaceSchema
from terminal.service import ImpliedVolService, HistoricalVolService, InstrumentService
from terminal.service import VolSurfaceService as TerminalVolSurfaceService
from terminal.utils import Logging

logging = Logging.getLogger(__name__)

STATUS_LIVE = 'LIVE'
SUPPORTED_PRODUCT_TYPES = ['VANILLA_EUROPEAN', 'VANILLA_AMERICAN']
UNIT_PERCENT = 'PERCENT'
INSTRUMENT_TYPE_FUTURES = 'FUTURES'
VOL_SURFACE_NAME = 'TRADER_VOL'
VOL_SURFACE_INSTANCE = 'CLOSE'
UNDERLYER_PRICE_FIELD = 'close'
VOL_SURFACE_SOURCE = 'OFFICIAL'
RISK_FREE_RATE_NAME = 'TRADER_RISK_FREE_CURVE'
RISK_FREE_RATE_INSTANCE = 'close'
MODEL_TYPE_RISK_FREE_CURVE = 'RISK_FREE_CURVE'
CALENDAR_NAMES = ['DEFAULT_CALENDAR']
VOL_CALENDAR_NAME = 'DEFAULT_VOL_CALENDAR'
DATE_FMT = '%Y-%m-%d'

# fair vol marking parameters
tenors_trading = np.array([1, 3, 5, 10, 22, 44, 66, 132])
tenors_calendar = np.array([1, 3, 7, 14, 30, 60, 90, 180])
realized_diff_weights = np.array([[0.9, 0.1, 0, 0, 0, 0, 0, 0],
                                  [0.5, 0.5, 0, 0, 0, 0, 0, 0],
                                  [0.4, 0.3, 0.3, 0, 0, 0, 0, 0],
                                  [0.4, 0.3, 0.2, 0.1, 0, 0, 0, 0],
                                  [0.3, 0.2, 0.2, 0.2, 0.1, 0, 0, 0],
                                  [0.3, 0.2, 0.2, 0.1, 0.1, 0.1, 0, 0],
                                  [0.3, 0.2, 0.1, 0.1, 0.1, 0.1, 0.1, 0],
                                  [0.2, 0.2, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]])
tenor_bin_bounds = np.array([0, 2, 4, 10, 21, 45, 75, 150, np.inf])
market_vol_shift_scale = 0.1
local_vol_smooth_weights = np.array([0.1, 0.8, 0.1])
days_in_year = 365


class VolSurfaceService(object):

    @staticmethod
    def get_market_data(val_date, instrument_ids, session, bct_host, bct_port, bct_headers):
        """Return market data(price, instrumentType, isFutures).
        Use primary instrument price if not found for certain instrument."""
        try:
            val_date_str = val_date.strftime(DATE_FMT)
            market_data = server_utils.call_request(
                bct_host, bct_port, 'market-data-service', 'mktQuotesListPaged',
                {'instrumentIds': instrument_ids, 'valuationDate': val_date_str},
                bct_headers)['result']['page']
        except:
            return pd.DataFrame(columns=['instrumentId', UNDERLYER_PRICE_FIELD, 'instrumentType', 'isFutures']
                                ).set_index('instrumentId')
        market_data = pd.DataFrame(market_data, columns=['instrumentId', UNDERLYER_PRICE_FIELD, 'instrumentType'])
        market_data = market_data.dropna()
        market_data.set_index('instrumentId', inplace=True)
        market_data = market_data.reindex(instrument_ids)
        for i, row in market_data.iterrows():
            if np.isnan(row[UNDERLYER_PRICE_FIELD]):
                try:
                    primary_id = InstrumentService.get_primary_instrument_id(session, i, val_date, True)
                    row[UNDERLYER_PRICE_FIELD] = market_data.loc[primary_id, UNDERLYER_PRICE_FIELD]
                    row['instrumentType'] = market_data.loc[primary_id, 'instrumentType']
                except NoSuchColumnError as e:
                    raise e
                except Exception as e:
                    logging.error(e)
        market_data['isFutures'] = market_data['instrumentType'] == INSTRUMENT_TYPE_FUTURES
        return market_data

    @staticmethod
    def get_positions(val_date, instrument_ids, session, bct_host, bct_port, bct_headers):
        """Return positions with implied vol"""
        # position info from BCT
        try:
            trades = server_utils.call_request(bct_host, bct_port, 'trade-service',
                                               'trdTradeSearchIndex', {}, bct_headers)['result']
        except:
            return pd.DataFrame(columns=['transcode', 'underlyer', 'tenor', 'implied_vol']).set_index('transcode')
        positions = []
        for t in trades:
            if t['tradeStatus'] != STATUS_LIVE:
                continue
            for p in t['positions']:
                if p['productType'] not in SUPPORTED_PRODUCT_TYPES:
                    continue
                a = p['asset']
                if a['underlyerInstrumentId'] not in instrument_ids:
                    continue
                positions.append({
                    'transcode': t['tradeId'],
                    'position_id': p['positionId'],
                    'underlyer': a['underlyerInstrumentId'],
                    'strike': a['strike'] * a['initialSpot'] if a['strikeType'] == UNIT_PERCENT else a['strike'],
                    'expiry': a['expirationDate'],
                    'quantity': a['annualizedActualNotionalAmountByLot'] * a['underlyerMultiplier']
                })
        if len(positions) < 1:
            return pd.DataFrame(columns=['transcode', 'underlyer', 'tenor', 'implied_vol']).set_index('transcode')
        positions = pd.DataFrame(positions)
        positions.set_index('transcode', inplace=True)
        positions['tenor'] = positions['expiry'].apply(
            lambda d: (datetime.strptime(d, DATE_FMT).date() - val_date).days)
        # implied vol from terminal
        try:
            implied_vols = session.query(
                ROTCPosition.TRANSCODE, ROTCPosition.IMPLIEDVOL, ROTCPosition.REPORTDATE).filter(
                ROTCPosition.STANDASSCONT.in_(instrument_ids),
                ROTCPosition.POSITIONDATE == val_date.strftime(DATE_FMT)).all()
        except NoSuchColumnError as e:
            raise e
        except Exception as e:
            logging.error(e)
            implied_vols = None
        if implied_vols is None or len(implied_vols) < 1:
            return pd.DataFrame(columns=['transcode', 'underlyer', 'tenor', 'implied_vol']).set_index('transcode')
        implied_vols = pd.DataFrame(implied_vols, columns=['transcode', 'implied_vol', 'report_date'])
        implied_vols = implied_vols.sort_values(by=['report_date'], ascending=False)
        implied_vols = implied_vols.drop_duplicates(subset='transcode', keep='first')
        implied_vols.set_index('transcode', inplace=True)
        positions['implied_vol'] = implied_vols['implied_vol']
        positions = positions.dropna()
        positions['implied_vol'] = positions['implied_vol'].apply(float)
        return positions

    @staticmethod
    def vega(tenor, strike, spot, vol, r, is_futures, quantity):
        """Return position vega. Dividend is assumed zero."""
        t = tenor / days_in_year
        if is_futures:
            forward = spot
        else:
            forward = np.exp(r * t) * spot
        var = vol * np.sqrt(t)
        dp = np.log(forward / strike) / var + 0.5 * var
        return norm.pdf(dp) * spot * np.sqrt(t) * quantity

    @staticmethod
    def get_interest_rate(val_date, expiry, bct_host, bct_port, bct_headers):
        try:
            res = server_utils.call_request(bct_host, bct_port, 'model-service', 'mdlLoad', {
                'modelType': MODEL_TYPE_RISK_FREE_CURVE,
                'modelName': RISK_FREE_RATE_NAME,
                'instance': RISK_FREE_RATE_INSTANCE,
                'valuationDate': val_date.strftime(DATE_FMT)
            }, bct_headers)
            if res == 'error':
                return 0
            curve = res['result']
            res = server_utils.call_request(bct_host, bct_port, 'quant-service', 'qlDf', {
                'curve': curve,
                't': expiry.strftime(DATE_FMT)
            }, bct_headers)
            if res == 'error':
                return 0
            df = res['result']
        except Exception as e:
            return 0
        return -np.log(df) / ((expiry - val_date) / timedelta(days=365))

    @staticmethod
    def calc_weighted_vol_by_instrument(positions, market_data, r):
        """Return vega weighted vols and weights given positions with implied vol."""
        positions['vega'] = np.zeros(len(positions))
        grouped_positions = positions.groupby('underlyer')
        for u, i in grouped_positions.groups.items():
            positions.loc[i, 'vega'] = VolSurfaceService.vega(
                positions.loc[i, 'tenor'].to_numpy(), positions.loc[i, 'strike'].to_numpy(),
                market_data.loc[u, UNDERLYER_PRICE_FIELD], positions.loc[i, 'implied_vol'].to_numpy(),
                r, market_data.loc[u, 'isFutures'], positions.loc[i, 'quantity'].to_numpy())
        positions.dropna()
        positions['vol_x_vega'] = positions['implied_vol'] * positions['vega']
        positions['tenor_bin'] = pd.cut(positions['tenor'], tenor_bin_bounds, labels=tenors_calendar)
        grouped_positions = positions[['underlyer', 'tenor_bin', 'vol_x_vega', 'vega']].groupby(
            ['underlyer', 'tenor_bin'])
        return grouped_positions.sum()

    @staticmethod
    def mark_one_fair_vol(val_date, instrument_ids, weighted_market_vols, market_data,
                          session, bct_host, bct_port, bct_headers):
        """Mark and save fair vol for one instrument or a set of instruments sharing vol surface.
        Return marked vol as numpy array."""
        vols = pd.DataFrame(columns=['last', 'realized'], index=tenors_trading)

        realized_vol = HistoricalVolService.calc_instrument_realized_vol(
            session, instrument_ids[0], val_date, (tenors_trading - 1).tolist(), True)[0]
        if len(realized_vol) == 0:
            raise Exception('标的: %s的realized_vol为空' % instrument_ids[0])
        if realized_vol is not None:
            for r in realized_vol:
                vols.loc[r.window + 1, 'realized'] = r.vol

        last_fair_vol, diagnostic = ImpliedVolService.get_instrument_vol_surface(
            session, instrument_ids[0], val_date - timedelta(days=1),
            VolSurfaceStrikeType.PERCENT.name, VolSurfaceInstanceType.CLOSE.name, True)
        if last_fair_vol is not None:
            for i in last_fair_vol.modelInfo.instruments:
                t = int(i.tenor[:-1])
                vols.loc[t, 'last'] = i.vols[0].quote
        else:
            vols['last'] = vols['realized']

        vols[['last', 'realized']] = vols[['last', 'realized']].fillna(method='ffill')
        vols[['last', 'realized']] = vols[['last', 'realized']].fillna(method='bfill')

        vols.index = tenors_calendar
        realized_last_diff = vols['realized'].to_numpy() - vols['last'].to_numpy().reshape((tenors_calendar.size, 1))
        vols['marked1'] = vols['last'] + np.einsum('ij,ij->i', realized_last_diff, realized_diff_weights)

        position_underlyers = []
        for i in instrument_ids:
            if i in weighted_market_vols.index:
                position_underlyers.append(i)
        if len(weighted_market_vols) == 0 or len(position_underlyers) == 0:
            vols['marked2'] = vols['marked1']
        else:
            weighted_market_vol = weighted_market_vols.loc[position_underlyers].groupby('tenor_bin').sum()
            vols['market'] = weighted_market_vol['vol_x_vega'] / weighted_market_vol['vega']
            vols.loc[vols['market'].isna().to_numpy(), 'market'] = vols['marked1']
            market_marked1_diff = vols['market'].to_numpy() - vols['marked1'].to_numpy()
            vols['marked2'] = vols['marked1']\
                + (1 - np.exp(-np.abs(market_marked1_diff / market_vol_shift_scale))) * market_marked1_diff

        variance = vols['marked2'].to_numpy() ** 2 * tenors_calendar / days_in_year
        for i in range(1, len(variance)):
            if variance[i] < variance[i-1]:
                variance[i] = variance[i-1]
        expiry_intervals = np.ediff1d(np.insert(tenors_calendar, 0, 0)) / days_in_year
        squared_local_vol = np.ediff1d(np.insert(variance, 0, 0)) / expiry_intervals
        smoothed_squared_local_vol = np.concatenate(
            ([squared_local_vol[0]],
             np.convolve(squared_local_vol, local_vol_smooth_weights, mode='valid'),
             [squared_local_vol[-1]]))
        smoothed_variances = np.cumsum(smoothed_squared_local_vol * expiry_intervals)
        vols['smoothed'] = np.sqrt(smoothed_variances / tenors_calendar * days_in_year)

        vols.index = tenors_trading
        vs_dto = VolSurfaceDTO(instrument_ids[0], val_date, VolSurfaceStrikeType.PERCENT.name,
                               instance=VOL_SURFACE_INSTANCE,
                               modelInfo=VolSurfaceModelInfoDTO(
                                   days_in_year,
                                   [VolGridItemDTO(
                                       str(t) + 'D',
                                       vols=[VolItemDTO(
                                           percent=1 + ds, quote=vols.loc[t, 'smoothed'],
                                           label=str(int((1 + ds) * 100)) + '% SPOT'
                                       ) for ds in [-0.1, 0, 0.1]]
                                   ) for t in tenors_trading],
                                   VOL_SURFACE_NAME, True,
                                   VolSurfaceUnderlyerDTO(instrument_ids[0], VOL_SURFACE_INSTANCE,
                                                          UNDERLYER_PRICE_FIELD, None)
                               ), source=VOL_SURFACE_SOURCE)
        vol_surface_schema = VolSurfaceSchema(exclude=[])
        for i in instrument_ids:
            vs_dto.instrumentId = i
            vs_dto.modelInfo.underlyer.instrumentId = i
            vs_dto.modelInfo.underlyer.quote = market_data.loc[i, UNDERLYER_PRICE_FIELD]
            if np.isnan(vs_dto.modelInfo.underlyer.quote):
                vs_dto.modelInfo.underlyer.quote = 1
            TerminalVolSurfaceService.save_vol_surface(session, vs_dto)
            model_info = vol_surface_schema.dump(vs_dto).data['modelInfo']
            model_info['instance'] = VOL_SURFACE_INSTANCE
            model_info['valuationDate'] = val_date.strftime(DATE_FMT)
            model_info['useCalendarForTenor'] = True
            model_info['calendars'] = CALENDAR_NAMES
            model_info['useVolCalendar'] = True
            model_info['volCalendar'] = VOL_CALENDAR_NAME
            server_utils.call_request(bct_host, bct_port, 'model-service', 'mdlVolSurfaceInterpolatedStrikeCreate',
                                      model_info, bct_headers)
        return vols['smoothed'].to_numpy()

    @staticmethod
    def mark_fair_vols(val_date, instrument_id_list):
        """Mark and save for the given instruments. Return (success_num, failed_num)
        instrument_ids: List of list. Each inner list contains instruments sharing vol surface, usually belonging to
                        the same underlyer product."""
        bct_host = BCTServerConfig.host
        bct_port = BCTServerConfig.port
        bct_headers = server_utils.login_bct()
        engine = sqlalchemy.create_engine(
            DBConfig.db_connection,
            connect_args={'options': '-csearch_path={}'.format(DBConfig.default_schema)},
            echo=DBConfig.show_sql)
        BaseModel.metadata.bind = engine
        BaseModel.metadata.create_all()
        session = sqlalchemy.orm.scoped_session(sqlalchemy.orm.sessionmaker(bind=engine))

        market_data = VolSurfaceService.get_market_data(val_date, np.concatenate(instrument_id_list).tolist(),
                                                        session, bct_host, bct_port, bct_headers)
        logging.info('Fetched market data of {n} instruments.'.format(n=len(market_data)))
        positions = VolSurfaceService.get_positions(val_date, np.concatenate(instrument_id_list).tolist(),
                                                    session, bct_host, bct_port, bct_headers)
        logging.info('Fetched {n} OTC positions.'.format(n=len(positions)))
        r = VolSurfaceService.get_interest_rate(val_date, val_date + timedelta(days=1), bct_host, bct_port, bct_headers)
        logging.info('Using risk free rate: ' + str(r))
        weighted_vols = VolSurfaceService.calc_weighted_vol_by_instrument(positions, market_data, r)
        logging.info('Calculated {n} averaged vols.'.format(n=len(weighted_vols)))

        success, failed = (0, 0)
        for instrument_ids in instrument_id_list:
            try:
                VolSurfaceService.mark_one_fair_vol(val_date, instrument_ids, weighted_vols, market_data,
                                                    session, bct_host, bct_port, bct_headers)
                success += 1
            except NoSuchColumnError as e:
                raise e
            except DatabaseError as e:
                raise e
            except Exception as e:
                logging.error(e)
                failed += 1
        return success, failed

    @staticmethod
    def update_all_vol_surface(start_date, end_date, process_num=4):
        session = Session()
        try:
            while start_date <= end_date:
                val_date = start_date
                logging.info('开始计算%s的vol surface' % val_date)
                instrument_id_list = InstrumentService.get_grouped_instrument_id_list(session,
                                                                                      val_date.strftime(DATE_FMT))
                id_num = len(instrument_id_list)
                id_lists = []
                for i in range(process_num):
                    id_list = instrument_id_list[int(i / process_num * id_num):int((i + 1) / process_num * id_num)]
                    if len(id_list) > 0:
                        id_lists.append(id_list)

                with multiprocessing.Pool(process_num) as pool:
                    results = []
                    for i in id_lists:
                        results.append(pool.apply_async(VolSurfaceService.mark_fair_vols, (val_date, i)))
                    succ_fail = [r.get() for r in results]
                    success = sum([sf[0] for sf in succ_fail])
                    failed = sum([sf[1] for sf in succ_fail])

                logging.info('成功了: %d, 失败了: %d' % (success, failed))
                start_date = start_date + timedelta(days=1)
        except NoSuchColumnError as e:
            raise e
        except DatabaseError as e:
            raise e
        except Exception as e:
            logging.error(e)
        finally:
            session.close()


# if __name__ == '__main__':
#     # mark fair vol
#     start_date, end_date = date(2019, 12, 4), date(2019, 12, 4)
#     t = time.time()
#     VolSurfaceService.update_all_vol_surface(start_date, end_date, 4)
#     print(time.time() - t)
