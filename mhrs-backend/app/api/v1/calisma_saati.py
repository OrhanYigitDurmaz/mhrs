from typing import Annotated

from datetime import datetime, time

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import require_doctor
from app.core.database import get_db
from app.core.exceptions import BadRequestException, NotFoundException
from app.models.calisma_saati import CalismaSaati
from app.models.doktor import Doktor
from app.models.kullanici import Kullanici
from app.schemas.calisma_saati import CalismaSaati as CalismaSaatiSchema, CalismaSaatiCreate
from app.core.exceptions import ErrorCode

router = APIRouter(prefix="/calisma-saati", tags=["Çalışma Saati"])


def _can_manage_hours(user: Kullanici, doktor_id: int, db: Session) -> bool:
    """Kullanıcı doktorun çalışma saatlerini yönetebilir mi?"""
    if user.rol.value == "admin":
        return True
    if user.rol.value == "doktor":
        # Find this user's doctor profile
        doktor = db.query(Doktor).filter(Doktor.kullanici_id == user.id).first()
        # Compare the doctor table ID
        return doktor and doktor.id == doktor_id
    return False


@router.get("", response_model=list[CalismaSaatiSchema])
def list_calisma_saati(current_user: Annotated[Kullanici, Depends(require_doctor)] = None, db: Session = Depends(get_db)):
    """Tüm çalışma saatlerini listeler (admin/doktor)"""
    return db.query(CalismaSaati).all()


@router.get("/doktor/{doktor_id}", response_model=list[CalismaSaatiSchema])
def get_doktor_calisma_saati(doktor_id: int, db: Session = Depends(get_db)):
    """Doktorun çalışma saatlerini listeler"""
    return db.query(CalismaSaati).filter(CalismaSaati.doktor_id == doktor_id).all()


@router.post("", response_model=CalismaSaatiSchema)
def create_calisma_saati(
    saat: CalismaSaatiCreate,
    current_user: Annotated[Kullanici, Depends(require_doctor)],
    db: Session = Depends(get_db),
):
    """Yeni çalışma saati oluşturur (admin veya doktor kendisi için)"""
    if not _can_manage_hours(current_user, saat.doktor_id, db):
        raise BadRequestException("Sadece kendi çalışma saatlerinizi oluşturabilirsiniz", ErrorCode.FORBIDDEN)

    # Parse time strings to time objects
    saat_bas_obj = datetime.strptime(saat.saat_bas, "%H:%M").time()
    saat_bit_obj = datetime.strptime(saat.saat_bit, "%H:%M").time()

    db_saati = CalismaSaati(
        doktor_id=saat.doktor_id,
        gun=saat.gun,
        saat_bas=saat_bas_obj,
        saat_bit=saat_bit_obj,
    )
    db.add(db_saati)
    db.commit()
    db.refresh(db_saati)
    return db_saati


@router.delete("/{saat_id}")
def delete_calisma_saati(
    saat_id: int,
    current_user: Annotated[Kullanici, Depends(require_doctor)],
    db: Session = Depends(get_db),
):
    """Çalışma saatini siler (admin veya doktor kendisi için)"""
    saat = db.query(CalismaSaati).filter(CalismaSaati.id == saat_id).first()
    if not saat:
        raise NotFoundException("Çalışma saati bulunamadı")

    if not _can_manage_hours(current_user, saat.doktor_id, db):
        raise BadRequestException("Sadece kendi çalışma saatlerinizi silebilirsiniz", ErrorCode.FORBIDDEN)

    db.delete(saat)
    db.commit()
    return {"success": True, "message": "Çalışma saati silindi"}
