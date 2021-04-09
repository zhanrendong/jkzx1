import os


class SpiderSetting:
    # term list
    term_list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 22, 44, 66, 123]

    # windows平台 chromedriver.exe路径
    default_windows_chromedriver_path = r"C:\Program Files (x86)\Google\Chrome\Application\chromedriver.exe"
    windows_chromedriver_path = os.environ.get('CHROMEDRIVER_PATH', default_windows_chromedriver_path)

    # linux平台 chromedriver.sh路径
    default_linux_chromedriver_path = r"/root/chromedriver"
    linux_chromedriver_path = os.environ.get('CHROMEDRIVER_PATH', default_linux_chromedriver_path)

    # 鲁证 url
    luzheng_get_url = 'http://www.luzhengqh.com/rest/weixin/otcpricelist'
    luzheng_post_url = 'http://www.luzhengqh.com/rest/weixin/getOptionPrice'
    # 南华 url
    nanhua_url = 'http://www.nanhuacapital.com/fd/'
    # 华泰 url
    huatai_url = 'http://www.htoption.cn/weixin/app/index.php?i=4&c=entry&eid=62&wxref=mp.weixin.qq.com#'
    # 银河列表页 url
    yinhe_list_url = 'http://47.102.139.227:8150/api'
    yinhe_detail_url = 'http://47.102.139.227:8150/api'

    # todo 华泰 cookies,有效期问题
    huatai_cookies1 = {'name': '20171212', 'value': 'true'}
    huatai_cookies = {'name': 'PHPSESSID', 'value': 'c3bd2e1e10162a3c45700abf367665f6'}
