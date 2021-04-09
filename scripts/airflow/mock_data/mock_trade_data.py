# -*- coding: utf-8 -*-
import uuid
import time
import random
import numpy as np
from utils import utils
from trade_import import db_utils
from datetime import datetime, timedelta
from sqlalchemy.orm import sessionmaker
from sqlalchemy import create_engine
from config.data_source_config import hive_database_url
from config.bct_config import bct_password, bct_user, bct_host

ip = bct_host
login_body = {
    'userName': bct_user,
    'password': bct_password
}

product_types = ['US', 'EU']
option_types = ['PUT', 'CALL']
directions = ['B', 'S']
annualized = ['Y', 'N']
days_in_year = [360, 365]
specified_price = [2, 3]
# 200 companies
companies = list(map(lambda num: 'FIRM_' + str(num), range(1, 201)))
# 以0.25位对称轴的，符合正态分布的,宽度为0.3的随机数组
vols = list(filter(lambda num: 0 <= num <= 1, np.random.normal(loc=0.25, scale=0.3, size=1000)))
trades_size = 10


def get_session():
    engine = create_engine(hive_database_url)
    session = sessionmaker(bind=engine)
    return session()


def gen_trade_data(instruments, trade_date=datetime.now()):
    trade = dict()
    trade['trade_id'] = str(uuid.uuid1())
    trade['product_type'] = random.choice(product_types)
    trade['option_type'] = random.choice(option_types)
    trade['days_in_year'] = random.choice(days_in_year)
    # 参与率为0.05-1的随机数
    trade['participation_rate'] = round(random.uniform(0.05, 1.001), 2)
    trade['annualized'] = random.choice(annualized)
    trade['direction'] = random.choice(directions)
    trade['book_name'] = random.choice(companies)
    trade['counter_party'] = random.choice(companies)
    trade['specified_price'] = random.choice(specified_price)
    while trade['counter_party'] == trade['book_name']:
        trade['counter_party'] = random.choice(companies)
    # 期限为随机1-270周
    trade['term'] = random.randint(1, 270) * 7
    trade['effective_date'] = str(trade_date.date())
    trade['trade_date'] = str(trade_date.date())
    trade['expire_date'] = str((trade_date + timedelta(days=trade['term'])).date())
    trade['settle_date'] = str((trade_date + timedelta(days=trade['term'])).date())

    instrument = random.choice(instruments)
    while instrument.get('instrumentId') is None or (instrument.get('yesterdayClose') is None and instrument.get(
            'last') is None and instrument.get('ask') is None and instrument.get('close') is None and instrument.get(
        'bid') is None and instrument.get('settle') is None):
        instrument = random.choice(instruments)
    trade['underlyer'] = instrument['instrumentId']
    trade['exchange'] = instrument['exchange']
    trade['init_spot'] = instrument.get('last') or instrument.get('bid') or instrument.get('ask') or instrument.get(
        'close') or instrument.get('yesterdayClose') or instrument.get('settle')
    trade['notional'] = trade['init_spot'] * random.randint(1000, 100000)
    trade['premium'] = trade['notional'] * trade['participation_rate'] * random.uniform(0.01, 0.2)
    trade['strike'] = trade['init_spot'] * random.uniform(0.8, 1.2)
    trade['vol'] = random.choice(vols)
    trade['status'] = '1'
    trade['rtvalmepay'] = 'VA'
    return trade


def save_trade_to_db(trades):
    session = get_session()
    record_id = int(time.time())
    for trade in trades:
        try:
            position = db_utils.ROTCPosition()
            # not null columns
            position.RECORDID = record_id
            position.REPORTID = trade['trade_id']
            position.REPORTDATE = trade['effective_date']
            position.COMPANYID = trade['book_name']
            position.MAINBODYNAME = trade['book_name']
            position.NOCID = trade['book_name']
            position.ANALOGUENAME = trade['counter_party']

            # required columns
            position.TRANSCONFIRTIME = trade['trade_date']
            position.OPTEXERCTMTYPE = trade['product_type']
            position.TRANSCODE = trade['trade_id']
            position.STANDASSCONT = trade['underlyer']
            position.OPTRIGHTTYPE = trade['option_type']
            position.SIDE = trade['direction']
            position.ANNUALIZED = trade['annualized']
            position.EFFECTIVEDAY = trade['days_in_year']
            position.PARTICIPATERATE = trade['participation_rate']
            position.STRIKE = trade['strike']
            position.VALUATIONSPOT = trade['init_spot']
            position.TRADENOTIONAL = trade['notional']
            position.IMPLIEDVOL = trade['vol']
            position.STATUS = trade['status']
            position.RTVALMEPAY = trade['rtvalmepay']
            position.POSITIONDATE = trade['effective_date']
            position.EFFECTDATE = trade['effective_date']
            position.EXPIREDATE = trade['expire_date']

            session.add(position)

            trade_data = db_utils.ROTCTradeData()
            # not null columns
            trade_data.RECORDID = record_id
            trade_data.REPORTID = trade['trade_id']
            trade_data.REPORTDATE = trade['effective_date']
            trade_data.COMPANYID = trade['book_name']
            trade_data.MAINBODYNAME = trade['book_name']
            trade_data.NOCID = trade['book_name']
            trade_data.ANALOGUENAME = trade['counter_party']

            # required columns
            trade_data.STANDASSTRADPLC = trade['exchange']
            trade_data.TRANSCONFIRTIME = trade['trade_date']
            trade_data.FINALSETTDAY = trade['settle_date']
            trade_data.STANDASSCONT = trade['underlyer']
            trade_data.OPTEXERCTMTYPE = trade['product_type']
            trade_data.TRANSCODE = trade['trade_id']
            trade_data.OPTRIGHTTYPE = trade['option_type']
            trade_data.DIRECTREPPARTY = trade['direction']
            trade_data.ANNUALIZED = trade['annualized']
            trade_data.PARTICIPATIONRATE = trade['participation_rate']
            trade_data.EXECUTPRICE = trade['strike']
            trade_data.ASSENTRYPRICE = trade['init_spot']
            trade_data.TRADENOTIONAL = trade['notional']
            trade_data.PREMIUMAMOUNT = trade['premium']
            trade_data.EFFECTDATE = trade['effective_date']
            trade_data.EXPIREDATE = trade['expire_date']
            trade_data.EXERCISEDATE = trade['settle_date']
            trade_data.SETTPRIMETHED = trade['specified_price']
            trade_data.STATUS = trade['status']
            trade_data.RTVALMEPAY = trade['rtvalmepay']

            session.add(trade_data)
            record_id = record_id + 1
            session.commit()
        except Exception as e:
            print(e)


def mock_trades():
    headers = utils.login(ip, login_body)
    instruments = utils.call_request(ip, 'market-data-service', 'mktQuotesListPaged', {}, headers)['result']['page']
    trades = list()
    trade_date = datetime(2019, 8, 16)
    while len(trades) < trades_size:
        trades.append(gen_trade_data(instruments, trade_date))
    save_trade_to_db(trades)
    print('done')


if __name__ == '__main__':
    print('start mock')
    mock_trades()
    print('over mock')
