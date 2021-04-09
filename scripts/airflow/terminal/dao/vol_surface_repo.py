from terminal.dbo import VolSurface
from sqlalchemy import and_
from terminal.dto import VolSurfaceStrikeType, InstanceType, VolSurfaceSourceType


class VolSurfaceRepo:
    @staticmethod
    def get_instrument_vol_surface(db_session, instrument_id, valuation_date, strike_type, instance, source=None):
        if source:
            vol_surface_dbo = db_session.query(VolSurface).filter(
                and_(
                    VolSurface.instrumentId == instrument_id,
                    VolSurface.valuationDate == valuation_date,
                    VolSurface.strikeType == strike_type,
                    VolSurface.instance == instance,
                    VolSurface.source == source
                )
            ).order_by(VolSurface.updatedAt.desc()).first()
        else:
            vol_surface_dbo = db_session.query(VolSurface).filter(
                and_(
                    VolSurface.instrumentId == instrument_id,
                    VolSurface.valuationDate == valuation_date,
                    VolSurface.strikeType == strike_type,
                    VolSurface.instance == instance
                )
            ).order_by(VolSurface.updatedAt.desc()).first()
        return vol_surface_dbo

    @staticmethod
    def get_latest_vol_surface(db_session, instrument_id, valuation_date, strike_type, instance, source=None):
        if source:
            vol_surface_dbo = db_session.query(VolSurface).filter(
                and_(
                    VolSurface.instrumentId == instrument_id,
                    VolSurface.valuationDate <= valuation_date,
                    VolSurface.strikeType == strike_type,
                    VolSurface.instance == instance,
                    VolSurface.source == source
                )
            ).order_by(VolSurface.updatedAt.desc()).first()
        else:
            vol_surface_dbo = db_session.query(VolSurface).filter(
                and_(
                    VolSurface.instrumentId == instrument_id,
                    VolSurface.valuationDate <= valuation_date,
                    VolSurface.strikeType == strike_type,
                    VolSurface.instance == instance
                )
            ).order_by(VolSurface.updatedAt.desc()).first()
        return vol_surface_dbo

    @staticmethod
    def insert_vol_surface(db_session, vol_surface_dbo):
        db_session.query(VolSurface).filter(VolSurface.valuationDate == vol_surface_dbo.valuationDate,
                                            VolSurface.instance == vol_surface_dbo.instance,
                                            VolSurface.strikeType == vol_surface_dbo.strikeType,
                                            VolSurface.instrumentId == vol_surface_dbo.instrumentId)\
            .delete(synchronize_session=False)
        db_session.flush()
        db_session.add(vol_surface_dbo)
        db_session.flush()
        db_session.commit()
        return vol_surface_dbo

    @staticmethod
    def insert_vol_surfaces(db_session, vol_surface_dbos):
        db_session.add_all(vol_surface_dbos)
        db_session.flush()
        db_session.commit()
        return vol_surface_dbos

    @staticmethod
    def get_vol_surface_by_observed_date(db_session, observed_date):
        return db_session.query(VolSurface).filter(
            VolSurface.valuationDate == observed_date
        ).all()

