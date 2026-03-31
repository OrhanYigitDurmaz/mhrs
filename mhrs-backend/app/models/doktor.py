from sqlalchemy import Column, ForeignKey, Integer
from sqlalchemy.orm import relationship

from app.core.database import Base


class Doktor(Base):
    __tablename__ = "doktorlar"

    id = Column(Integer, primary_key=True, index=True)
    kullanici_id = Column(Integer, ForeignKey("kullanicilar.id"), nullable=False, unique=True)
    hastane_id = Column(Integer, ForeignKey("hastaneler.id"), nullable=False)
    brans_id = Column(Integer, ForeignKey("branşlar.id"), nullable=False)

    # Relationships
    hesap = relationship("Kullanici", back_populates="doktor_profil")
    hastane = relationship("Hastane", back_populates="doktorlar")
    brans = relationship("Brans", backref="doktorlar")
