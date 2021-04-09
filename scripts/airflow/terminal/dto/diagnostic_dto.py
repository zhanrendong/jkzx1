from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load
from enum import Enum
from .base_schema import BaseSchema


class DiagnosticType(Enum):
    ERROR = 0
    WARNING = 1
    INFO = 2


class DiagnosticDTO(BaseDTO):
    def __init__(self, type=None, message=None, details=None):
        # self.code = None
        self.type = type
        self.message = message
        self.details = [] if details is None else details


class DiagnosticSchema(BaseSchema):
    type = fields.Str()
    message = fields.Str()
    detail = fields.Str()

    @post_load
    def make_diagnostic(self, dic):
        return DiagnosticDTO(**dic)


class DiagnosticResponse(BaseDTO):
    def __init__(self, data=None, diagnostics=None):
        self.data = data
        self.diagnostics = [] if diagnostics is None else diagnostics




