import utils
from timeit import default_timer as timer

token = utils.login('admin', '12345')

trade = {
        		"trade": {
        			"bookName": "test",
        			"tradeId": "trade002",
        			"trader": "LU",
        			"comment": "no comment",
        			"tradeStatus": "LIVE",
        			"tradeDate": "2019-01-11",
        			"partyCode": "party01",
        			"partyName": "party01",
        			"salesCode": "sales01",
        			"salesName": "sales01",
        			"salesCommission": "0",
        			"positions": [{
        				"positionId": "trade001.01",

        				"lcmEventType": "OPEN",
        				"productType": "VANILLA_EUROPEAN",
        				"assetClass": "EQUITY",
        				"counterPartyCode": "party02",
        				"counterPartyName": "party02",
        				"positionAccountCode": "acct01",
        				"positionAccountName": "acct01",
        				"counterPartyAccountCode": "acct02",
        				"counterPartyAccountName": "acct02",
        				"asset": {
        					"instrumentId": "600519.SH",
        					"direction": "BUYER",
        					"initialSpot": "618",
        					"optionType": "CALL",
        					"strike": "618",
        					"expirationDate": "2019-04-11",
        					"expirationTime": "15:00:00",
        					"effectiveDate": "2019-01-11",
        					"underlyerMultiplier": "1",
        					"notional": "1000000",
        					"premium": "40000",
        					"premiumPaymentDate": "2019-01-11"
        				}
        			}]
        		},
        		"validTime": "2019-01-11T00:00:00"
        	}

start = timer()
for i in range(10000):
  trade['trade']['tradeId'] = 'stress3_' + str(i)
  trade['trade']['positions'][0]['positionId'] = 'stress3_' + str(i) + '.1'
  utils.call('tradeCreate', trade, service='trade-service', token=token)
end = timer()
print(end - start)

start = timer()
utils.call('prcPrice', {}, service='pricing-service', token=token)
end = timer()
print(end - start)
