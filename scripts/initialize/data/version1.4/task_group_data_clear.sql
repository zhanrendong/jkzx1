----审批组与任务节点关联数据冗余数据清洗
delete from workflow_service.task_approve_group where task_node_id not in (select id from workflow_service.task_node);