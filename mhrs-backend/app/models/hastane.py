from sqlalchemy import Column, ForeignKey, Integer, String, Text
from sqlalchemy.orm import relationship

from app.core.database import Base


class Hastane(Base):
    __tablename__ = "hastaneler"

    id = Column(Integer, primary_key=True, index=True)
    il_id = Column(Integer, ForeignKey("iller.id"), nullable=False)
    ilce_id = Column(Integer, ForeignKey("ilceler.id"), nullable=False)
    adi = Column(String(200), nullable=False)
    tip = Column(String(50), nullable=False)  # Devlet, Özel, Üniversite
    adres = Column(Text, nullable=True)

    il = relationship("Il", backref="hastaneler")
    ilce = relationship("Ilce", backref="hastaneler")
    doktorlar = relationship("Doktor", back_populates="hastane")
