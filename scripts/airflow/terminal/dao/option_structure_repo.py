from terminal.dbo import OptionStructure
from sqlalchemy import and_, distinct


class OptionStructureRepo:
    @staticmethod
    def get_instrument_option_chain(db_session, underlier, trade_date):
        option_dbo_list = db_session.query(OptionStructure).filter(
            and_(
                OptionStructure.underlier == underlier,
                OptionStructure.listedDate <= trade_date,
                OptionStructure.delistedDate > trade_date
            )
        ).all()
        return option_dbo_list

    @staticmethod
    def get_option_chain_by_variety_type(db_session, variety_type, trade_date):
        option_dbo_list = db_session.query(OptionStructure).filter(
            and_(
                OptionStructure.contractType == variety_type,
                OptionStructure.listedDate <= trade_date,
                OptionStructure.delistedDate > trade_date
            )
        ).all()
        return option_dbo_list

    @staticmethod
    def get_underlyers_by_contract_type(db_session, contract_type, observed_date):
        underlyers = db_session.query(distinct(OptionStructure.underlier)).filter(
            OptionStructure.contractType == contract_type, OptionStructure.listedDate < observed_date,
            OptionStructure.delistedDate > observed_date).all()
        underlyers = [_[0] for _ in underlyers]
        return underlyers

    @staticmethod
    def get_options_by_underlyer(db_session, underlier, observed_date):
        option_structure = db_session.query(OptionStructure).filter(
            OptionStructure.underlier == underlier, OptionStructure.expireDate >= observed_date,
            OptionStructure.listedDate < observed_date).order_by(
            OptionStructure.expireDate).all()
        return option_structure

    @staticmethod
    def check_commodity(db_session, contract_type):
        instrument = db_session.query(OptionStructure).filter(
            OptionStructure.contractType == contract_type
        ).first()
        return True if instrument else False

    @staticmethod
    def load_option_structure(db_session):
        option_structures = db_session.query(OptionStructure).all()
        return option_structures

    @staticmethod
    def load_option_structure_dict(db_session):
        option_structures = OptionStructureRepo.load_option_structure(db_session)
        option_structure_dict = {}
        for structure in option_structures:
            option_structure_dict[structure.instrumentId] = structure
        return option_structure_dict