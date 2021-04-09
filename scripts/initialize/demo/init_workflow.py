# -*- coding: utf-8 -*-
import init_auth
import utils

host = init_auth.host
admin_user = init_auth.admin_user
admin_password = init_auth.admin_password

def create_approve(name, description, host, token):
    """Create approve group"""
    approve_result = utils.call('wkApproveGroupCreate', {
        'approveGroupName': name,
        'description': description
    }, 'workflow-service', host, token)
    return list(filter(lambda x: x['approveGroupName'] == name, approve_result))[0]['approveGroupId']

def add_approve_user(id, name, user_list, host, token):
    """Set user to roles."""
    return utils.call('wkApproveGroupUserListModify', {
        'approveGroupId': id,
        'approveGroupName': name,
        'userList' : user_list
    }, 'workflow-service', host, token)

def get_all_user(host, token):
    return utils.call('authUserList', {
    }, 'auth-service', host, token)

def init_process_diagram(process_name, task_list, host, token):
    return utils.call('wkProcessModify', {
        'processName': process_name,
        'taskList': task_list
    }, 'workflow-service', host, token)

def close_process(process_name, host, token):
    return utils.call('wkProcessStatusModify', {
        'processName': process_name,
        'status': False
    }, 'workflow-service', host, token)


if __name__ == '__main__':
    # log in as admin
    admin_token = utils.login(admin_user, admin_password, host)

    print('========== Creating approve group ==========')
    # get approve group id
    approveGroupId_s = create_approve('发起审批', '发起审批审批组，加入此审批组用户可以进行发起审批操作', host, admin_token)
    approveGroupId_t = create_approve('交易和客户风控审批', '发起审批审批组，加入此审批组用户可以进行交易录入/开户/授信额度变更审批操作', host, admin_token)
    approveGroupId_f = create_approve('财务出入金审批', '财务出入金审批，加入此审批组用户可以进行财务出入金复核审批操作', host, admin_token)
    print('Created approve group: ' + approveGroupId_s +'/' + approveGroupId_t +'/'+ approveGroupId_f +'/' )

    print('========== add approve group user ==========')
    #  add approve group user
    start_user_names = []

    all_user = get_all_user(host, admin_token)
    for user in all_user:
        start_user_names.append(user.get('username'))

    risk_user_name = ['risk1']

    fund_user_name = ['account1']

    #add all user to start group
    add_approve_user(approveGroupId_s, '发起审批', start_user_names, host, admin_token)
    #add risk1 user to risk group
    add_approve_user(approveGroupId_t, '交易和客户风控审批', risk_user_name, host, admin_token)
    #add account2 user to fund group
    add_approve_user(approveGroupId_f, '财务出入金审批', fund_user_name, host, admin_token)
    print('========== add approve group user ==========')

    print('========== init process diagram ==========')
    #trade
    trade_input_task = {
        'taskName': '录入交易流水',
        'taskType': 'INPUT_DATA',
        'sequence': 0,
        'approveGroupList': [approveGroupId_s],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.trade.TradeInputTaskAction'
    }
    trade_review_task = {
        'taskName': '复核交易流水',
        'taskType': 'REVIEW_DATA',
        'sequence': 1,
        'approveGroupList': [approveGroupId_t],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.trade.TradeReviewTaskAction'
    }
    trade_update_task = {
        'taskName': '修改交易流水',
        'taskType': 'MODIFY_DATA',
        'sequence': 2,
        'approveGroupList': [approveGroupId_s],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.trade.TradeInputTaskAction'
    }
    trade_task_list = [trade_input_task, trade_review_task, trade_update_task]
    init_process_diagram('交易录入', trade_task_list, host, admin_token)
    #fund
    fund_input_task = {
        'taskName': '录入资金流水',
        'taskType': 'INPUT_DATA',
        'sequence': 0,
        'approveGroupList': [approveGroupId_s],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.cap.FundInputTaskAction'
    }
    fund_review_task = {
        'taskName': '复核资金流水',
        'taskType': 'REVIEW_DATA',
        'sequence': 1,
        'approveGroupList': [approveGroupId_f],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.cap.FundReviewTaskAction'
    }
    fund_update_task = {
        'taskName': '修改资金流水',
        'taskType': 'MODIFY_DATA',
        'sequence': 2,
        'approveGroupList': [approveGroupId_s],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.cap.FundInputTaskAction'
    }
    fund_task_list = [fund_input_task, fund_review_task, fund_update_task]
    init_process_diagram('财务出入金', fund_task_list, host, admin_token)
    #credit
    credit_input_task = {
        'taskName': '录入授信额度',
        'taskType': 'INPUT_DATA',
        'sequence': 0,
        'approveGroupList': [approveGroupId_s],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.credit.CreditInputTaskAction'
    }
    credit_review_task = {
        'taskName': '复核授信额度',
        'taskType': 'REVIEW_DATA',
        'sequence': 1,
        'approveGroupList': [approveGroupId_t],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.credit.CreditReviewTaskAction'
    }
    credit_update_task = {
        'taskName': '修改授信额度',
        'taskType': 'MODIFY_DATA',
        'sequence': 2,
        'approveGroupList': [approveGroupId_s],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.credit.CreditInputTaskAction'
    }
    credit_task_list = [credit_input_task, credit_review_task, credit_update_task]
    init_process_diagram('授信额度变更', credit_task_list, host, admin_token)
    #party
    account_input_task = {
        'taskName': '录入开户信息',
        'taskType': 'INPUT_DATA',
        'sequence': 0,
        'approveGroupList': [approveGroupId_s],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.account.AccountInputTaskAction'
    }
    account_review_task = {
        'taskName': '复核开户信息',
        'taskType': 'REVIEW_DATA',
        'sequence': 1,
        'approveGroupList': [approveGroupId_t],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.account.AccountReviewTaskAction'
    }
    account_update_task = {
        'taskName': '修改开户信息',
        'taskType': 'MODIFY_DATA',
        'sequence': 2,
        'approveGroupList': [approveGroupId_s],
        'actionClass': 'tech.tongyu.bct.workflow.process.func.action.account.AccountInputTaskAction'
    }
    account_task_list = [account_input_task, account_review_task, account_update_task]
    init_process_diagram('开户', account_task_list, host, admin_token)

    print('========== init process diagram ==========')

    print('========== close process ==========')
    #trade
    close_process('交易录入', host, admin_token)
    #fund
    close_process('财务出入金', host, admin_token)
    #credit
    close_process('授信额度变更', host, admin_token)
    #party
    close_process('开户', host, admin_token)
    print('========== close process ==========')