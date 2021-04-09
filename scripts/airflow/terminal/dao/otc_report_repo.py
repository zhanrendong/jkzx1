from sqlalchemy import func
from terminal.dbo import OTCSummaryReport, OTCPositionSummaryReport, OTCTradeSummaryReport, OTCCusTypeReport, \
    OTCAssetToolReport, OTCMarketDistReport, OTCEtSubCompanyReport, OTCEtCusReport, OTCEtCommodityReport, \
    OTCMarketManipulateReport, OTCCompPropagateReport, OTCCusPosPercentageReport


class OTCReportRepo:

    @staticmethod
    def get_page_reports_by_date_range(db_session, cur_dbo, start_date, end_date, page, page_size):
        total_count = db_session.query(func.count(cur_dbo.UUID))\
            .filter(cur_dbo.STATDATE >= start_date, cur_dbo.STATDATE <= end_date)\
            .scalar()
        if page is None and page_size is None:
            reports = db_session.query(cur_dbo)\
                .filter(cur_dbo.STATDATE >= start_date, cur_dbo.STATDATE <= end_date)\
                .order_by(cur_dbo.STATDATE).all()
        else:
            reports = db_session.query(cur_dbo) \
                .filter(cur_dbo.STATDATE >= start_date, cur_dbo.STATDATE <= end_date) \
                .order_by(cur_dbo.STATDATE) \
                .limit(page_size).offset(page_size * page)\
                .all()
        return total_count, reports

    @staticmethod
    def get_otc_summary_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCSummaryReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_trade_summary_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCTradeSummaryReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_position_summary_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCPositionSummaryReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_asset_tool_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCAssetToolReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_cus_type_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCCusTypeReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_market_dist_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCMarketDistReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_et_commodity_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCEtCommodityReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_et_cus_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCEtCusReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_et_sub_company_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCEtSubCompanyReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_market_manipulate_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCMarketManipulateReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_comp_propagate_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCCompPropagateReport, start_date, end_date,
                                                            page, page_size)

    @staticmethod
    def get_otc_cus_pos_percentage_list(db_session, start_date, end_date, page, page_size):
        return OTCReportRepo.get_page_reports_by_date_range(db_session, OTCCusPosPercentageReport, start_date, end_date,
                                                            page, page_size)
