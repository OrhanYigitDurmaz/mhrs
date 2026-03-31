from datetime import time
from pydantic import BaseModel, field_serializer


class CalismaSaatiBase(BaseModel):
    doktor_id: int
    gun: str  # Pazartesi, Salı, Çarşamba, Perşembe, Cuma, Cumartesi, Pazar


class CalismaSaatiCreate(CalismaSaatiBase):
    saat_bas: str  # HH:MM format
    saat_bit: str  # HH:MM format


class CalismaSaati(CalismaSaatiBase):
    id: int
    saat_bas: time
    saat_bit: time

    @field_serializer('saat_bas', 'saat_bit')
    def serialize_time(self, value: time) -> str:
        return value.strftime('%H:%M')

    class Config:
        from_attributes = True
