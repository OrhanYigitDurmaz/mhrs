from datetime import date
from pydantic import BaseModel


class RandevuSlotBase(BaseModel):
    doktor_id: int
    tarih: date
    saat: str  # HH:MM format


class RandevuSlotCreate(RandevuSlotBase):
    pass


class RandevuSlot(RandevuSlotBase):
    id: int
    dolu: int

    class Config:
        from_attributes = True
