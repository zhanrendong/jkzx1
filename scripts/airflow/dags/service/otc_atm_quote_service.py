from dags.dbo.db_model import create_db_session
import pandas as pd
from terminal.dbo import OtcAtmQuote
from terminal.dto import OptionProductType
import uuid
import os
from terminal.utils import DateTimeUtils, Logging
from datetime import datetime
logging = Logging.getLogger(__name__)


class OtcAtmQuoteService:
    @staticmethod
    def import_atm_vol_from_csv_file(file_path, run_id):
        fname, ext = os.path.splitext(file_path)
        valuation_date = DateTimeUtils.str2date(fname.split('_')[1], pattern='%Y%m%d')
        df = pd.read_csv(file_path)
        atm_quote_list = []
        current_time = datetime.now()
        for index, row in df.iterrows():
            atm_quote = OtcAtmQuote()
            atm_quote.uuid = uuid.uuid4()
            atm_quote.varietyType = row['udly'].strip()
            atm_quote.underlyer = row['code'].strip()
            atm_quote.legalEntityName = row['ctpt'].strip()
            atm_quote.expireDate = DateTimeUtils.str2date(row['exe_date'])
            if row['type'] == 'am':
                atm_quote.productType = OptionProductType.VANILLA_AMERICAN.name
            if row['type'] == 'eu':
                atm_quote.productType = OptionProductType.VANILLA_EUROPEAN.name
            atm_quote.ptm = float(row['ptm'])
            atm_quote.askVol = float(row['ask_vol_cal'])
            atm_quote.bidVol = float(row['bid_vol_cal'])
            atm_quote.volEdge = float(row['vol_edge_cal'])
            atm_quote.source = 'EXCEL_%s' % run_id
            atm_quote.valuationDate = valuation_date
            atm_quote.updatedAt = current_time
            atm_quote_list.append(atm_quote)
        db_sessin = create_db_session()
        db_sessin.add_all(atm_quote_list)
        db_sessin.commit()


if __name__ == '__main__':
    # 保存不同模型的目录名(绝对路径)
    file_dir = r'C:\Users\NUC\Desktop\OTC数据 - 副本'
    # root是指当前目录路径(文件夹的绝对路径)
    # dirs是指路径下所有的子目录(文件夹里的文件夹)
    # files是指路径下所有的文件(文件夹里所有的文件)
    run_id = uuid.uuid4()
    for root, dirs, files in os.walk(file_dir):
        for file in files:
            if os.path.splitext(file)[1] == '.csv':
                logging.info('开始导入：%s' % file)
                file_path = os.path.join(root, file)
                OtcAtmQuoteService.import_atm_vol_from_csv_file(file_path, run_id)
                logging.info('导入完成：%s' % file)
