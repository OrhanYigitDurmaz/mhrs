from sqlalchemy import Column, ForeignKey, Integer, String, Time
from sqlalchemy.orm import relationship

from app.core.database import Base


class CalismaSaati(Base):
    __tablename__ = "calisma_saati"

    id = Column(Integer, primary_key=True, index=True)
    doktor_id = Column(Integer, ForeignKey("doktorlar.id"), nullable=False)
    gun = Column(String(20), nullable=False)  # Pazartesi, Salı, ...
    saat_bas = Column(Time, nullable=False)
    saat_bit = Column(Time, nullable=False)

    doktor = relationship("Doktor", backref="calisma_saati")
