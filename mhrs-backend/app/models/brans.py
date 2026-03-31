from sqlalchemy import Column, Integer, String
from app.core.database import Base


class Brans(Base):
    __tablename__ = "branşlar"

    id = Column(Integer, primary_key=True, index=True)
    adi = Column(String(100), nullable=False, unique=True)
