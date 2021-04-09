from terminal.dao import OptionStructureRepo
from terminal.dto import OptionStructureDTO


class OptionStructureService:

    @staticmethod
    def get_instrument_option_chain(db_session, underlier, trade_date, has_dividend):
        option_dbo_list = OptionStructureRepo.get_instrument_option_chain(db_session, underlier, trade_date)
        # 过滤掉带分红的option
        if has_dividend is False:
            option_dbo_list = OptionStructureService.get_option_list_without_dividend(option_dbo_list)
        return OptionStructureService.to_dtos(option_dbo_list)

    @staticmethod
    def get_option_chain_by_variety_type(db_session, variety_type, trade_date, has_dividend):
        option_dbo_list = OptionStructureRepo.get_option_chain_by_variety_type(db_session, variety_type, trade_date)
        # 过滤掉带分红的option
        if has_dividend is False:
            option_dbo_list = OptionStructureService.get_option_list_without_dividend(option_dbo_list)
        return OptionStructureService.to_dtos(option_dbo_list)

    @staticmethod
    def get_option_list_without_dividend(option_dbo_list):
        filtered_option_dbo_list = []
        for option in option_dbo_list:
            if option.fullName is not None and option.fullName[-1] != 'A':
                filtered_option_dbo_list.append(option)
        return filtered_option_dbo_list

    @staticmethod
    def to_dtos(dbos):
        return [OptionStructureService.to_dto(dbo) for dbo in dbos]

    @staticmethod
    def to_dto(dbo):
        if dbo is not None:
            dto = OptionStructureDTO()
            dto.instrumentId = dbo.instrumentId
            dto.underlier = dbo.underlier
            dto.expirationDate = dbo.expireDate
            # TODO: need to refactor
            dto.strike = float(dbo.exercisePrice)
            dto.optionType = dbo.optionType
            dto.fullName = dbo.fullName
        else:
            dto = None
        return dto
