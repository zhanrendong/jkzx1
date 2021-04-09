from .base_dto import BaseDTO


# 市场规模
class OTCSummaryReportDTO(BaseDTO):
    def __init__(self, stat_date=None, trd_notion_amount=None, trd_trans_num=None, opt_fee_amount=None, trd_cus_num=None,
                 pos_notion_amount=None, pos_trans_num=None, pos_cus_num=None, in_market_cus_num=None,
                 full_market_cus_num=None, pos_value=None):
        self.statDate = stat_date
        self.trdNotionAmount = trd_notion_amount
        self.trdTransNum = trd_trans_num
        self.optFeeAmount = opt_fee_amount
        self.trdCusNum = trd_cus_num
        self.posNotionAmount = pos_notion_amount
        self.posTransNum = pos_trans_num
        self.posCusNum = pos_cus_num
        self.inMarketCusNum = in_market_cus_num
        self.fullMarketCusNum = full_market_cus_num
        self.posValue = pos_value


# 成交结构
class OTCTradeSummaryReportDTO(BaseDTO):
    def __init__(self, stat_date=None, open_trd_notion_amount=None, close_trd_notion_amount=None,
                 end_trd_notion_amount=None, trd_notion_amount=None, trd_open_premium=None, trd_close_premium=None,
                 trd_end_premium=None, premium_amount=None, trd_open_cus_num=None, trd_close_cus_num=None,
                 trd_end_cus_num=None, trd_cus_num=None):
        self.statDate = stat_date
        self.openTrdNotionAmount = open_trd_notion_amount
        self.closeTrdNotionAmount = close_trd_notion_amount
        self.endTrdNotionAmount = end_trd_notion_amount
        self.trdNotionAmount = trd_notion_amount
        self.trdOpenPremium = trd_open_premium
        self.trdClosePremium = trd_close_premium
        self.trdEndPremium = trd_end_premium
        self.premiumAmount = premium_amount
        self.trdOpenCusNum = trd_open_cus_num
        self.trdCloseCusNum = trd_close_cus_num
        self.trdEndCusNum = trd_end_cus_num
        self.trdCusNum = trd_cus_num


# 持仓结构
class OTCPositionSummaryReportDTO(BaseDTO):
    def __init__(self, stat_date=None, pos_call_buy_amount=None, pos_put_buy_amount=None, pos_other_buy_amount=None,
                 pos_buy_amount_total=None, pos_call_sell_amount=None, pos_put_sell_amount=None,
                 pos_sell_amount_total=None, pos_call_buy_c_value=None, pos_put_buy_c_value=None, pos_other_buy_c_value=None,
                 pos_buy_value_total=None, pos_call_sell_c_value=None, pos_put_sell_c_value=None, pos_sell_value_total=None):
        self.statDate = stat_date
        self.posCallBuyAmount = pos_call_buy_amount
        self.posPutBuyAmount = pos_put_buy_amount
        self.posOtherBuyAmount = pos_other_buy_amount
        self.posBuyAmountTotal = pos_buy_amount_total
        self.posCallSellAmount = pos_call_sell_amount
        self.posPutSellAmount = pos_put_sell_amount
        self.posSellAmountTotal = pos_sell_amount_total
        self.posCallBuyCValue = pos_call_buy_c_value
        self.posPutBuyCValue = pos_put_buy_c_value
        self.posOtherBuyCValue = pos_other_buy_c_value
        self.posBuyValueTotal = pos_buy_value_total
        self.posCallSellCValue = pos_call_sell_c_value
        self.posPutSellCValue = pos_put_sell_c_value
        self.posSellValueTotal = pos_sell_value_total


# 资产和工具结构
class OTCAssetToolReportDTO(BaseDTO):
    def __init__(self, stat_date=None, asset_type=None, tool_type=None, trd_trans_num=None, trd_notion_amount=None,
                 pos_trans_num=None, pos_notion_amount=None, in_market_cus_num=None):
        self.statDate = stat_date
        self.assetType = asset_type
        self.toolType = tool_type
        self.trdTransNum = trd_trans_num
        self.trdNotionAmount = trd_notion_amount
        self.posTransNum = pos_trans_num
        self.posNotionAmount = pos_notion_amount
        self.inMarketCusNum = in_market_cus_num


# 客户类型结构
class OTCCusTypeReportDTO(BaseDTO):
    def __init__(self, stat_date=None, cus_type=None, asset_type=None, trd_trans_num=None, trd_notion_amount=None,
                 pos_trans_num=None, pos_notion_amount=None, in_market_cus_num=None):
        self.statDate = stat_date
        self.cusType = cus_type
        self.assetType = asset_type
        self.trdTransNum = trd_trans_num
        self.trdNotionAmount = trd_notion_amount
        self.posTransNum = pos_trans_num
        self.posNotionAmount = pos_notion_amount
        self.inMarketCusNum = in_market_cus_num


# 市场集中度
class OTCMarketDistReportDTO(BaseDTO):
    def __init__(self, stat_date=None, total_pos=None, top3_pos=None, top3_pos_dist=None, top5_pos=None,
                 top5_pos_dist=None, top10_pos=None, top10_pos_dist=None, dist_type=None):
        self.statDate = stat_date
        self.totalPos = total_pos
        self.top3Pos = top3_pos
        self.top3PosDist = top3_pos_dist
        self.top5Pos = top5_pos
        self.top5PosDist = top5_pos_dist
        self.top10Pos = top10_pos
        self.top10PosDist = top10_pos_dist
        self.distType = dist_type


# 品种联动
class OTCEtCommodityReportDTO(BaseDTO):
    def __init__(self, stat_date=None, commodity_id=None, otc_pos_amount=None, otc_pos_ratio=None, et_pos_amount=None,
                 et_pos_ratio=None, otc_et_ratio=None):
        self.statDate = stat_date
        self.commodityId = commodity_id
        self.otcPosAmount = otc_pos_amount
        self.otcPosRatio = otc_pos_ratio
        self.etPosAmount = et_pos_amount
        self.etPosRatio = et_pos_ratio
        self.otcEtRatio = otc_et_ratio


# 交易对手方联动
class OTCEtCusReportDTO(BaseDTO):
    def __init__(self, stat_date=None, et_account_cus_num=None, otc_cus_pos_amount=None, et_cus_pos_amount=None,
                 et_cus_right=None):
        self.statDate = stat_date
        self.etAccountCusNum = et_account_cus_num
        self.otcCusPosAmount = otc_cus_pos_amount
        self.etCusPosAmount = et_cus_pos_amount
        self.etCusRight = et_cus_right


# 子公司联动
class OTCEtSubCompanyReportDTO(BaseDTO):
    def __init__(self, stat_date=None, commodity_id=None, main_body_name=None, otc_sub_pos_amount=None,
                 et_sub_pos_amount=None):
        self.statDate = stat_date
        self.commodityId = commodity_id
        self.mainBodyName = main_body_name
        self.otcSubPosAmount = otc_sub_pos_amount
        self.etSubPosAmount = et_sub_pos_amount


# 操纵风险
class OTCMarketManipulateReportDTO(BaseDTO):
    def __init__(self, stat_date=None, inter_comp_cus_num=None, inter_comp_trd=None, inter_comp_pos=None):
        self.statDate = stat_date
        self.interCompCusNum = inter_comp_cus_num
        self.interCompTrd = inter_comp_trd
        self.interCompPos = inter_comp_pos


# 子公司传染风险
class OTCCompPropagateReportDTO(BaseDTO):
    def __init__(self, stat_date=None, inter_comp_num=None, inter_comp_trd_amount=None, comp_trd_amount_total=None,
                 trd_ratio=None, inter_comp_pos_amount=None, comp_pos_amount_total=None, pos_ratio=None):
        self.statDate = stat_date
        self.interCompNum = inter_comp_num
        self.interCompTrdAmount = inter_comp_trd_amount
        self.compTrdAmountTotal = comp_trd_amount_total
        self.trdRatio = trd_ratio
        self.interCompPosAmount = inter_comp_pos_amount
        self.compPosAmountTotal = comp_pos_amount_total
        self.posRatio = pos_ratio


# 对手方场内外合并持仓占比
class OTCCusPosPercentageReport(BaseDTO):
    def __init__(self, stat_date=None, analogue_name=None, under_ass_varit=None, cus_positive_delta=None,
                 cus_short_position=None, cus_negative_delta=None, cus_long_position=None, exchange_max_pos=None,
                 exchange_pos=None, cus_exg_otc_ratio=None):
        self.statDate = stat_date
        self.analogueName = analogue_name
        self.underAssVarit = under_ass_varit
        self.cusPositiveDelta = cus_positive_delta
        self.cusShortPosition = cus_short_position
        self.cusNegativeDelta = cus_negative_delta
        self.cusLongPosition = cus_long_position
        self.exchangeMaxPos = exchange_max_pos
        self.exchangePos = exchange_pos
        self.cusExgOtcRatio = cus_exg_otc_ratio


# 分页
class PageDTO(BaseDTO):
    def __init__(self, total_count=None, page=None):
        self.totalCount = total_count
        self.page = page
