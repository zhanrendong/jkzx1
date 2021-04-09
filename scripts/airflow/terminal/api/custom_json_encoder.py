import json
from datetime import date, datetime
from terminal.dto import BaseDTO
from decimal import Decimal
import uuid
from enum import Enum
from terminal.utils import DateTimeUtils


class CustomJSONEncoder(json.JSONEncoder):
    """
    JSONEncoder subclass that knows how to encode date/time, decimal types, and
    UUIDs.
    """
    def default(self, o):
        # See "Date Time String Format" in the ECMA-262 specification.
        if isinstance(o, datetime):
            r = o.isoformat()
            if o.microsecond:
                r = r[:23] + r[26:]
            if r.endswith('+00:00'):
                r = r[:-6] + 'Z'
            return r
        elif isinstance(o, date):
            return DateTimeUtils.date2str(o)
        elif isinstance(o, uuid.UUID):
            return str(o)
        elif isinstance(o, Decimal):
            return float(o)
        elif isinstance(o, BaseDTO):
            return o.__dict__
        elif isinstance(o, Enum):
            return o.name
        else:
            return super().default(o)
