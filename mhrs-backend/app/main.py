from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.api.v1.api import api_router
from app.core.database import Base, engine, SessionLocal
from app.core.exceptions import (
    APIException,
    api_exception_handler,
    http_exception_handler,
    validation_exception_handler,
)
from app.core.security import get_password_hash
from app.models.brans import Brans
from app.models.doktor import Doktor
from app.models.hasta import Hasta
from app.models.hastane import Hastane
from app.models.il import Il
from app.models.ilce import Ilce
from app.models.kullanici import Kullanici, Rol


TEST_ACCOUNTS = [
    {
        "tc_no": "10000000146",
        "adi_soyadi": "Test Kullanıcı",
        "telefon": "5551234567",
        "sifre": "password123",
        "rol": Rol.HASTA,
    },
    {
        "tc_no": "10000000147",
        "adi_soyadi": "Test Doktor",
        "telefon": "5552345678",
        "sifre": "password123",
        "rol": Rol.DOKTOR,
    },
    {
        "tc_no": "10000000148",
        "adi_soyadi": "Test Admin",
        "telefon": "5553456789",
        "sifre": "password123",
        "rol": Rol.ADMIN,
    },
]


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        # Create test location data
        il = db.query(Il).filter(Il.adi == "İstanbul").first()
        if not il:
            il = Il(adi="İstanbul")
            db.add(il)
            db.flush()

        ilce = db.query(Ilce).filter(Ilce.adi == "Kadıköy", Ilce.il_id == il.id).first()
        if not ilce:
            ilce = Ilce(il_id=il.id, adi="Kadıköy")
            db.add(ilce)
            db.flush()

        hastane = db.query(Hastane).filter(Hastane.adi == "Test Hastanesi").first()
        if not hastane:
            hastane = Hastane(
                il_id=il.id,
                ilce_id=ilce.id,
                adi="Test Hastanesi",
                tip="Devlet",
                adres="Test Sokak No:1",
            )
            db.add(hastane)
            db.flush()

        brans = db.query(Brans).filter(Brans.adi == "Dahiliye").first()
        if not brans:
            brans = Brans(adi="Dahiliye")
            db.add(brans)
            db.flush()

        # Create test accounts
        for account in TEST_ACCOUNTS:
            tc_no = account["tc_no"]
            existing = db.query(Kullanici).filter(Kullanici.tc_no == tc_no).first()
            if not existing:
                sifre_hash = get_password_hash(account["sifre"])
                rol = account["rol"]
                kullanici = Kullanici(
                    tc_no=account["tc_no"],
                    adi_soyadi=account["adi_soyadi"],
                    telefon=account["telefon"],
                    sifre_hash=sifre_hash,
                    rol=rol,
                )
                db.add(kullanici)
                db.flush()  # Get the ID without committing

                # Create profile based on role
                if rol == Rol.HASTA:
                    hasta = Hasta(kullanici_id=kullanici.id)
                    db.add(hasta)
                elif rol == Rol.DOKTOR:
                    # Create doctor profile linked to test hospital and branch
                    doktor = Doktor(
                        kullanici_id=kullanici.id,
                        hastane_id=hastane.id,
                        brans_id=brans.id,
                    )
                    db.add(doktor)

        db.commit()
    finally:
        db.close()
    yield
    # Shutdown


app = FastAPI(
    title="MHRS API",
    description="Merkezi Hekim Randevu Sistemi Clone",
    version="1.0.0",
    lifespan=lifespan,
)

# Register exception handlers
app.add_exception_handler(APIException, api_exception_handler)
app.add_exception_handler(StarletteHTTPException, http_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)

# CORS ayarları
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000"],  # Svelte dev server
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router)


@app.get("/")
def root():
    return {
        "success": True,
        "data": {
            "message": "MHRS API",
            "version": "1.0.0",
        },
    }


@app.get("/health")
def health():
    return {
        "success": True,
        "data": {
            "status": "healthy",
        },
    }
