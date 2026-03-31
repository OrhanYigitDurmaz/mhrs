from sqlalchemy import Column, Date, ForeignKey, Integer, String, Time
from sqlalchemy.orm import relationship

from app.core.database import Base


class Randevu(Base):
    __tablename__ = "randevular"

    id = Column(Integer, primary_key=True, index=True)
    hasta_id = Column(Integer, ForeignKey("hastalar.id"), nullable=False)
    doktor_id = Column(Integer, ForeignKey("doktorlar.id"), nullable=False)
    tarih = Column(Date, nullable=False, index=True)
    saat = Column(Time, nullable=False)
    durum = Column(String(20), default="aktif")  # aktif, iptal, tamamlandı

    hasta = relationship("Hasta", backref="randevular")
    doktor = relationship("Doktor", backref="randevular")
