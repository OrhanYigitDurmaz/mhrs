from typing import Annotated, Optional

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import require_admin
from app.core.database import get_db
from app.core.exceptions import NotFoundException
from app.models.doktor import Doktor
from app.models.hastane import Hastane
from app.models.kullanici import Kullanici
from app.schemas.doktor import DoktorWithUser
from app.schemas.hastane import Hastane as HastaneSchema, HastaneCreate

router = APIRouter(prefix="/hastaneler", tags=["Hastaneler"])


@router.get("", response_model=list[HastaneSchema])
def list_hastaneler(
    il: Optional[int] = None,
    ilce: Optional[int] = None,
    brans: Optional[int] = None,
    db: Session = Depends(get_db),
):
    """Hastaneleri filtreleyerek listeler"""
    query = db.query(Hastane)

    if il is not None:
        query = query.filter(Hastane.il_id == il)
    if ilce is not None:
        query = query.filter(Hastane.ilce_id == ilce)

    # Branş filtresi - doktor tablosu ile join gerekli
    if brans is not None:
        from app.models.doktor import Doktor
        query = query.join(Doktor).filter(Doktor.brans_id == brans).distinct()

    return query.all()


@router.get("/{hastane_id}", response_model=HastaneSchema)
def get_hastane(hastane_id: int, db: Session = Depends(get_db)):
    """Hastane detayını getirir"""
    hastane = db.query(Hastane).filter(Hastane.id == hastane_id).first()
    if not hastane:
        raise NotFoundException("Hastane bulunamadı")
    return hastane


@router.get("/{hastane_id}/doktorlar", response_model=list[DoktorWithUser])
def get_hastane_doktorlari(hastane_id: int, brans: Optional[int] = None, db: Session = Depends(get_db)):
    """Hastanedeki doktorları listeler"""
    hastane = db.query(Hastane).filter(Hastane.id == hastane_id).first()
    if not hastane:
        raise NotFoundException("Hastane bulunamadı")

    query = db.query(Doktor).filter(Doktor.hastane_id == hastane_id)

    if brans is not None:
        query = query.filter(Doktor.brans_id == brans)

    doktorlar = query.all()

    # Join with Kullanici to get user info
    from app.models.kullanici import Kullanici
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


@router.post("", response_model=HastaneSchema)
def create_hastane(
    hastane: HastaneCreate,
    _: Annotated[Kullanici, Depends(require_admin)] = None,
    db: Session = Depends(get_db),
):
    """Yeni hastane oluşturur"""
    db_hastane = Hastane(
        il_id=hastane.il_id,
        ilce_id=hastane.ilce_id,
        adi=hastane.adi,
        tip=hastane.tip,
        adres=hastane.adres,
    )
    db.add(db_hastane)
    db.commit()
    db.refresh(db_hastane)
    return db_hastane


@router.delete("/{hastane_id}")
def delete_hastane(hastane_id: int, _: Annotated[Kullanici, Depends(require_admin)] = None, db: Session = Depends(get_db)):
    """Hastane siler"""
    hastane = db.query(Hastane).filter(Hastane.id == hastane_id).first()
    if not hastane:
        raise NotFoundException("Hastane bulunamadı")
    db.delete(hastane)
    db.commit()
    return {"success": True, "message": "Hastane silindi"}
