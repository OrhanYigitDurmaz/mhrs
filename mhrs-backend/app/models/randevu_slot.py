from sqlalchemy import Column, Date, ForeignKey, Integer, Time
from sqlalchemy.orm import relationship

from app.core.database import Base


class RandevuSlot(Base):
    __tablename__ = "randevu_slot"

    id = Column(Integer, primary_key=True, index=True)
    doktor_id = Column(Integer, ForeignKey("doktorlar.id"), nullable=False)
    tarih = Column(Date, nullable=False, index=True)
    saat = Column(Time, nullable=False)
    dolu = Column(Integer, default=0)  # 0: müsait, 1: dolu

    doktor = relationship("Doktor", backref="randevu_slot")
