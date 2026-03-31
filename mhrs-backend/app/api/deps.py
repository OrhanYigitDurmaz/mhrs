from collections.abc import Callable
from typing import Annotated

from fastapi import Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.exceptions import ForbiddenException, UnauthorizedException
from app.core.security import decode_access_token
from app.models.kullanici import Kullanici, Rol

security = HTTPBearer()


def get_current_user(
    credentials: Annotated[HTTPAuthorizationCredentials, Depends(security)],
    db: Session = Depends(get_db),
) -> Kullanici:
    token = credentials.credentials
    payload = decode_access_token(token)
    if payload is None:
        raise UnauthorizedException("Geçersiz token")
    user_id = payload.get("sub")
    if user_id is None:
        raise UnauthorizedException("Geçersiz token")
    user = db.query(Kullanici).filter(Kullanici.id == int(user_id)).first()
    if user is None:
        raise UnauthorizedException("Kullanıcı bulunamadı")
    return user


def require_role(*roles: Rol) -> Callable[[Kullanici], Kullanici]:
    """Rol bazlı yetkilendirme decorator'ı"""

    def role_checker(current_user: Kullanici = Depends(get_current_user)) -> Kullanici:
        if current_user.rol not in roles:
            raise ForbiddenException("Bu işlem için yetkiniz yok")
        return current_user

    return role_checker


# Shorthand helpers
require_admin = require_role(Rol.ADMIN)
require_doctor = require_role(Rol.DOKTOR, Rol.ADMIN)
require_user = require_role(Rol.HASTA, Rol.DOKTOR, Rol.ADMIN)  # All authenticated users
