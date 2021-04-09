# -*- coding: utf-8 -*-
from trade_import.db_utils import ROTCCompanyType, get_session

LEVEL_TWO_TYPE_RISK_SUB_COMPANY = '风险子公司'


def get_sub_company_from_oracle():
    session = None
    try:
        session = get_session()
        sub_companies = session.query(
            ROTCCompanyType
        ).filter(
            ROTCCompanyType.LEVELTWOTYPE == LEVEL_TWO_TYPE_RISK_SUB_COMPANY
        ).all()
    finally:
        if session is not None:
            session.close()
    return covert(sub_companies)


def covert(sub_companies):
    if sub_companies is None:
        return []
    res = []
    for i in sub_companies:
        if i is None:
            continue
        sub = {}
        sub['futuresId'] = i.FUTURESID
        sub['levelOneType'] = i.LEVELONETYPE
        sub['levelTwoType'] = i.LEVELTWOTYPE
        sub['classifyName'] = i.CLASSIFYNAME
        sub['clientName'] = i.CLIENTNAME
        sub['nocId'] = i.NOCID
        res.append(sub)
    return res
