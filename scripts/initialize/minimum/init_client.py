# -*- encoding: utf-8 -*-
import utils
from init_params import *

_CLIENT_TYPE_INSTITUTION = 'INSTITUTION'
_CLIENT_TYPE_PRODUCT = 'PRODUCT'


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


def create_party(client_type, legal_name, legal_representative, address, contact, warrantor, warrantor_address,
                 trade_phone, trade_email, subsidiary, branch, sales, master_agreement_id, host, token):
    return utils.call('refPartySave', {
        'clientType': client_type,
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


def init_sales():
    token = utils.login(admin_user, admin_password, host)
    print('========== Creating sales =========')
    subInfo = create_subsidiary(subsidiary, host, token)
    branchInfo = create_branch(subInfo['subsidiaryId'], branch, host, token)
    create_sales(branchInfo['branchId'], sales, host, token)
    print('Created {sub} - {branch} - {sales}'.format(sub=subsidiary, branch=branch, sales=sales))


if __name__ == '__main__':
    token = utils.login(admin_user, admin_password, host)
    init_sales()
    print('========== Creating client =========')
    create_party(_CLIENT_TYPE_INSTITUTION, legal_name, representative, address, contact, warrantor, warrantor_address,
                 trade_phone, trade_email, subsidiary, branch, sales, master_agreement_id, host, token)
    print('Created client: ' + legal_name)
    create_bank_account(legal_name, account, account_name, bank, payment_system_code, host, token)
    print('Created bank account: ' + account_name)
    init_bank_account_balance(legal_name + "0", "1000000", "initialize", host, token)
    print('Initialize bank account: ' + "1000000")
