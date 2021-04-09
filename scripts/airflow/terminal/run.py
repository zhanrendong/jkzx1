from tornado import ioloop
from terminal import create_app

if __name__ == "__main__":
    # 获取环境变量
    app = create_app('default')
    ioloop.IOLoop.current().start()
