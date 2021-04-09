# -*- encoding: utf-8 -*-
import init_auth
import utils

calendar_name = 'DEFAULT_CALENDAR'
vol_calendar_name = 'DEFAULT_VOL_CALENDAR'

holidays = [{
    'holiday': '2019-01-01',
    'note': '元旦'
}, {
    'holiday': '2019-02-04',
    'note': '除夕'
}, {
    'holiday': '2019-02-05',
    'note': '春节'
}, {
    'holiday': '2019-02-06',
    'note': '初二'
}, {
    'holiday': '2019-02-07',
    'note': '初三'
}, {
    'holiday': '2019-02-08',
    'note': '初四'
}, {
    'holiday': '2019-05-01',
    'note': '劳动节'
}, {
    'holiday': '2019-06-07',
    'note': '端午节'
}, {
    'holiday': '2019-09-13',
    'note': '中秋节'
}, {
    'holiday': '2019-10-01',
    'note': '国庆节'
}, {
    'holiday': '2019-10-02',
    'note': '国庆节'
}, {
    'holiday': '2019-10-03',
    'note': '国庆节'
}, {
    'holiday': '2019-10-04',
    'note': '国庆节'
}, {
    'holiday': '2019-10-07',
    'note': '国庆节'
}, {
    'holiday': '2020-01-01',
    'note': '元旦'
}, {
    'holiday': '2020-01-27',
    'note': '初三'
}, {
    'holiday': '2020-01-28',
    'note': '初四'
}, {
    'holiday': '2020-01-29',
    'note': '初五'
}, {
    'holiday': '2020-01-30',
    'note': '初六'
}, {
    'holiday': '2020-04-04',
    'note': '清明节'
}, {
    'holiday': '2020-05-01',
    'note': '劳动节'
}, {
    'holiday': '2020-06-25',
    'note': '端午节'
}, {
    'holiday': '2020-10-01',
    'note': '国庆节'
}, {
    'holiday': '2020-10-02',
    'note': '国庆节'
}, {
    'holiday': '2020-10-05',
    'note': '国庆节'
}, {
    'holiday': '2021-01-01',
    'note': '元旦'
}, {
    'holiday': '2021-02-12',
    'note': '初一'
}, {
    'holiday': '2021-02-15',
    'note': '初四'
}, {
    'holiday': '2021-02-16',
    'note': '初五'
}, {
    'holiday': '2021-02-17',
    'note': '初六'
}, {
    'holiday': '2021-04-05',
    'note': '清明节'
}, {
    'holiday': '2021-06-14',
    'note': '端午节'
}, {
    'holiday': '2021-10-01',
    'note': '国庆节'
}, {
    'holiday': '2021-10-04',
    'note': '国庆节'
}, {
    'holiday': '2021-10-05',
    'note': '国庆节'
}, {
    'holiday': '2022-01-03',
    'note': '元旦'
}, {
    'holiday': '2022-02-01',
    'note': '初一'
}, {
    'holiday': '2022-02-02',
    'note': '初二'
}, {
    'holiday': '2022-02-03',
    'note': '初三'
}, {
    'holiday': '2022-02-04',
    'note': '初四'
}, {
    'holiday': '2022-04-05',
    'note': '清明节'
}, {
    'holiday': '2022-06-03',
    'note': '端午节'
}, {
    'holiday': '2022-10-03',
    'note': '国庆节'
}, {
    'holiday': '2022-10-04',
    'note': '国庆节'
}, {
    'holiday': '2022-10-05',
    'note': '国庆节'
}, {
    'holiday': '2023-01-02',
    'note': '元旦'
}, {
    'holiday': '2023-01-23',
    'note': '初二'
}, {
    'holiday': '2023-01-24',
    'note': '初三'
}, {
    'holiday': '2023-01-25',
    'note': '初四'
}, {
    'holiday': '2023-01-26',
    'note': '初五'
}, {
    'holiday': '2023-01-27',
    'note': '初六'
}, {
    'holiday': '2023-04-05',
    'note': '清明节'
}, {
    'holiday': '2023-05-01',
    'note': '劳动节'
}, {
    'holiday': '2023-06-22',
    'note': '端午节'
}, {
    'holiday': '2023-09-29',
    'note': '中秋节'
}, {
   'holiday': '2023-10-02',
   'note': '国庆节'
}, {
   'holiday': '2023-10-03',
   'note': '国庆节'
}, {
    'holiday': '2023-10-04',
    'note': '国庆节'
}, {
    'holiday': '2023-10-05',
    'note': '国庆节'
}, {
     'holiday': '2024-01-01',
     'note': '元旦'
}, {
    'holiday': '2024-02-12',
    'note': '初三'
}, {
    'holiday': '2024-02-13',
    'note': '初四'
}, {
    'holiday': '2024-02-14',
    'note': '初五'
}, {
    'holiday': '2024-02-15',
    'note': '初六'
}, {
    'holiday': '2024-04-04',
    'note': '清明节'
}, {
    'holiday': '2024-05-01',
    'note': '劳动节'
}, {
    'holiday': '2024-06-10',
    'note': '端午节'
}, {
    'holiday': '2024-09-17',
    'note': '中秋节'
}, {
   'holiday': '2024-10-01',
   'note': '国庆节'
}, {
   'holiday': '2024-10-02',
   'note': '国庆节'
}, {
    'holiday': '2024-10-03',
    'note': '国庆节'
}, {
    'holiday': '2024-10-04',
    'note': '国庆节'
}]


def create_calendar(calendar_id, name, holidays, host, token):
    utils.call('refTradingCalendarCreate', {
        'calendarId': calendar_id,
        'calendarName': name,
        'holidays': holidays
    }, 'reference-data-service', host, token)


def create_vol_calendar(calendar_id, holidays, weekend_weight, holiday_weight, host, token):
    utils.call('refVolCalendarCreate', {
        'calendarId': calendar_id,
        'weekendWeight': weekend_weight,
        'specialDates': [{
            'specialDate': h['holiday'],
            'weight': holiday_weight,
            'note': h['note']
        } for h in holidays]
    }, 'reference-data-service', host, token)


if __name__ == '__main__':
    host = init_auth.host
    token = utils.login(init_auth.script_user_name, init_auth.script_user_password, host)

    print('========== Creating default trading calendar ==========')
    create_calendar(calendar_name, '交易日历', holidays, host, token)

    print('========== Creating default vol calendar ==========')
    create_vol_calendar(vol_calendar_name, holidays, 0, 0, host, token)
