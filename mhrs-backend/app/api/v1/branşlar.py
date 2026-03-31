from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import require_admin
from app.core.database import get_db
from app.core.exceptions import NotFoundException
from app.models.brans import Brans
from app.models.kullanici import Kullanici
from app.schemas.brans import Brans as BransSchema, BransCreate

router = APIRouter(prefix="/branşlar", tags=["Branşlar"])


@router.get("", response_model=list[BransSchema])
def list_branşlar(db: Session = Depends(get_db)):
    """Tüm branşları listeler"""
    return db.query(Brans).order_by(Brans.adi).all()


@router.post("", response_model=BransSchema)
def create_branş(branş: BransCreate, _: Annotated[Kullanici, Depends(require_admin)] = None, db: Session = Depends(get_db)):
    """Yeni branş oluşturur"""
    db_branş = Brans(adi=branş.adi)
    db.add(db_branş)
    db.commit()
    db.refresh(db_branş)
    return db_branş


@router.delete("/{branş_id}")
def delete_branş(branş_id: int, _: Annotated[Kullanici, Depends(require_admin)] = None, db: Session = Depends(get_db)):
    """Branş siler"""
    branş = db.query(Brans).filter(Brans.id == branş_id).first()
    if not branş:
        raise NotFoundException("Branş bulunamadı")
    db.delete(branş)
    db.commit()
    return {"success": True, "message": "Branş silindi"}
