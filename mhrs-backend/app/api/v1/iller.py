from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import require_admin
from app.core.database import get_db
from app.core.exceptions import ErrorCode, NotFoundException
from app.models.il import Il
from app.models.ilce import Ilce
from app.models.kullanici import Kullanici
from app.schemas.il import Il as IlSchema, IlCreate
from app.schemas.ilce import Ilce as IlceSchema, IlceCreate

router = APIRouter(prefix="/iller", tags=["İller"])


@router.get("", response_model=list[IlSchema])
def list_iller(db: Session = Depends(get_db)):
    """Tüm illeri listeler"""
    return db.query(Il).order_by(Il.adi).all()


@router.post("", response_model=IlSchema)
def create_il(il: IlCreate, _: Annotated[Kullanici, Depends(require_admin)] = None, db: Session = Depends(get_db)):
    """Yeni il oluşturur"""
    db_il = Il(adi=il.adi)
    db.add(db_il)
    db.commit()
    db.refresh(db_il)
    return db_il


@router.delete("/{il_id}")
def delete_il(il_id: int, _: Annotated[Kullanici, Depends(require_admin)] = None, db: Session = Depends(get_db)):
    """İl siler"""
    il = db.query(Il).filter(Il.id == il_id).first()
    if not il:
        raise NotFoundException("İl bulunamadı")
    db.delete(il)
    db.commit()
    return {"success": True, "message": "İl silindi"}


@router.get("/{il_id}/ilceler", response_model=list[IlceSchema])
def list_ilceler(il_id: int, db: Session = Depends(get_db)):
    """İlin ilçelerini listeler"""
    return db.query(Ilce).filter(Ilce.il_id == il_id).order_by(Ilce.adi).all()


@router.post("/{il_id}/ilceler", response_model=IlceSchema)
def create_ilce(
    il_id: int,
    ilce: IlceCreate,
    _: Annotated[Kullanici, Depends(require_admin)] = None,
    db: Session = Depends(get_db),
):
    """İle yeni ilçe oluşturur"""
    # İl var mı kontrol et
    il = db.query(Il).filter(Il.id == il_id).first()
    if not il:
        raise NotFoundException("İl bulunamadı")

    db_ilce = Ilce(il_id=il_id, adi=ilce.adi)
    db.add(db_ilce)
    db.commit()
    db.refresh(db_ilce)
    return db_ilce


@router.delete("/{il_id}/ilceler/{ilce_id}")
def delete_ilce(
    il_id: int,
    ilce_id: int,
    _: Annotated[Kullanici, Depends(require_admin)] = None,
    db: Session = Depends(get_db),
):
    """İlçe siler"""
    ilce = db.query(Ilce).filter(Ilce.id == ilce_id, Ilce.il_id == il_id).first()
    if not ilce:
        raise NotFoundException("İlçe bulunamadı")
    db.delete(ilce)
    db.commit()
    return {"success": True, "message": "İlçe silindi"}
