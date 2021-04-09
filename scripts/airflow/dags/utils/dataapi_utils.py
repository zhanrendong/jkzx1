# -*- coding: utf-8 -*-
import http.client
import urllib
import gzip
import json
from io import BytesIO
from dags.conf.settings import app_config


class Client:
    domain = 'api.wmcloud.com'
    port = 443
    token = ''
    #设置因网络连接，重连的次数
    reconnectTimes=2
    httpClient = None

    def __init__( self ):
        self.httpClient = http.client.HTTPSConnection(self.domain, self.port, timeout=60)
        # 初始化连接使用的token
        self.token = app_config['tonglian']['token']

    def __del__( self ):
        if self.httpClient is not None:
            self.httpClient.close()

    def encodepath(self, path):
        #转换参数的编码
        start=0
        n=len(path)
        re=''
        i=path.find('=',start)
        while i!=-1 :
            re+=path[start:i+1]
            start=i+1
            i=path.find('&',start)
            if(i>=0):
                for j in range(start,i):
                    if(path[j]>'~'):
                        re+=urllib.quote(path[j])
                    else:
                        re+=path[j]
                re+='&'
                start=i+1
            else:
                for j in range(start,n):
                    if(path[j]>'~'):
                        re+=urllib.quote(path[j])
                    else:
                        re+=path[j]
                start=n
            i=path.find('=',start)
        return re

    def init(self, token):
        self.token=token

    def getData(self, path):
        result = None
        path='/data/v1' + path
        print (path)
        path=self.encodepath(path)
        for i in range(self.reconnectTimes):
            try:
                #set http header here
                print(path)
                self.httpClient.request('GET', path, headers = {"Authorization": "Bearer " + self.token,
                                                                "Accept-Encoding": "gzip, deflate"})
                #make request
                response = self.httpClient.getresponse()
                result = response.read()
                compressedstream = BytesIO(result)
                gziper = gzip.GzipFile(fileobj=compressedstream)
                try:
                    result = gziper.read()
                except:
                    pass
                return response.status, result
            except Exception as e:
                if i == self.reconnectTimes-1:
                    raise e
                if self.httpClient is not None:
                    self.httpClient.close()
                self.httpClient = http.client.HTTPSConnection(self.domain, self.port, timeout=60)
        return -1, result


if __name__ == '__main__':
    client = Client()
    dates = ['20190320', '20190321']
    # python datetime
    startDate = '20190320'
    endDate = '20190329'
    while startDate < endDate:
        url = '/api/market/getOptionTicksHistOneDay.json?optionId=10001698&date=%s' % startDate
        # 请求数据并进行处理
        code, result = client.getData(url)
        ret = json.loads(result)
        print(code)
        startDate = startDate + 1