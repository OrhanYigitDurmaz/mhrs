from pydantic import BaseModel


class HastaneBase(BaseModel):
    il_id: int
    ilce_id: int
    adi: str
    tip: str
    adres: str | None = None


class HastaneCreate(HastaneBase):
    pass


class Hastane(HastaneBase):
    id: int

    class Config:
        from_attributes = True
