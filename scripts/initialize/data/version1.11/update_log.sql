update auth_service.error_log set created_date_at = date(create_time) where created_date_at is null;
update auth_service.sys_log set created_date_at = date(create_time) where created_date_at is null;
