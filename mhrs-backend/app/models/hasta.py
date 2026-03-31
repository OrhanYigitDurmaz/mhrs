from sqlalchemy import Column, ForeignKey, Integer, String
from sqlalchemy.orm import relationship

from app.core.database import Base


class Hasta(Base):
    __tablename__ = "hastalar"

    id = Column(Integer, primary_key=True, index=True)
    kullanici_id = Column(Integer, ForeignKey("kullanicilar.id"), nullable=False, unique=True)

    # Patient-specific fields can be added here
    # e.g., blood_type, allergies, emergency_contact, etc.

    # Relationship
    hesap = relationship("Kullanici", back_populates="hasta_profil")
