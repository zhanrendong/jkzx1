/*bct workflow-service 升级脚本*/

-- ----------------------------
-- 清空多余数据(已删除或上版本中多余数据)
-- ----------------------------
delete from "workflow_service"."user_approve_group" where revoked = true;
delete from "workflow_service"."approve_group" where revoked = true;
delete from "workflow_service"."task_approve_group" where revoked = true;
delete from "workflow_service"."filter" where process_name != '';

delete from "workflow_service"."process" where process_name = '资金录入经办复合流程';
delete from "workflow_service"."process" where process_name = '交易录入经办复合流程';

-- ----------------------------
-- 修改process表,去除action_class与modifier_class
-- ----------------------------
alter table "workflow_service"."process" drop column action_class;
alter table "workflow_service"."process" drop column modifier_class;

-- ----------------------------
-- 重新绑定审批组与任务节点 -> 前置需要运行的1.1审批脚本
-- ----------------------------
update "workflow_service"."task_approve_group" set task_node_id = (select id from "workflow_service"."task_node" where task_name = '录入资金流水') where node_id = 
(select id from "workflow_service"."task_node" where node_name = '录入资金流水');
update "workflow_service"."task_approve_group" set task_node_id = (select id from "workflow_service"."task_node" where task_name = '修改资金流水') where node_id = 
(select id from "workflow_service"."task_node" where node_name = '修改资金流水');
update "workflow_service"."task_approve_group" set task_node_id = (select id from "workflow_service"."task_node" where task_name = '复核资金流水') where node_id = 
(select id from "workflow_service"."task_node" where node_name = '复核资金流水');
update "workflow_service"."task_approve_group" set task_node_id = (select id from "workflow_service"."task_node" where task_name = '录入交易流水') where node_id = 
(select id from "workflow_service"."task_node" where node_name = '录入交易流水');
update "workflow_service"."task_approve_group" set task_node_id = (select id from "workflow_service"."task_node" where task_name = '录入交易流水') where node_id = 
(select id from "workflow_service"."task_node" where node_name = '修改交易流水');
update "workflow_service"."task_approve_group" set task_node_id = (select id from "workflow_service"."task_node" where task_name = '录入交易流水') where node_id = 
(select id from "workflow_service"."task_node" where node_name = '复核交易流水');