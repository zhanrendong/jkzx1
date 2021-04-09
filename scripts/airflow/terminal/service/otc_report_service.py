from terminal.dto import OTCSummaryReportDTO, OTCTradeSummaryReportDTO, OTCPositionSummaryReportDTO, \
    OTCAssetToolReportDTO, OTCMarketDistReportDTO,\
    PageDTO, OTCCusTypeReportDTO, OTCEtCommodityReportDTO, OTCEtCusReportDTO, OTCEtSubCompanyReportDTO, \
    OTCCompPropagateReportDTO, OTCMarketManipulateReportDTO, OTCCusPosPercentageReport
from terminal.dao import OTCReportRepo


class OTCReportService:

    @staticmethod
    def get_otc_summary_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_summary_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCSummaryReportDTO(stat_date=data.STATDATE,
                                          trd_notion_amount=data.TRDNOTIONAMOUNT,
                                          trd_trans_num=data.TRDTRANSNUM,
                                          opt_fee_amount=data.OPTFEEAMOUNT,
                                          trd_cus_num=data.TRDCUSNUM,
                                          pos_notion_amount=data.POSNOTIONAMOUNT,
                                          pos_trans_num=data.POSTRANSNUM,
                                          pos_cus_num=data.POSCUSNUM,
                                          in_market_cus_num=data.INMARKETCUSNUM,
                                          full_market_cus_num=data.FULLMARKETCUSNUM,
                                          pos_value=data.POSVALUE)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_trade_summary_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_trade_summary_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCTradeSummaryReportDTO(stat_date=data.STATDATE,
                                               open_trd_notion_amount=data.OPENTRDNOTIONAMOUNT,
                                               close_trd_notion_amount=data.CLOSETRDNOTIONAMOUNT,
                                               end_trd_notion_amount=data.ENDTRDNOTIONAMOUNT,
                                               trd_notion_amount=data.TRDNOTIONAMOUNT,
                                               trd_open_premium=data.TRDOPENPREMIUM,
                                               trd_close_premium=data.TRDCLOSEPREMIUM,
                                               trd_end_premium=data.TRDENDPREMIUM,
                                               premium_amount=data.PREMIUMAMOUNT,
                                               trd_open_cus_num=data.TRDOPENCUSNUM,
                                               trd_close_cus_num=data.TRDCLOSECUSNUM,
                                               trd_end_cus_num=data.TRDENDCUSNUM,
                                               trd_cus_num=data.TRDCUSNUM)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_position_summary_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_position_summary_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCPositionSummaryReportDTO(stat_date=data.STATDATE,
                                                  pos_call_buy_amount=data.POSCALLBUYAMOUNT,
                                                  pos_put_buy_amount=data.POSPUTBUYAMOUNT,
                                                  pos_other_buy_amount=data.POSOTHERBUYAMOUNT,
                                                  pos_buy_amount_total=data.POSBUYAMOUNTTOTAL,
                                                  pos_call_sell_amount=data.POSCALLSELLAMOUNT,
                                                  pos_put_sell_amount=data.POSPUTSELLAMOUNT,
                                                  pos_sell_amount_total=data.POSSELLAMOUNTTOTAL,
                                                  pos_call_buy_c_value=data.POSCALLBUYCVALUE,
                                                  pos_put_buy_c_value=data.POSPUTBUYCVALUE,
                                                  pos_other_buy_c_value=data.POSOTHERBUYCVALUE,
                                                  pos_buy_value_total=data.POSBUYVALUETOTAL,
                                                  pos_call_sell_c_value=data.POSCALLSELLCVALUE,
                                                  pos_put_sell_c_value=data.POSPUTSELLCVALUE,
                                                  pos_sell_value_total=data.POSSELLVALUETOTAL)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_asset_tool_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_asset_tool_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCAssetToolReportDTO(stat_date=data.STATDATE,
                                            asset_type=data.ASSETTYPE,
                                            tool_type=data.TOOLTYPE,
                                            trd_trans_num=data.TRDTRANSNUM,
                                            trd_notion_amount=data.TRDNOTIONAMOUNT,
                                            pos_trans_num=data.POSTRANSNUM,
                                            pos_notion_amount=data.POSNOTIONAMOUNT,
                                            in_market_cus_num=data.INMARKETCUSNUM)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_cus_type_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_cus_type_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCCusTypeReportDTO(stat_date=data.STATDATE,
                                          cus_type=data.CUSTYPE,
                                          asset_type=data.ASSETTYPE,
                                          trd_trans_num=data.TRDTRANSNUM,
                                          trd_notion_amount=data.TRDNOTIONAMOUNT,
                                          pos_trans_num=data.POSTRANSNUM,
                                          pos_notion_amount=data.POSNOTIONAMOUNT,
                                          in_market_cus_num=data.INMARKETCUSNUM)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_market_dist_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_market_dist_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCMarketDistReportDTO(stat_date=data.STATDATE,
                                             total_pos=data.TOTALPOS,
                                             top3_pos=data.TOP3POS,
                                             top3_pos_dist=data.TOP3POSDIST,
                                             top5_pos=data.TOP5POS,
                                             top5_pos_dist=data.TOP5POSDIST,
                                             top10_pos=data.TOP10POS,
                                             top10_pos_dist=data.TOP10POSDIST,
                                             dist_type=data.DISTTYPE)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_et_commodity_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_et_commodity_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCEtCommodityReportDTO(stat_date=data.STATDATE,
                                              commodity_id=data.COMMODITYID,
                                              otc_pos_amount=data.OTCPOSAMOUNT,
                                              otc_pos_ratio=data.OTCPOSRATIO,
                                              et_pos_amount=data.ETPOSAMOUNT,
                                              et_pos_ratio=data.ETPOSRATIO,
                                              otc_et_ratio=data.OTCETRATIO)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_et_cus_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_et_cus_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCEtCusReportDTO(stat_date=data.STATDATE,
                                        et_account_cus_num=data.ETACCOUNTCUSNUM,
                                        otc_cus_pos_amount=data.OTCCUSPOSAMOUNT,
                                        et_cus_pos_amount=data.ETCUSPOSAMOUNT,
                                        et_cus_right=data.ETCUSRIGHT)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_et_sub_company_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_et_sub_company_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCEtSubCompanyReportDTO(stat_date=data.STATDATE,
                                               commodity_id=data.COMMODITYID,
                                               main_body_name=data.MAINBODYNAME,
                                               otc_sub_pos_amount=data.OTCSUBPOSAMOUNT,
                                               et_sub_pos_amount=data.ETSUBPOSAMOUNT)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_market_manipulate_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_market_manipulate_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCMarketManipulateReportDTO(stat_date=data.STATDATE,
                                                   inter_comp_cus_num=data.INTERCOMPCUSNUM,
                                                   inter_comp_trd=data.INTERCOMPTRD,
                                                   inter_comp_pos=data.INTERCOMPPOS)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_comp_propagate_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_comp_propagate_list(db_session, start_date, end_date, page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCCompPropagateReportDTO(stat_date=data.STATDATE,
                                                inter_comp_num=data.INTERCOMPNUM,
                                                inter_comp_trd_amount=data.INTERCOMPTRDAMOUNT,
                                                comp_trd_amount_total=data.COMPTRDAMOUNTTOTAL,
                                                trd_ratio=data.TRDRATIO,
                                                inter_comp_pos_amount=data.INTERCOMPPOSAMOUNT,
                                                comp_pos_amount_total=data.COMPPOSAMOUNTTOTAL,
                                                pos_ratio=data.POSRATIO)
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)

    @staticmethod
    def get_otc_cus_pos_percentage_report(db_session, start_date, end_date, page, page_size):
        total_count, report_list = OTCReportRepo.get_otc_cus_pos_percentage_list(db_session, start_date, end_date,
                                                                                 page, page_size)
        report_dto_list = []
        total_count = total_count if total_count is not None else 0
        if report_list is not None and len(report_list) > 0:
            for data in report_list:
                dto = OTCCusPosPercentageReport(stat_date=data.STATDATE,
                                                analogue_name=data.ANALOGUENAME,
                                                under_ass_varit=data.UNDERASSVARIT,
                                                cus_positive_delta=data.CUSPOSITIVEDELTA,
                                                cus_short_position=data.CUSSHORTPOSITION,
                                                cus_negative_delta=data.CUSNEGATIVEDELTA,
                                                cus_long_position=data.CUSLONGPOSITION,
                                                exchange_max_pos=data.EXCHANGEMAXPOS,
                                                exchange_pos=data.EXCHANGEPOS,
                                                cus_exg_otc_ratio=data.CUSEXGOTCRATIO,
                                                )
                report_dto_list.append(dto)
        return PageDTO(total_count, report_dto_list)