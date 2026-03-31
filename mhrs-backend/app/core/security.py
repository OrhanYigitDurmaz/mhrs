from datetime import datetime, timedelta
from typing import Optional

import bcrypt
from jose import JWTError, jwt

from app.core.config import settings


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(plain_password.encode("utf-8"), hashed_password.encode("utf-8"))


def get_password_hash(password: str) -> str:
    salt = bcrypt.gensalt()
    return bcrypt.hashpw(password.encode("utf-8"), salt).decode("utf-8")


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt


def decode_access_token(token: str) -> Optional[dict]:
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        return payload
    except JWTError:
        return None


def validate_tc_no(tc_no: str) -> bool:
    """TC Kimlik No algoritması ile validasyon"""
    if not tc_no or not tc_no.isdigit() or len(tc_no) != 11:
        return False

    if tc_no[0] == "0":
        return False

    # 1, 3, 5, 7, 9. basamakların toplamı
    odd_sum = sum(int(tc_no[i]) for i in range(0, 9, 2))
    # 2, 4, 6, 8. basamakların toplamı
    even_sum = sum(int(tc_no[i]) for i in range(1, 9, 2))

    # 10. basamak kontrolü
    tenth_digit = (odd_sum * 7 - even_sum) % 10
    if int(tc_no[9]) != tenth_digit:
        return False

    # 11. basamak kontrolü
    total_sum = sum(int(tc_no[i]) for i in range(10))
    if int(tc_no[10]) != total_sum % 10:
        return False

    return True
