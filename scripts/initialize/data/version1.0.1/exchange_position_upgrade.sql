UPDATE exchange_service.position_record SET total_buy = 0 WHERE total_buy IS NULL;
UPDATE exchange_service.position_record SET total_sell = 0 WHERE total_sell IS NULL;

UPDATE exchange_service.position_snapshot SET total_buy = 0 WHERE total_buy IS NULL;
UPDATE exchange_service.position_snapshot SET total_sell = 0 WHERE total_sell IS NULL;