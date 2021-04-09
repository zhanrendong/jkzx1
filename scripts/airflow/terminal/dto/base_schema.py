from marshmallow import Schema, fields, post_load, pre_load
import re

class BaseSchema(Schema):
    def camel(self, match):
        return match[1][1].upper().upper()

    def convert_snake_string(self, name: str) -> str:
        """Convert snake case string to camel case"""
        # 是jsonPRC/request里对应的方法
        # 反序列化时将驼峰命名——下划线
        return re.sub("([_][a-z])", self.camel, name)

    def convert_snake_case_keys(self, original_dict):
        """Converts all keys of a dict from snake case to camel case, recursively"""
        new_dict = dict()
        for key, val in original_dict.items():
            if isinstance(val, dict):
                # Recurse
                new_dict[self.convert_snake_string(key)] = self.convert_snake_case_keys(val)
            else:
                new_dict[self.convert_snake_string(key)] = val
        return new_dict

    @pre_load
    def pre(self, data):
        return self.convert_snake_case_keys(data)


    # def xload(self, data, many=None, partial=None):
    #     data = self.convert_snake_case_keys(data)
    #     return super().load(data, many=None, partial=None)