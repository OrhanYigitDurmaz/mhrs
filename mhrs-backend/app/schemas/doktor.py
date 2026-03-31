from pydantic import BaseModel


class DoktorBase(BaseModel):
    hastane_id: int
    brans_id: int


class DoktorCreate(DoktorBase):
    kullanici_id: int


class Doktor(DoktorBase):
    id: int
    kullanici_id: int

    class Config:
        from_attributes = True


class DoktorWithUser(Doktor):
    """Doktor with user account information"""
    tc_no: str
    adi_soyadi: str
    telefon: str | None = None
