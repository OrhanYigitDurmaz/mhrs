from datetime import date
from typing import Annotated, Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.api.deps import require_admin
from app.core.database import get_db
from app.core.exceptions import NotFoundException
from app.models.doktor import Doktor
from app.models.kullanici import Kullanici
from app.models.randevu_slot import RandevuSlot
from app.schemas.doktor import Doktor as DoktorSchema, DoktorCreate, DoktorWithUser
from app.schemas.randevu import RandevuSlot as RandevuSlotSchema

router = APIRouter(prefix="/doktorlar", tags=["Doktorlar"])


@router.get("", response_model=list[DoktorWithUser])
def list_doktorlar(
    hastane: Optional[int] = None,
    brans: Optional[int] = None,
    db: Session = Depends(get_db),
):
    """Doktorları filtreleyerek listeler"""
    query = db.query(Doktor)

    if hastane is not None:
        query = query.filter(Doktor.hastane_id == hastane)
    if brans is not None:
        query = query.filter(Doktor.brans_id == brans)

    doktorlar = query.all()

    # Join with Kullanici to get user info
    result = []
    for doktor in doktorlar:
        kullanici = db.query(Kullanici).filter(Kullanici.id == doktor.kullanici_id).first()
        if kullanici:
            result.append(DoktorWithUser(
                id=doktor.id,
                kullanici_id=doktor.kullanici_id,
                hastane_id=doktor.hastane_id,
                brans_id=doktor.brans_id,
                tc_no=kullanici.tc_no,
                adi_soyadi=kullanici.adi_soyadi,
                telefon=kullanici.telefon,
            ))
    return result


@router.get("/{doktor_id}", response_model=DoktorWithUser)
def get_doktor(doktor_id: int, db: Session = Depends(get_db)):
    """Doktor detayını getirir"""
    doktor = db.query(Doktor).filter(Doktor.id == doktor_id).first()
    if not doktor:
        raise NotFoundException("Doktor bulunamadı")

    kullanici = db.query(Kullanici).filter(Kullanici.id == doktor.kullanici_id).first()
    if not kullanici:
        raise NotFoundException("Doktorun kullanıcısı bulunamadı")

    return DoktorWithUser(
        id=doktor.id,
        kullanici_id=doktor.kullanici_id,
        hastane_id=doktor.hastane_id,
        brans_id=doktor.brans_id,
        tc_no=kullanici.tc_no,
        adi_soyadi=kullanici.adi_soyadi,
        telefon=kullanici.telefon,
    )


@router.post("", response_model=DoktorSchema)
def create_doktor(
    doktor: DoktorCreate,
    _: Annotated[Kullanici, Depends(require_admin)] = None,
    db: Session = Depends(get_db),
):
    """Yeni doktor profili oluşturur (kullanıcı hesabı /auth/admin/create-user ile oluşturulmalı)"""
    # Check if the user exists
    kullanici = db.query(Kullanici).filter(Kullanici.id == doktor.kullanici_id).first()
    if not kullanici:
        raise NotFoundException("Kullanıcı bulunamadı")

    # Check if user is a doctor
    if kullanici.rol.value != "doktor":
        raise NotFoundException("Kullanıcının rolü doktor değil")

    # Check if doctor profile already exists
    existing_doktor = db.query(Doktor).filter(Doktor.kullanici_id == doktor.kullanici_id).first()
    if existing_doktor:
        raise NotFoundException("Bu kullanıcı için zaten doktor profili mevcut")

    db_doktor = Doktor(
        kullanici_id=doktor.kullanici_id,
        hastane_id=doktor.hastane_id,
        brans_id=doktor.brans_id,
    )
    db.add(db_doktor)
    db.commit()
    db.refresh(db_doktor)
    return db_doktor


@router.delete("/{doktor_id}")
def delete_doktor(doktor_id: int, _: Annotated[Kullanici, Depends(require_admin)] = None, db: Session = Depends(get_db)):
    """Doktor siler"""
    doktor = db.query(Doktor).filter(Doktor.id == doktor_id).first()
    if not doktor:
        raise NotFoundException("Doktor bulunamadı")
    db.delete(doktor)
    db.commit()
    return {"success": True, "message": "Doktor silindi"}


@router.get("/{doktor_id}/musaitlik", response_model=list[RandevuSlotSchema])
def get_musaitlik(
    doktor_id: int,
    baslangic: date = Query(..., description="Başlangıç tarihi (YYYY-MM-DD)"),
    bitis: date = Query(..., description="Bitiş tarihi (YYYY-MM-DD)"),
    db: Session = Depends(get_db),
):
    """Doktorun müsait randevu saatlerini listeler"""
    doktor = db.query(Doktor).filter(Doktor.id == doktor_id).first()
    if not doktor:
        raise NotFoundException("Doktor bulunamadı")

    # Randevu slotlarını getir
    slotlar = db.query(RandevuSlot).filter(
        RandevuSlot.doktor_id == doktor_id,
        RandevuSlot.tarih >= baslangic,
        RandevuSlot.tarih <= bitis,
    ).order_by(RandevuSlot.tarih, RandevuSlot.saat).all()

    return [
        RandevuSlotSchema(
            tarih=slot.tarih,
            saat=slot.saat.strftime("%H:%M"),
            musait=slot.dolu == 0,
        )
        for slot in slotlar
    ]
