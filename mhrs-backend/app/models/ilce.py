from sqlalchemy import Column, ForeignKey, Integer, String
from sqlalchemy.orm import relationship

from app.core.database import Base


class Ilce(Base):
    __tablename__ = "ilceler"

    id = Column(Integer, primary_key=True, index=True)
    il_id = Column(Integer, ForeignKey("iller.id"), nullable=False)
    adi = Column(String(50), nullable=False)

    il = relationship("Il", backref="ilceler")
