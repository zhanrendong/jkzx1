/*
首先清空已撤销的假数据 -> 若未清空数据,本版本必须执行
*/
----------------------------
delete from "workflow_service"."approve_group" where revoked = true;
delete from "workflow_service"."task_approve_group" where revoked = true;
delete from "workflow_service"."user_approve_group" where revoked = true;
----------------------------

/*
由于未重新初始化process task_node表无数据,插入数据 -> 若未清空数据,本版本必须执行
*/
----------------------------
INSERT INTO "workflow_service"."task_node" (id, create_time, revoke_time, revoked, update_time, node_name, node_number, node_type, process_name, sequence) VALUES ('7b792473-e103-4724-8746-03d170d32029', '2019-05-09 02:09:24.39', NULL, 'f', '2019-05-09 02:09:24.39', '录入资金流水', '_0', 'INPUT_DATA', '资金录入经办复合流程', -1);
INSERT INTO "workflow_service"."task_node" (id, create_time, revoke_time, revoked, update_time, node_name, node_number, node_type, process_name, sequence) VALUES ('8c163d56-32c2-46f7-8ee4-6d16ed44234a', '2019-05-09 02:09:24.395', NULL, 'f', '2019-05-09 02:09:24.395', '修改资金流水', '_1', 'MODIFY_DATA', '资金录入经办复合流程', -1);
INSERT INTO "workflow_service"."task_node" (id, create_time, revoke_time, revoked, update_time, node_name, node_number, node_type, process_name, sequence) VALUES ('d9719afa-f5a5-4161-a479-b913ab6b4ee3', '2019-05-09 02:09:24.399', NULL, 'f', '2019-05-09 02:09:24.399', '复核资金流水', '_2', 'REVIEW_DATA', '资金录入经办复合流程', 0);
INSERT INTO "workflow_service"."task_node" (id, create_time, revoke_time, revoked, update_time, node_name, node_number, node_type, process_name, sequence) VALUES ('f07f1e17-0434-435a-afee-3c8088612bb9', '2019-05-09 02:09:24.482', NULL, 'f', '2019-05-09 02:09:24.482', '录入交易流水', '_0', 'INPUT_DATA', '交易录入经办复合流程', -1);
INSERT INTO "workflow_service"."task_node" (id, create_time, revoke_time, revoked, update_time, node_name, node_number, node_type, process_name, sequence) VALUES ('799541f2-9fe0-4762-b304-94d3bcd28065', '2019-05-09 02:09:24.483', NULL, 'f', '2019-05-09 02:09:24.483', '修改交易流水', '_1', 'MODIFY_DATA', '交易录入经办复合流程', -1);
INSERT INTO "workflow_service"."task_node" (id, create_time, revoke_time, revoked, update_time, node_name, node_number, node_type, process_name, sequence) VALUES ('1dabb0aa-a14b-4252-8a9a-cf7d23361a4a', '2019-05-09 02:09:24.484', NULL, 'f', '2019-05-09 02:09:24.484', '复核交易流水', '_2', 'REVIEW_DATA', '交易录入经办复合流程', 0);

ALTER TABLE "workflow_service"."task_node" ADD CONSTRAINT "task_node_pkey" PRIMARY KEY ("id");
----------------------------


/*
重新绑定流程节点与审批组 -> 若未清空数据,本版本必须执行
*/
----------------------------
UPDATE "workflow_service"."task_approve_group" SET node_id = '7b792473-e103-4724-8746-03d170d32029' where task_name = '录入资金流水';
UPDATE "workflow_service"."task_approve_group" SET node_id = '8c163d56-32c2-46f7-8ee4-6d16ed44234a' where task_name = '修改资金流水';
UPDATE "workflow_service"."task_approve_group" SET node_id = 'd9719afa-f5a5-4161-a479-b913ab6b4ee3' where task_name = '复核资金流水';
UPDATE "workflow_service"."task_approve_group" SET node_id = 'f07f1e17-0434-435a-afee-3c8088612bb9' where task_name = '录入交易流水';
UPDATE "workflow_service"."task_approve_group" SET node_id = '799541f2-9fe0-4762-b304-94d3bcd28065' where task_name = '修改交易流水';
UPDATE "workflow_service"."task_approve_group" SET node_id = '1dabb0aa-a14b-4252-8a9a-cf7d23361a4a' where task_name = '复核交易流水';
----------------------------

/*
若global_config表有数据则不需要进行以下代码 -> 审批流程配置,全局配置为空时,运行以下代码
*/
----------------------------

INSERT INTO "workflow_service"."global_config" (id, create_time, revoke_time, revoked, update_time, global_id, global_name, process_name, status, task_name) VALUES ('b8a631b7-ad62-4ab4-a646-4b402fe96cea', '2019-04-27 07:07:29.807', NULL, 'f', '2019-04-27 07:07:29.807', 'not_allow_start_by_self_trade', '不允许审批自己发起的审批单', '交易录入经办复合流程', 't', '复核交易流水');
INSERT INTO "workflow_service"."global_config" (id, create_time, revoke_time, revoked, update_time, global_id, global_name, process_name, status, task_name) VALUES ('d4845d47-5191-4b8e-b203-f99c8db8fc48', '2019-04-27 07:07:29.782', NULL, 'f', '2019-05-05 13:20:45.389', 'not_allow_start_by_self_fund', '不允许审批自己发起的审批单', '资金录入经办复合流程', 'f', '复核资金流水');

----------------------------

/*
若request表有数据则不需要进行以下代码 -> 若出现无法完成复核操作并显示未配置结束请求,运行以下代码
*/
----------------------------

INSERT INTO "workflow_service"."request" (id, create_time, revoke_time, revoked, update_time, method, process_name, service) VALUES ('3d11371e-aee7-444d-a6b3-4e96963ebd80', '2019-04-27 07:07:29.781', NULL, 'f', '2019-04-27 07:07:29.781', 'cliFundEventSave', '资金录入经办复合流程', 'reference-data-service');
INSERT INTO "workflow_service"."request" (id, create_time, revoke_time, revoked, update_time, method, process_name, service) VALUES ('b8c6d7a9-72cf-4628-b10e-df910d574d44', '2019-04-27 07:07:29.806', NULL, 'f', '2019-04-27 07:07:29.806', 'trdTradeCreate', '交易录入经办复合流程', 'trade-service');

----------------------------

/*
若process表无status列,运行以下代码
*/
----------------------------

alter table "workflow_service"."process" drop column "status";
ALTER TABLE "workflow_service"."process" ADD "status" bool default(true);

----------------------------

/*
若process表有stauts列但无数据,运行以下代码
*/
----------------------------
/*
UPDATE "workflow_service"."process" SET status = true where process_name = '资金录入经办复合流程'
UPDATE "workflow_service"."process" SET status = true where process_name = '交易录入经办复合流程'
*/
----------------------------