import redis


class RedisUtils:
    host = ''
    port = -1
    redis_pool = None

    @staticmethod
    def set_redis_params(params):
        host, port = params
        if host is not None:
            RedisUtils.host = host
        if port is not None:
            RedisUtils.port = port
        RedisUtils.redis_pool = redis.ConnectionPool(host=RedisUtils.host, port=RedisUtils.port)

    @staticmethod
    def get_redis_session():
        if RedisUtils.redis_pool is not None:
            redis_instance = redis.Redis(connection_pool=RedisUtils.redis_pool)
            return redis_instance
        else:
            return None
