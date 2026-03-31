from datetime import timedelta
from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, require_admin, require_role
from app.core.config import settings
from app.core.exceptions import BadRequestException, NotFoundException
from app.core.security import create_access_token, get_password_hash, validate_tc_no, verify_password
from app.models.hasta import Hasta
from app.models.kullanici import Kullanici, Rol
from app.schemas.auth import LoginRequest, RegisterRequest, TokenResponse, UserCreateRequest
from app.core.database import get_db
from app.core.exceptions import ErrorCode

router = APIRouter(prefix="/auth", tags=["Auth"])


@router.post("/register", response_model=TokenResponse)
def register(request: RegisterRequest, db: Session = Depends(get_db)):
    """Normal kullanıcı kaydı - sadece HASTA rolü"""
    # TC kimlik no validasyonu
    if not validate_tc_no(request.tc_no):
        raise BadRequestException("Geçersiz TC Kimlik No", ErrorCode.INVALID_TC_NO)

    # Zaten kayıtlı mı?
    existing_user = db.query(Kullanici).filter(Kullanici.tc_no == request.tc_no).first()
    if existing_user:
        raise BadRequestException("Bu TC Kimlik No ile zaten kayıtlı kullanıcı var", ErrorCode.USER_ALREADY_EXISTS)

    # Yeni kullanıcı oluştur - always HASTA role for public registration
    kullanici = Kullanici(
        tc_no=request.tc_no,
        adi_soyadi=request.adi_soyadi,
        telefon=request.telefon,
        sifre_hash=get_password_hash(request.sifre),
        rol=Rol.HASTA,
    )
    db.add(kullanici)
    db.commit()
    db.refresh(kullanici)

    # Hasta profili oluştur
    hasta = Hasta(kullanici_id=kullanici.id)
    db.add(hasta)
    db.commit()

    # Token oluştur
    access_token = create_access_token(
        data={"sub": str(kullanici.id)},
        expires_delta=timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES),
    )

    return TokenResponse(access_token=access_token)


@router.post("/admin/create-user", response_model=TokenResponse)
def create_user(
    request: UserCreateRequest,
    current_admin: Annotated[Kullanici, Depends(require_admin)],
    db: Session = Depends(get_db),
):
    """Admin only - herhangi rolde kullanıcı oluşturur"""
    # TC kimlik no validasyonu
    if not validate_tc_no(request.tc_no):
        raise BadRequestException("Geçersiz TC Kimlik No", ErrorCode.INVALID_TC_NO)

    # Zaten kayıtlı mı?
    existing_user = db.query(Kullanici).filter(Kullanici.tc_no == request.tc_no).first()
    if existing_user:
        raise BadRequestException("Bu TC Kimlik No ile zaten kayıtlı kullanıcı var", ErrorCode.USER_ALREADY_EXISTS)

    # Role validation
    try:
        user_rol = Rol(request.rol)
    except ValueError:
        raise BadRequestException("Geçersiz rol. Seçenekler: admin, doktor, hasta", ErrorCode.INVALID_ROLE)

    # Yeni kullanıcı oluştur
    kullanici = Kullanici(
        tc_no=request.tc_no,
        adi_soyadi=request.adi_soyadi,
        telefon=request.telefon,
        sifre_hash=get_password_hash(request.sifre),
        rol=user_rol,
    )
    db.add(kullanici)
    db.commit()
    db.refresh(kullanici)

    # Role göre profil oluştur
    if user_rol == Rol.HASTA:
        hasta = Hasta(kullanici_id=kullanici.id)
        db.add(hasta)
        db.commit()

    # Token oluştur
    access_token = create_access_token(
        data={"sub": str(kullanici.id)},
        expires_delta=timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES),
    )

    return TokenResponse(access_token=access_token)


@router.post("/login", response_model=TokenResponse)
def login(request: LoginRequest, db: Session = Depends(get_db)):
    kullanici = db.query(Kullanici).filter(Kullanici.tc_no == request.tc_no).first()

    if not kullanici:
        raise BadRequestException("Geçersiz TC Kimlik No veya şifre", ErrorCode.INVALID_CREDENTIALS)

    # Şifre kontrolü (hash kontrolü)
    if not verify_password(request.sifre, kullanici.sifre_hash):
        raise BadRequestException("Geçersiz TC Kimlik No veya şifre", ErrorCode.INVALID_CREDENTIALS)

    access_token = create_access_token(
        data={"sub": str(kullanici.id)},
        expires_delta=timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES),
    )

    return TokenResponse(access_token=access_token)


@router.get("/me")
def get_me(current_user: Kullanici = Depends(get_current_user), db: Session = Depends(get_db)):
    response_data = {
        "id": current_user.id,
        "tc_no": current_user.tc_no,
        "adi_soyadi": current_user.adi_soyadi,
        "telefon": current_user.telefon,
        "rol": current_user.rol,
    }

    # For doctors, include the doctor record ID
    if current_user.rol == Rol.DOKTOR:
        from app.models.doktor import Doktor
        doktor = db.query(Doktor).filter(Doktor.kullanici_id == current_user.id).first()
        if doktor:
            response_data["doktor_id"] = doktor.id

    return {
        "success": True,
        "data": response_data,
    }
