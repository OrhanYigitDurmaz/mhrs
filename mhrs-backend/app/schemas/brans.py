from pydantic import BaseModel


class BransBase(BaseModel):
    adi: str


class BransCreate(BransBase):
    pass


class Brans(BransBase):
    id: int

    class Config:
        from_attributes = True
