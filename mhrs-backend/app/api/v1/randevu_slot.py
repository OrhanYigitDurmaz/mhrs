from datetime import date, datetime, timedelta
from typing import Annotated, Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.api.deps import require_doctor
from app.core.database import get_db
from app.core.exceptions import BadRequestException, NotFoundException
from app.models.calisma_saati import CalismaSaati
from app.models.doktor import Doktor
from app.models.kullanici import Kullanici
from app.models.randevu_slot import RandevuSlot
from app.schemas.randevu_slot import RandevuSlot as RandevuSlotSchema, RandevuSlotCreate
from app.core.exceptions import ErrorCode

router = APIRouter(prefix="/randevu-slot", tags=["Randevu Slot"])


GUN_MAP = {
    "Monday": "Pazartesi",
    "Tuesday": "Salı",
    "Wednesday": "Çarşamba",
    "Thursday": "Perşembe",
    "Friday": "Cuma",
    "Saturday": "Cumartesi",
    "Sunday": "Pazar",
}


def _can_manage_slots(user: Kullanici, doktor_id: int, db: Session) -> bool:
    """Kullanıcı doktorun randevu slotlarını yönetebilir mi?"""
    if user.rol.value == "admin":
        return True
    if user.rol.value == "doktor":
        # Find this user's doctor profile
        doktor = db.query(Doktor).filter(Doktor.kullanici_id == user.id).first()
        # Compare the doctor table ID
        return doktor and doktor.id == doktor_id
    return False


@router.get("/doktor/{doktor_id}", response_model=list[RandevuSlotSchema])
def get_doktor_slots(
    doktor_id: int,
    baslangic: Optional[date] = None,
    bitis: Optional[date] = None,
    db: Session = Depends(get_db),
):
    """Doktorun randevu slotlarını listeler"""
    query = db.query(RandevuSlot).filter(RandevuSlot.doktor_id == doktor_id)

    if baslangic:
        query = query.filter(RandevuSlot.tarih >= baslangic)
    if bitis:
        query = query.filter(RandevuSlot.tarih <= bitis)

    return query.order_by(RandevuSlot.tarih, RandevuSlot.saat).all()


@router.post("", response_model=RandevuSlotSchema)
def create_slot(
    slot: RandevuSlotCreate,
    current_user: Annotated[Kullanici, Depends(require_doctor)],
    db: Session = Depends(get_db),
):
    """Manuel randevu slotu oluşturur"""
    if not _can_manage_slots(current_user, slot.doktor_id, db):
        raise BadRequestException("Sadece kendi randevu slotlarınızı oluşturabilirsiniz", ErrorCode.FORBIDDEN)

    saat_obj = datetime.strptime(slot.saat, "%H:%M").time()

    db_slot = RandevuSlot(
        doktor_id=slot.doktor_id,
        tarih=slot.tarih,
        saat=saat_obj,
        dolu=0,
    )
    db.add(db_slot)
    db.commit()
    db.refresh(db_slot)
    return db_slot


@router.delete("/{slot_id}")
def delete_slot(
    slot_id: int,
    current_user: Annotated[Kullanici, Depends(require_doctor)],
    db: Session = Depends(get_db),
):
    """Randevu slotunu siler"""
    slot = db.query(RandevuSlot).filter(RandevuSlot.id == slot_id).first()
    if not slot:
        raise NotFoundException("Slot bulunamadı")

    if not _can_manage_slots(current_user, slot.doktor_id, db):
        raise BadRequestException("Sadece kendi randevu slotlarınızı silebilirsiniz", ErrorCode.FORBIDDEN)

    db.delete(slot)
    db.commit()
    return {"success": True, "message": "Slot silindi"}


@router.post("/doktor/{doktor_id}/generate")
def generate_slots(
    doktor_id: int,
    baslangic: date = Query(..., description="Başlangıç tarihi (YYYY-MM-DD)"),
    bitis: date = Query(..., description="Bitiş tarihi (YYYY-MM-DD)"),
    slot_suresi: int = Query(30, description="Slot süresi (dakika)"),
    current_user: Annotated[Kullanici, Depends(require_doctor)] = None,
    db: Session = Depends(get_db),
):
    """
    Doktorun çalışma saatlerine göre randevu slotları oluşturur.
    Var olan slotları silip yeniden oluşturur.
    """
    if not _can_manage_slots(current_user, doktor_id, db):
        raise BadRequestException("Sadece kendi randevu slotlarınızı oluşturabilirsiniz", ErrorCode.FORBIDDEN)

    # Doktor var mı?
    doktor = db.query(Doktor).filter(Doktor.id == doktor_id).first()
    if not doktor:
        raise NotFoundException("Doktor bulunamadı")

    # Çalışma saatlerini getir
    calisma_saati = db.query(CalismaSaati).filter(CalismaSaati.doktor_id == doktor_id).all()
    if not calisma_saati:
        raise BadRequestException("Doktorun çalışma saati tanımlı değil", ErrorCode.NO_WORKING_HOURS)

    # Var olan slotları sil (tarih aralığındakileri)
    db.query(RandevuSlot).filter(
        RandevuSlot.doktor_id == doktor_id,
        RandevuSlot.tarih >= baslangic,
        RandevuSlot.tarih <= bitis,
    ).delete()

    # Yeni slotları oluştur
    gunluk_calisma = {}
    for cs in calisma_saati:
        if cs.gun not in gunluk_calisma:
            gunluk_calisma[cs.gun] = []
        gunluk_calisma[cs.gun].append((cs.saat_bas, cs.saat_bit))

    current_date = baslangic
    slot_count = 0

    while current_date <= bitis:
        gun_adi = current_date.strftime("%A")
        gun_tr = GUN_MAP.get(gun_adi)

        if gun_tr in gunluk_calisma:
            for saat_bas, saat_bit in gunluk_calisma[gun_tr]:
                # Saatleri dakikaya çevir
                bas_dakika = saat_bas.hour * 60 + saat_bas.minute
                bit_dakika = saat_bit.hour * 60 + saat_bit.minute

                # Slotları oluştur
                for dakika in range(bas_dakika, bit_dakika, slot_suresi):
                    saat = datetime.strptime(f"{dakika // 60:02d}:{dakika % 60:02d}", "%H:%M").time()

                    # Var olan slot var mı kontrol et (dolu olanlar korunur)
                    existing = db.query(RandevuSlot).filter(
                        RandevuSlot.doktor_id == doktor_id,
                        RandevuSlot.tarih == current_date,
                        RandevuSlot.saat == saat,
                    ).first()

                    if not existing:
                        slot = RandevuSlot(
                            doktor_id=doktor_id,
                            tarih=current_date,
                            saat=saat,
                            dolu=0,
                        )
                        db.add(slot)
                        slot_count += 1

        current_date += timedelta(days=1)

    db.commit()
    return {"success": True, "message": f"{slot_count} randevu slotu oluşturuldu"}
