# -*- encoding: utf-8 -*-
import init_auth
import utils

subsidiary = '分公司000'
branch = '营业部000'
sales = '销售000'
ids = ['001', '002', '003']
legal_name = ['交易对手' + i for i in ids]
representative = ['法人代表' + i for i in ids]
address = ['注册地址' + i for i in ids]
contact = ['联系人' + i for i in ids]
warrantor = ['担保人' + i for i in ids]
warrantor_address = ['担保人地址' + i for i in ids]
trade_phone = ['60000' + i for i in ids]
trade_email = ['client' + i + '@email.com' for i in ids]
master_agreement_id = ['MA' + i for i in ids]
account = ['6666' + i for i in ids]
account_name = ['开户名' + i for i in ids]
bank = ['开户行' + i for i in ids]
payment_system_code = ['PS' + i for i in ids]

_CLIENT_TYPE_INSTITUTION = 'INSTITUTION'
_CLIENT_TYPE_PRODUCT = 'PRODUCT'
_CLIENT_LEVEL_A = 'LEVEL_A'


def create_subsidiary(subsidiary_name, host, token):
    return utils.call('refSubsidiaryCreate', {
        'subsidiaryName': subsidiary_name,
    }, 'reference-data-service', host, token)


def create_branch(subsidiary_id, branch_name, host, token):
    return utils.call('refBranchCreate', {
        'subsidiaryId': subsidiary_id,
        'branchName': branch_name
    }, 'reference-data-service', host, token)


def create_sales(branch_id, sales_name, host, token):
    return utils.call('refSalesCreate', {
        'branchId': branch_id,
        'salesName': sales_name
    }, 'reference-data-service', host, token)


def create_party(client_type, client_level, legal_name, legal_representative, address, contact, warrantor, warrantor_address,
                 trade_phone, trade_email, subsidiary, branch, sales, master_agreement_id, host, token):
    return utils.call('refPartySave', {
        'clientType': client_type,
        'clientLevel': client_level,
        'legalName': legal_name,
        'legalRepresentative': legal_representative,
        'address': address,
        'contact': contact,
        'warrantor': warrantor,
        'warrantorAddress': warrantor_address,
        'tradePhone': trade_phone,
        'tradeEmail': trade_email,
        'subsidiaryName': subsidiary,
        'branchName': branch,
        'salesName': sales,
        'masterAgreementId': master_agreement_id
    }, 'reference-data-service', host, token)


def create_bank_account(legal_name, account, account_name, bank, payment_system_code, host, token):
    return utils.call('refBankAccountSave', {
        'legalName': legal_name,
        'bankAccount': account,
        'bankAccountName': account_name,
        'bankName': bank,
        'paymentSystemCode': payment_system_code
    }, 'reference-data-service', host, token)


def init_bank_account_balance(account, amount, information, host, token):
    return utils.call('clientDeposit', {
        'accountId': account,
        'amount': amount,
        'information': information
    }, 'reference-data-service', host, token)


if __name__ == '__main__':
    host = init_auth.host
    token = utils.login(init_auth.script_user_name, init_auth.script_user_password, host)

    print('========== Creating sales =========')
    subInfo = create_subsidiary(subsidiary, host, token)
    branchInfo = create_branch(subInfo['subsidiaryId'], branch, host, token)
    create_sales(branchInfo['branchId'], sales, host, token)
    print('Created {sub} - {branch} - {sales}'.format(sub=subsidiary, branch=branch, sales=sales))

    print('========== Creating clients =========')
    for i in range(len(ids)):
        create_party(_CLIENT_TYPE_INSTITUTION, _CLIENT_LEVEL_A, legal_name[i], representative[i], address[i], contact[i], warrantor[i],
                     warrantor_address[i], trade_phone[i], trade_email[i], subsidiary, branch, sales,
                     master_agreement_id[i], host, token)
        print('Created client: ' + legal_name[i])
        create_bank_account(legal_name[i], account[i], account_name[i], bank[i], payment_system_code[i], host, token)
        init_bank_account_balance(legal_name[i] + '0', '10000000', 'initialize', host, token)
        print('Created bank account: ' + account_name[i])
