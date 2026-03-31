from pydantic import BaseModel


class IlBase(BaseModel):
    adi: str


class IlCreate(IlBase):
    pass


class Il(IlBase):
    id: int

    class Config:
        from_attributes = True
