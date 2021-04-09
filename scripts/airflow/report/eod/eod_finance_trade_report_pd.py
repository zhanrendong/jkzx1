from timeit import default_timer as timer


def get_financial_otc_trade(parties_df, position, risk, cash_flows, headers, ip):
    parties = parties_df.set_index('legalName')
    report = position[
        ['tradeId', 'bookName', 'counterPartyName', 'tradeDate', 'asset.expirationDate', 'asset.direction',
         'asset.underlyerInstrumentId', 'asset.notionalAmount', 'actualPremium', 'positionId', 'tradeStatus']]
    report.columns = ['optionName', 'bookName', 'client', 'dealStartDate', 'expiry', 'side', 'baseContract',
                      'nominalPrice', 'beginPremium', 'positionId', 'status']
    start = timer()
    report = report.merge(risk[['price']].reset_index(), on='positionId', how='left')
    report['baseContract'].fillna('', inplace=True)
    report.fillna(0, inplace=True)
    report['beginPremium'] = abs(report['beginPremium'])
    report.rename(columns={'price': 'endPremium'}, inplace=True)
    end = timer()
    print('\tmerge risk and position takes ' + str(end - start) + ' seconds')
    start = timer()
    report['assetType'] = report.apply(
        lambda row: 'STOCK' if row.get('baseContract', '').endswith('.SZ') or row.get('baseContract', '').endswith(
            '.SH') or row.get('baseContract', '').endswith('.CFE') else 'COMMODITY', axis=1)
    cash_flows.set_index('positionId', inplace=True)
    end = timer()
    print('\t compute assetType takes ' + str(end - start) + ' seconds')
    start = timer()
    report['endDate'] = report.apply(
        lambda row: cash_flows.loc[row['positionId'], 'timestamp'] if row['positionId'] in cash_flows.index and
                                                                      row['status'] != 'LIVE' else '', axis=1)
    end = timer()
    print('\t compute endDate takes ' + str(end - start) + ' seconds')
    start = timer()
    report['masterAgreementId'] = report.apply(lambda row: parties.loc[row['client']]['masterAgreementId'], axis=1)
    end = timer()
    print('\t compute masterAgreementId takes ' + str(end - start) + ' seconds')
    start = timer()
    report['totalPremium'] = report.apply(
        lambda row: row['beginPremium'] + row['endPremium'] if row['side'] == 'SELLER' else row['endPremium'] - row[
            'beginPremium'], axis=1).fillna(0)
    end = timer()
    print('\t compute totalPremium takes ' + str(end - start) + ' seconds')
    return report.fillna(0)
