from datetime import datetime
from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.core.database import get_db
from app.core.exceptions import BadRequestException, NotFoundException
from app.models.hasta import Hasta
from app.models.kullanici import Kullanici
from app.models.randevu import Randevu
from app.models.randevu_slot import RandevuSlot
from app.schemas.randevu import Randevu as RandevuSchema, RandevuCreate
from app.core.exceptions import ErrorCode

router = APIRouter(prefix="/randevular", tags=["Randevular"])


@router.post("", response_model=RandevuSchema, status_code=201)
def create_randevu(
    randevu_data: RandevuCreate,
    current_user: Annotated[Kullanici, Depends(get_current_user)],
    db: Session = Depends(get_db),
):
    """Yeni randevu oluşturur"""

    # Get hasta profile for current user
    hasta = db.query(Hasta).filter(Hasta.kullanici_id == current_user.id).first()
    if not hasta:
        raise BadRequestException("Hasta profili bulunamadı", ErrorCode.NOT_FOUND)

    # Slot müsait mi kontrol et
    saat_obj = datetime.strptime(randevu_data.saat, "%H:%M").time()
    slot = db.query(RandevuSlot).filter(
        RandevuSlot.doktor_id == randevu_data.doktor_id,
        RandevuSlot.tarih == randevu_data.tarih,
        RandevuSlot.saat == saat_obj,
    ).first()

    if not slot:
        raise BadRequestException("Randevu saati bulunamadı", ErrorCode.NOT_FOUND)

    if slot.dolu == 1:
        raise BadRequestException("Bu randevu saati dolu", ErrorCode.SLOT_NOT_AVAILABLE)

    # Aynı hasta aynı saatte başka randevu var mı?
    existing = db.query(Randevu).filter(
        Randevu.hasta_id == hasta.id,
        Randevu.tarih == randevu_data.tarih,
        Randevu.saat == saat_obj,
        Randevu.durum == "aktif",
    ).first()

    if existing:
        raise BadRequestException("Bu tarih ve saatte zaten aktif randevunuz var", ErrorCode.APPOINTMENT_EXISTS)

    # Randevu oluştur
    randevu = Randevu(
        hasta_id=hasta.id,
        doktor_id=randevu_data.doktor_id,
        tarih=randevu_data.tarih,
        saat=saat_obj,
        durum="aktif",
    )
    db.add(randevu)

    # Slotu dolu işaretle
    slot.dolu = 1

    db.commit()
    db.refresh(randevu)
    return randevu


@router.get("", response_model=list[RandevuSchema])
def list_randevular(
    current_user: Annotated[Kullanici, Depends(get_current_user)],
    db: Session = Depends(get_db),
):
    """Kullanıcının randevularını listeler"""
    # Get hasta profile for current user
    hasta = db.query(Hasta).filter(Hasta.kullanici_id == current_user.id).first()
    if not hasta:
        return []

    return (
        db.query(Randevu)
        .filter(Randevu.hasta_id == hasta.id)
        .order_by(Randevu.tarih.desc(), Randevu.saat.desc())
        .all()
    )


@router.delete("/{randevu_id}")
def cancel_randevu(
    randevu_id: int,
    current_user: Annotated[Kullanici, Depends(get_current_user)],
    db: Session = Depends(get_db),
):
    """Randevuyu iptal eder"""
    # Get hasta profile for current user
    hasta = db.query(Hasta).filter(Hasta.kullanici_id == current_user.id).first()
    if not hasta:
        raise NotFoundException("Hasta profili bulunamadı")

    randevu = (
        db.query(Randevu)
        .filter(
            Randevu.id == randevu_id,
            Randevu.hasta_id == hasta.id,
        )
        .first()
    )

    if not randevu:
        raise NotFoundException("Randevu bulunamadı")

    if randevu.durum != "aktif":
        raise BadRequestException("Bu randevu zaten iptal edilmiş veya tamamlanmış", ErrorCode.APPOINTMENT_ALREADY_CANCELLED)

    # Randevuyu iptal et
    randevu.durum = "iptal"

    # Slotu müsait yap
    slot = db.query(RandevuSlot).filter(
        RandevuSlot.doktor_id == randevu.doktor_id,
        RandevuSlot.tarih == randevu.tarih,
        RandevuSlot.saat == randevu.saat,
    ).first()
    if slot:
        slot.dolu = 0

    db.commit()
    return {"success": True, "message": "Randevu iptal edildi"}
