UPDATE pricing_service.pricing_environment SET description = '交易-日内-自然日' WHERE pricing_environment_id = 'DEFAULT_INTRADAY';
UPDATE pricing_service.pricing_environment SET description = '交易-收盘-自然日' WHERE pricing_environment_id = 'DEFAULT_CLOSE';
UPDATE pricing_service.pricing_environment SET description = '风控-收盘-自然日' WHERE pricing_environment_id = 'RISK_CLOSE';
UPDATE pricing_service.pricing_environment SET description = '交易-收盘-交易日' WHERE pricing_environment_id = 'DEFAULT_CLOSE_CALENDARS';
UPDATE pricing_service.pricing_environment SET description = '风控-收盘-交易日' WHERE pricing_environment_id = 'RISK_CLOSE_CALENDARS';
UPDATE pricing_service.pricing_environment SET description = '交易-日内-交易日' WHERE pricing_environment_id = 'DEFAULT_INTRADAY_CALENDARS';
