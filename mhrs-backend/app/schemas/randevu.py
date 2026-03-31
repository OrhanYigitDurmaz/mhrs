from datetime import date
from pydantic import BaseModel


class RandevuBase(BaseModel):
    doktor_id: int
    tarih: date
    saat: str


class RandevuCreate(RandevuBase):
    pass


class Randevu(RandevuBase):
    id: int
    hasta_id: int
    durum: str

    class Config:
        from_attributes = True


class RandevuSlot(BaseModel):
    tarih: date
    saat: str
    musait: bool
