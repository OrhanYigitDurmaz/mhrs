from pydantic import BaseModel, Field


class HastaBase(BaseModel):
    adi_soyadi: str
    telefon: str | None = None


class HastaCreate(HastaBase):
    tc_no: str = Field(..., min_length=11, max_length=11)
    sifre: str = Field(..., min_length=6)


class Hasta(HastaBase):
    id: int
    tc_no: str

    class Config:
        from_attributes = True
