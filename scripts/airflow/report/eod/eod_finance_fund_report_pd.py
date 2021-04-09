from utils import utils
from timeit import default_timer as timer


def get_financial_otc_fund_detail(parties_df, funds):
    if funds.empty:
        return funds
    parties = parties_df.set_index('legalName')
    reports = funds[['clientId', 'paymentDate', 'paymentDirection', 'paymentAmount']].fillna(0)
    reports.rename(columns={'clientId': 'clientName'}, inplace=True)
    reports['paymentIn'] = reports.apply(lambda row: row['paymentAmount'] if row['paymentDirection'] == 'IN' else 0,
                                         axis=1)
    reports['paymentOut'] = reports.apply(lambda row: row['paymentAmount'] if row['paymentDirection'] == 'OUT' else 0,
                                          axis=1)
    reports['paymentAmount'] = reports['paymentIn'] - reports['paymentOut']
    start = timer()
    reports['masterAgreementId'] = reports.apply(lambda row: parties.loc[row['client']]['masterAgreementId'], axis=1)
    end = timer()
    print('\t compute masterAgreementId takes ' + str(end - start) + ' seconds')
    return reports
