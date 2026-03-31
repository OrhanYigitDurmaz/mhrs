from pydantic import BaseModel, Field


class LoginRequest(BaseModel):
    tc_no: str = Field(..., min_length=11, max_length=11)
    sifre: str


class RegisterRequest(BaseModel):
    tc_no: str = Field(..., min_length=11, max_length=11)
    adi_soyadi: str = Field(..., min_length=3)
    telefon: str | None = None
    sifre: str = Field(..., min_length=6)


class UserCreateRequest(BaseModel):
    tc_no: str = Field(..., min_length=11, max_length=11)
    adi_soyadi: str = Field(..., min_length=3)
    telefon: str | None = None
    sifre: str = Field(..., min_length=6)
    rol: str = Field(..., pattern="^(admin|doktor|hasta)$")


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
