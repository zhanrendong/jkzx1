from utils import utils
import pandas as pd


def get_cash(data, row, cash_type):
    return data.get((row['clientName'], cash_type), 0)


def get_finanical_otc_client_fund(headers, ip):
    client_account_list = utils.call_request(ip, 'reference-data-service', 'refPartyList', {}, headers)['result']
    client_accounts = pd.DataFrame(client_account_list)
    account_ids = list((client_accounts['legalName'] + '0').unique())

    account_record_list = utils.call_request(
        ip, 'reference-data-service', 'clientAccountOpRecordList', {'accountIds': account_ids}, headers)['result']
    account_records = pd.DataFrame(account_record_list).fillna(0)

    cash_in = account_records[account_records['cashChange'] > 0].groupby(['legalName', 'event']).sum()['cashChange']
    cash_out = account_records[account_records['cashChange'] < 0].groupby(['legalName', 'event']).sum()['cashChange']
    reports = client_accounts[['legalName', 'masterAgreementId']]
    reports.rename(columns={'legalName': 'clientName'}, inplace=True)

    # 期权支出权利金（客户卖期权）
    reports['premiumSell'] = reports.apply(
        lambda row: abs(get_cash(cash_in, row, 'CHANGE_PREMIUM') + get_cash(cash_in, row, 'START_TRADE')), axis=1)

    # # 期权收取权利金（客户买期权）
    reports['premiumBuy'] = reports.apply(
        lambda row: abs(get_cash(cash_out, row, 'CHANGE_PREMIUM') + get_cash(cash_out, row, 'START_TRADE')), axis=1)

    reports['profitAmount'] = reports.apply(lambda row: abs(
        get_cash(cash_in, row, 'UNWIND_TRADE') + get_cash(cash_in, row, 'TERMINATE_TRADE') +
        get_cash(cash_in, row, 'SETTLE_TRADE')), axis=1)
    reports['lossAmount'] = reports.apply(lambda row: abs(
        get_cash(cash_out, row, 'UNWIND_TRADE') + get_cash(cash_out, row, 'TERMINATE_TRADE') +
        get_cash(cash_out, row, 'SETTLE_TRADE')), axis=1)

    payment_list = utils.call_request(ip, 'reference-data-service', 'cliFundEventSearch', {}, headers)['result']
    payments = pd.DataFrame(payment_list)
    if payments.empty:
        reports['paymentIn'] = 0
        reports['paymentOut'] = 0
    else:
        payments = payments[['clientId', 'paymentDirection', 'paymentAmount']].fillna(0).groupby(
            ['clientId', 'paymentDirection']).sum()['paymentAmount']
        # 入金
        reports['paymentIn'] = reports.apply(lambda row: abs(payments.get((row['clientName'], 'IN'), 0)), axis=1)
        # 出金
        reports['paymentOut'] = reports.apply(lambda row: abs(payments.get((row['clientName'], 'OUT'), 0)), axis=1)

    reports['fundTotal'] = reports['paymentIn'] - reports['paymentOut'] - reports['premiumBuy'] + reports[
        'premiumSell'] - reports['profitAmount'] + reports['lossAmount']
    return list(reports.to_dict(orient='index').values())
