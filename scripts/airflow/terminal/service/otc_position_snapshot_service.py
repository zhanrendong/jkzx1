from terminal.dto import ImpliedVolReportDTO
from terminal.dao import OTCPositionSnapshotRepo
import numpy as np


class OTCPositionSnapshotService:
    @staticmethod
    def get_implied_vol_report(db_session, instrument_id, position_date):
        # TODO: 前端传来的应该是position_date, 暂时使用report_date作为变量名
        otc_position_dbo_list = OTCPositionSnapshotRepo.get_otc_position_snapshot_list(db_session, instrument_id,
                                                                                       position_date)
        otc_position_dbo_dict = {}
        # 将position按报告主体名称分组
        for position in otc_position_dbo_list:
            if otc_position_dbo_dict.get(position.mainBodyName) is None:
                otc_position_dbo_dict[position.mainBodyName] = []
            otc_position_dbo_dict[position.mainBodyName].append(position)
        # 统计implied vol结果
        implied_vol_report_list = []
        for legal_entity_name in otc_position_dbo_dict:
            position_list = otc_position_dbo_dict[legal_entity_name]
            # 按名称分组进行统计
            notional_amount = 0.0
            implied_vols = []
            for position in position_list:
                if position.tradeNotional is not None:
                    notional_amount += position.tradeNotional
                if position.impliedVol is not None:
                    implied_vols.append(position.impliedVol)
            # 构建统计结果，获取到的子公司的隐含波动率为空时，则不进行处理
            if len(implied_vols) != 0:
                dto = ImpliedVolReportDTO(reportDate=position_date, instrumentId=instrument_id,
                                          legalEntityName=legal_entity_name, notionalAmount=notional_amount,
                                          minVol=np.min(implied_vols),
                                          maxVol=np.max(implied_vols),
                                          meanVol=np.mean(implied_vols),
                                          medianVol=np.median(implied_vols),
                                          oneQuaterVol=np.percentile(implied_vols, 25),
                                          threeQuaterVol=np.percentile(implied_vols, 75))
                implied_vol_report_list.append(dto)
        return implied_vol_report_list

