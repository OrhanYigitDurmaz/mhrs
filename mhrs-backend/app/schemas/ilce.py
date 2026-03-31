from pydantic import BaseModel


class IlceBase(BaseModel):
    il_id: int
    adi: str


class IlceCreate(IlceBase):
    pass


class Ilce(IlceBase):
    id: int

    class Config:
        from_attributes = True
