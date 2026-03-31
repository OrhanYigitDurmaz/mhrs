from sqlalchemy import Column, Integer, String
from app.core.database import Base


class Il(Base):
    __tablename__ = "iller"

    id = Column(Integer, primary_key=True, index=True)
    adi = Column(String(50), nullable=False, unique=True)
