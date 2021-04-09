from utils import utils


# 获取所有交易对手账户
def get_client_count_list(ip, headers):
    client_account_list = utils.call_request(ip, 'reference-data-service', 'refPartyList', {}, headers)['result']
    client_id = []
    for client in client_account_list:
        client_id.append(client['legalName']+'0')
    return client_id, client_account_list


def get_finanical_otc_client_fund(headers, ip):
    reports = []
    client_name_list = get_client_count_list(ip, headers)
    client_account_list = utils.call_request(ip, 'reference-data-service', 'clientAccountOpRecordList', {'accountIds': client_name_list[0]}, headers)['result']
    payment_list = utils.call_request(ip, 'reference-data-service', 'cliFundEventSearch', {}, headers)['result']
    for client in client_name_list[1]:
        report = {}
        report['clientName'] = client['legalName']     # 客户名称
        report['masterAgreementId'] = client['masterAgreementId']         # SAC主协议编码
        report['paymentIn'] = 0.0                # 入金
        report['paymentOut'] = 0.0               # 出金
        report['premiumBuy'] = 0.0               # 期权收取权利金（客户买期权）
        report['premiumSell'] = 0.0              # 期权支出权利金（客户卖期权）
        report['profitAmount'] = 0.0             # 期权了结盈利
        report['lossAmount'] = 0.0               # 期权了结亏损
        for client_account in client_account_list:
            if client_account['legalName'] == report['clientName']:
                if client_account['event'] == 'CHANGE_PREMIUM' or client_account['event'] == 'START_TRADE':
                    if client_account['cashChange'] > 0:
                        report['premiumSell'] += client_account['cashChange']          # 期权支出权利金（客户卖期权）
                    elif client_account['cashChange'] < 0:
                        report['premiumBuy'] += client_account['cashChange']            # 期权收取权利金（客户买期权）
                elif client_account['event'] == 'UNWIND_TRADE' or client_account['event'] == 'SETTLE_TRADE' or client_account['event'] == 'TERMINATE_TRADE':
                    if client_account['cashChange'] > 0:
                        report['profitAmount'] += client_account['cashChange']         # 期权了结盈利
                    elif client_account['cashChange'] < 0:
                        report['lossAmount'] += client_account['cashChange']         # 期权了结亏损

        for payment in payment_list:
            if payment['clientId'] == report['clientName']:
                if payment['paymentDirection'] == 'IN':
                    report['paymentIn'] += payment['paymentAmount']
                elif payment['paymentDirection'] == 'OUT':
                    report['paymentOut'] += payment['paymentAmount']

        report['paymentIn'] = abs(report['paymentIn'])
        report['paymentOut'] = abs(report['paymentOut'])
        report['premiumBuy'] = abs(report['premiumBuy'])
        report['premiumSell'] = abs(report['premiumSell'])
        report['profitAmount'] = abs(report['profitAmount'])
        report['lossAmount'] = abs(report['lossAmount'])
        report['fundTotal'] = report['paymentIn'] - report['paymentOut'] - report['premiumBuy'] + report['premiumSell'] - report['profitAmount'] + report['lossAmount']
        reports.append(report)
    return reports

