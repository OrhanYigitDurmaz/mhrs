# MHRS Backend Planı

## 📁 Proje Yapısı
```
mhrs-backend/
├── app/
│   ├── __init__.py
│   ├── main.py                 # FastAPI app entry point
│   ├── core/
│   │   ├── __init__.py
│   │   ├── config.py           # Settings (pydantic-settings)
│   │   ├── database.py         # DB connection, session
│   │   └── security.py         # JWT, TC validation, password hash
│   ├── models/
│   │   ├── __init__.py
│   │   ├── il.py               # İl modeli
│   │   ├── ilce.py             # Ilçe modeli
│   │   ├── hastane.py          # Hastane modeli
│   │   ├── brans.py            # Branş modeli
│   │   ├── doktor.py           # Doktor modeli
│   │   ├── hasta.py            # Hasta modeli
│   │   ├── randevu.py          # Randevu modeli
│   │   └── calisma_saati.py    # Doktor çalışma saatleri
│   ├── schemas/
│   │   ├── __init__.py
│   │   ├── il.py
│   │   ├── ilce.py
│   │   ├── hastane.py
│   │   ├── brans.py
│   │   ├── doktor.py
│   │   ├── hasta.py
│   │   ├── randevu.py
│   │   └── auth.py             # Login, Register schemas
│   ├── api/
│   │   ├── __init__.py
│   │   ├── deps.py             # Dependency injections (get_db, get_current_user)
│   │   ├── v1/
│   │   │   ├── __init__.py
│   │   │   ├── api.py          # API router include
│   │   │   ├── auth.py         # /auth/login, /auth/register
│   │   │   ├── iller.py        # GET /iller, /ilceler
│   │   │   ├── hastaneler.py   # GET /hastaneler, /hastaneler/{id}
│   │   │   ├── branşlar.py     # GET /branşlar
│   │   │   ├── doktorlar.py    # GET /doktorlar, /musaitlik
│   │   │   └── randevular.py   # POST/DELETE /randevular
│   └── services/
│       ├── __init__.py
│       ├── auth_service.py     # TC validation, login logic
│       └── randevu_service.py  # Müsaitlik kontrolü, randevu oluşturma
├── alembic/
│   ├── versions/
│   └── env.py
├── alembic.ini
├── Dockerfile
├── requirements.txt
└── .env.example
```

## 📦 requirements.txt
```
fastapi==0.115.0
uvicorn[standard]==0.32.0
sqlalchemy==2.0.36
alembic==1.14.0
pymysql==1.1.1              # MariaDB connector
cryptography==44.0          # RSA key support
pydantic==2.10.3
pydantic-settings==2.6.0
python-jose[cryptography]==3.3.0  # JWT
passlib[bcrypt]==1.7.4
python-multipart==0.0.20
```

## 🗄️ Database Modelleri

| Tablo | Alanlar | Açıklama |
|-------|---------|----------|
| `iller` | id, adi | 81 il |
| `ilceler` | id, il_id(fk), adi | İlçeler |
| `hastaneler` | id, il_id(fk), ilce_id(fk), adi, tip, adres | Hastane bilgisi |
| `branşlar` | id, adi | Dahiliye, Cerrahi vs. |
| `doktorlar` | id, hastane_id(fk), brans_id(fk), adi_soyadi | Doktor |
| `calisma_saati` | id, doktor_id(fk), gun, saat_bas, saat_bit | Çalışma programı |
| `randevu_slot` | id, doktor_id(fk), tarih, saat, dolu | 20dk'lık slotlar |
| `hastalar` | id, tc_no(unique), adi_soyadi, telefon, sifre_hash | Hasta |
| `randevular` | id, hasta_id(fk), doktor_id(fk), tarih, saat, durum | Randevu kaydı |

## 🔌 API Endpoints

```
POST   /api/v1/auth/register     # Hasta kayıt (TC ile)
POST   /api/v1/auth/login        # Giriş -> JWT token

GET    /api/v1/iller             # İl listesi
GET    /api/v1/ilceler/{il_id}   # İlçe listesi

GET    /api/v1/hastaneler?il=1&ilce=5&brans=2  # Filtreleme
GET    /api/v1/hastaneler/{id}

GET    /api/v1/branşlar          # Tüm branşlar

GET    /api/v1/doktorlar?hastane=1&brans=2
GET    /api/v1/doktorlar/{id}/musaitlik?baslangic=2025-01-01&bitis=2025-01-07

POST   /api/v1/randevular        # Randevu al (hasta_id, doktor_id, tarih, saat)
DELETE /api/v1/randevular/{id}   # İptal
GET    /api/v1/randevular        # Hastanın randevuları
```

## 🔐 TC Kimlik Validasyon Algoritması
```python
def validate_tc_no(tc_no: str) -> bool:
    # 11 haneli, tümü rakam
    # 1. 3. 5. 7. 9. basamak toplamı * 7
    # - 2. 4. 6. 8. basamak toplamı
    # = 10. basamak
    # İlk 10 basamak toplamının mod 10'u = 11. basamak
```

## 📋 Geliştirme Sırası

1. Backend proje yapısını oluştur
2. Database modellerini yaz (SQLAlchemy)
3. Alembic kurulumu ve ilk migration
4. Pydantic schema'ları
5. Core: config, database, security
6. Auth API (register, login)
7. İl/İlçe/Hastane/Branş/Doktor CRUD
8. Randevu müsaitlik servisi
9. Randevu al/iptal API
