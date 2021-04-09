package tech.tongyu.bct.workflow.process;

public interface ProcessConstants {

    String PROCESS_SEQUENCE_NUM = "processSequenceNum";
    String SUBJECT = "subject";

    String PROCESS_INSTANCE = "processInstance";
    String OPERATOR = "operator";
    String TASK_HISTORY = "taskHistory";
    String ABANDON = "abandon";
    String PROCESS = "process";
    String TASK_ID = "taskId";
    String APPROVE_GROUP_LIST = "approveGroupList";

    String TASK_TYPE = "taskType";
    String TASK_TYPE$_INPUT_DATA = "inputData";
    String TASK_TYPE$_MODIFY_DATA = "modifyData";
    String TASK_TYPE$_REVIEW_DATA = "reviewData";
    String SEQUENCE = "sequence";

    String DESCRIPTION = "description";
    String APPROVE_GROUP_ID = "approveGroupId";
    String APPROVE_GROUP_NAME = "approveGroupName";

    String CONFIG_ID = "configId";
    String STATUS = "status";

    String TASK_NAME = "taskName";

    String ACTION_CLASS = "actionClass";

    String CONFIRMED = "confirmed";
    String COMMENT = "comment";

    String FUND_APPROVAL = "财务出入金";
    String TRADE_APPROVAL = "交易录入";
    String CREDIT_APPROVAL = "授信额度变更";
    String ACCOUNT_APPROVAL = "开户";

    String FUND_SUBJECT = "出入金审批";
    String TRADE_SUBJECT = "簿记审批";
    String CREDIT_SUBJECT = "授信审批";
    String ACCOUNT_SUBJECT = "开户审批";

    String TRADE = "trade";
    String CLIENT_ID = "clientId";
    String TRADE_ID = "tradeId";
    String LEGAL_NAME = "legalName";
    String COUNTER_PARTY_CODE = "counterPartyCode";

    String ACT_MUIT_VAR_NAME = "assignee";
    String ACT_MUIT_LIST_NAME = "AssigneeDTO.assigneeList";
    String ACT_LIST_NAME = "assigneeList";
    String ACT_MUIT_DTO = "AssigneeDTO";

    String UP = "up";
    String DOWN = "down";
}
