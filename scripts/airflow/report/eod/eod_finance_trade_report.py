from utils import utils


def get_financial_otc_trade(position, risk, cash_flows, headers, ip):
    reports = []
    for p_id, p in position[0].items():
        otcTrade = {}
        otcTrade['optionName'] = str(p['tradeId'])
        otcTrade['bookName'] = str(p['bookName'])
        otcTrade['client'] = str(p['counterPartyName'])
        otcTrade['dealStartDate'] = str(p['tradeDate'])
        otcTrade['expiry'] = str(p['expirationDate'])
        otcTrade['side'] = str(p['direction'])
        otcTrade['baseContract'] = str(p['underlyerInstrumentId'])
        if p['underlyerInstrumentId'][-2:] == 'SZ' or p['underlyerInstrumentId'][-2:] == 'SH':
            otcTrade['assetType'] = 'STOCK'
        else:
            otcTrade['assetType'] = 'COMMODITY'
        otcTrade['nominalPrice'] = float(p['notionalAmount'])
        otcTrade['beginPremium'] = abs(float("0.0" if p['actualPremium'] is None else p['actualPremium']))
        otcTrade['status'] = p['status']
        otcTrade['positionId'] = str(p['positionId'])
        otcTrade['endPremium'] = risk.get(p['positionId'], {}).get('price', 0) if risk.get(p['positionId'], {}).get(
            'isSuccess', 0) else 0
        otcTrade['totalPremium'] = otcTrade['beginPremium'] + otcTrade['endPremium'] if \
            otcTrade['side'] == 'SELLER' else otcTrade['endPremium'] - otcTrade['beginPremium']
        otcTrade['endDate'] = cash_flows.get(p['positionId'], {}).get('timestamp', '') if \
            otcTrade['status'] != "LIVE" else ''
        otcTrade['masterAgreementId'] = utils.get_sca_id(otcTrade['client'], headers, ip)
        reports.append(otcTrade)

    return reports
