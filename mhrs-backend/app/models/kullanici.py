from enum import Enum as PyEnum
from sqlalchemy import Column, Enum, Integer, String
from sqlalchemy.orm import relationship

from app.core.database import Base


class Rol(str, PyEnum):
    ADMIN = "admin"
    DOKTOR = "doktor"
    HASTA = "hasta"


class Kullanici(Base):
    __tablename__ = "kullanicilar"

    id = Column(Integer, primary_key=True, index=True)
    tc_no = Column(String(11), nullable=False, unique=True, index=True)
    sifre_hash = Column(String(255), nullable=False)
    rol = Column(Enum(Rol), nullable=False, default=Rol.HASTA)
    adi_soyadi = Column(String(100), nullable=False)
    telefon = Column(String(15), nullable=True)

    # Relationships
    doktor_profil = relationship("Doktor", back_populates="hesap", uselist=False)
    hasta_profil = relationship("Hasta", back_populates="hesap", uselist=False)
