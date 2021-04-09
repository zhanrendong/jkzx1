def get_financial_otc_fund_detail(funds):
    reports = []
    for fund in funds['result']:
        finaitcfound = {}
        finaitcfound['clientName'] = fund['clientId']
        finaitcfound['paymentDate'] = fund['paymentDate']
        finaitcfound['paymentIn'] = 0.0
        finaitcfound['paymentOut'] = 0.0
        if fund['paymentDirection'] == 'IN':
            finaitcfound['paymentIn'] = float(fund['paymentAmount'])
        if fund['paymentDirection'] == 'OUT':
            finaitcfound['paymentOut'] = float(fund['paymentAmount'])
        finaitcfound['paymentAmount'] = float(finaitcfound['paymentIn']) - float(finaitcfound['paymentOut'])
        reports.append(finaitcfound)
    return reports

