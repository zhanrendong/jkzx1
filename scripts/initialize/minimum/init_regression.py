from datetime import datetime

import utils
from init_auth import init_auth
from init_calendars import init_calendar
from init_client import init_sales
from init_models import create_risk_free_curve
from init_params import script_user_password, script_user_name, host, risk_free_curve_name, tenors, rs
from init_pe import init_pe


def init_model():
    token = utils.login(script_user_name, script_user_password, host)
    current_date = '2020-08-27'
    val = datetime.strptime(current_date, '%Y-%m-%d').date()
    print('========== Creating models ==========')
    res = create_risk_free_curve('TRADER_RISK_FREE_CURVE', 'close', tenors, rs, val, host, token)
    print(res)
    res = create_risk_free_curve('TRADER_RISK_FREE_CURVE', 'intraday', tenors, rs, val, host, token)
    print(res)


if __name__ == '__main__':
    init_auth()
    init_model()
    init_sales()
    init_calendar()
    init_pe()
