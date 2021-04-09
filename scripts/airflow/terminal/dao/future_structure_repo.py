from terminal.dbo import FutureStructure
from sqlalchemy import and_, distinct
from terminal.utils import Logging

logging = Logging.getLogger(__name__)


class FutureStructureRepo:
    @staticmethod
    def load_future_structure(db_session):
        future_structures = db_session.query(FutureStructure).all()
        return future_structures

    @staticmethod
    def load_future_structure_dict(db_session):
        future_structures = FutureStructureRepo.load_future_structure(db_session)
        future_structure_dict = {}
        for structure in future_structures:
            future_structure_dict[structure.instrumentId] = structure
        return future_structure_dict

    @staticmethod
    def update_structure_list(db_session, current_structure_dict, existing_structure_dict):
        missing_structures = []
        for key in current_structure_dict.keys():
            if key not in existing_structure_dict:
                missing_structures.append(current_structure_dict[key])
        logging.info('准备添加期权到数据库')
        missing_ids = []
        for structure in missing_structures:
            missing_ids.append(structure.instrumentId)
            db_session.add(structure)
        db_session.flush()
        db_session.commit()
        logging.info('新增%d个期权结构到数据库：%s' % (len(missing_structures), ','.join(missing_ids)))