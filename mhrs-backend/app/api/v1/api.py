from fastapi import APIRouter

from app.api.v1 import (
    auth,
    branşlar as branslar,
    calisma_saati,
    doktorlar,
    hastaneler,
    iller,
    randevular,
    randevu_slot,
)

api_router = APIRouter(prefix="/api/v1")

api_router.include_router(auth.router)
api_router.include_router(iller.router)
api_router.include_router(hastaneler.router)
api_router.include_router(branslar.router)
api_router.include_router(doktorlar.router)
api_router.include_router(randevular.router)
api_router.include_router(calisma_saati.router)
api_router.include_router(randevu_slot.router)
