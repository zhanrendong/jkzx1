from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method, exception_watch
from terminal.service import OTCReportService
from terminal.dto import CustomException
from terminal.utils import DateTimeUtils
import asyncio

REPORT_DATE_PATTERN = '%Y-%m-%d'


class OTCReportHandler(JsonRpcHandler):

    @method  # 市场规模
    @asyncio.coroutine
    @exception_watch
    def get_otc_summary_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_summary_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 成交结构
    @asyncio.coroutine
    @exception_watch
    def get_otc_trade_summary_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_trade_summary_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 持仓结构
    @asyncio.coroutine
    @exception_watch
    def get_otc_position_summary_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_position_summary_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 资产和工具结构
    @asyncio.coroutine
    @exception_watch
    def get_otc_asset_tool_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_asset_tool_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 客户类型结构
    @asyncio.coroutine
    @exception_watch
    def get_otc_cus_type_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_cus_type_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 市场集中度
    @asyncio.coroutine
    @exception_watch
    def get_otc_market_dist_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_market_dist_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 品种联动
    @asyncio.coroutine
    @exception_watch
    def get_otc_et_commodity_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_et_commodity_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 交易对手方联动
    @asyncio.coroutine
    @exception_watch
    def get_otc_et_cus_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_et_cus_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 子公司联动
    @asyncio.coroutine
    @exception_watch
    def get_otc_et_sub_company_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_et_sub_company_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 操纵风险
    @asyncio.coroutine
    @exception_watch
    def get_otc_market_manipulate_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_market_manipulate_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 子公司传染风险
    @asyncio.coroutine
    @exception_watch
    def get_otc_comp_propagate_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_comp_propagate_report(db_session, start_date, end_date, page, page_size)
        return reports

    @method  # 对手方场内外合并持仓占比
    @asyncio.coroutine
    @exception_watch
    def get_otc_cus_pos_percentage_report(self, start_date=None, end_date=None, page=None, page_size=None, current_user=None):
        with self.make_session() as db_session:
            if start_date is None or end_date is None:
                raise CustomException('所选日期不能为空')
            if (page is None and page_size is not None) or (page is not None and page_size is None):
                raise CustomException('分页参数page和page_size必须同时存在或不存在')
            DateTimeUtils.validate(start_date, REPORT_DATE_PATTERN)
            DateTimeUtils.validate(end_date, REPORT_DATE_PATTERN)
            reports = OTCReportService.get_otc_cus_pos_percentage_report(db_session, start_date, end_date, page, page_size)
        return reports
