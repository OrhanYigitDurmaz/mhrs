# MHRS API Documentation

**Base URL:** `http://localhost:8000`

Interactive API docs available at: `http://localhost:8000/docs`

---

## Table of Contents

- [Response Formats](#response-formats)
- [Authentication](#authentication)
- [Endpoints](#endpoints)
  - [Auth](#auth-apiv1auth)
  - [Cities (İller)](#cities-iller-apiv1iller)
  - [Districts (İlçeler)](#districts-ilçeler-apiv1illeril_idilceler)
  - [Branches (Branşlar)](#branches-branşlar-apiv1branşlar)
  - [Hospitals (Hastaneler)](#hospitals-hastaneler-apiv1hastaneler)
  - [Doctors (Doktorlar)](#doctors-doktorlar-apiv1doktorlar)
  - [Working Hours (Çalışma Saati)](#working-hours-çalışma-saati-apiv1calisma-saati)
  - [Appointment Slots (Randevu Slot)](#appointment-slots-randevu-slot-apiv1randevu-slot)
  - [Appointments (Randevular)](#appointments-randevular-apiv1randevular)

---

## Response Formats

### Success Response
```json
{
  "success": true,
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message",
    "details": { }
  }
}
```

### Error Codes

| Code | Description |
|------|-------------|
| `INVALID_TOKEN` | Token is invalid or expired |
| `INVALID_CREDENTIALS` | Wrong TC no or password |
| `USER_NOT_FOUND` | User not found |
| `USER_ALREADY_EXISTS` | TC no already registered |
| `INVALID_TC_NO` | Turkish ID number validation failed |
| `FORBIDDEN` | Insufficient permissions |
| `NOT_FOUND` | Resource not found |
| `VALIDATION_ERROR` | Request validation failed |
| `INVALID_ROLE` | Invalid role specified |
| `SLOT_NOT_AVAILABLE` | Appointment slot is full |
| `APPOINTMENT_EXISTS` | User already has an appointment at this time |
| `APPOINTMENT_ALREADY_CANCELLED` | Appointment is already cancelled/completed |
| `NO_WORKING_HOURS` | Doctor has no working hours defined |

---

## Authentication

### Roles

| Role | Description |
|------|-------------|
| `admin` | Full system access |
| `doktor` | Can manage own working hours and appointment slots |
| `user` | Can book/view/cancel own appointments |

### Authorization Header

All protected endpoints require:
```
Authorization: Bearer <access_token>
```

---

## Endpoints

### Auth (`/api/v1/auth`)

#### POST `/register`

Public registration for users. Creates account with `user` role.

**Request Body:**
```json
{
  "tc_no": "10000000146",
  "adi_soyadi": "John Doe",
  "telefon": "5551234567",
  "sifre": "password123"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGci...",
  "token_type": "bearer"
}
```

**Validations:**
- `tc_no`: 11 digits, valid Turkish ID
- `adi_soyadi`: min 3 characters
- `sifre`: min 6 characters

---

#### POST `/login`

Login with TC number and password.

**Request Body:**
```json
{
  "tc_no": "10000000146",
  "sifre": "password123"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGci...",
  "token_type": "bearer"
}
```

---

#### GET `/me`

Get current user profile. Requires authentication.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "tc_no": "10000000146",
    "adi_soyadi": "John Doe",
    "telefon": "5551234567",
    "rol": "user"
  }
}
```

---

#### POST `/admin/create-user`

Create a user with any role. **Admin only**.

**Request Body:**
```json
{
  "tc_no": "10000000147",
  "adi_soyadi": "Jane Doe",
  "telefon": "5551234568",
  "sifre": "password123",
  "rol": "doktor"
}
```

**Validations:**
- `rol`: must be one of `admin`, `doktor`, `user`

---

### Cities (İller) (`/api/v1/iller`)

#### GET `/iller`

List all cities.

**Response:**
```json
[
  { "id": 1, "adi": "İstanbul" },
  { "id": 2, "adi": "Ankara" }
]
```

---

#### POST `/iller`

Create a new city. **Admin only**.

**Request Body:**
```json
{
  "adi": "İstanbul"
}
```

---

#### DELETE `/iller/{il_id}`

Delete a city. **Admin only**.

---

### Districts (İlçeler) (`/api/v1/iller/{il_id}/ilceler`)

#### GET `/iller/{il_id}/ilceler`

List all districts of a city.

**Response:**
```json
[
  { "id": 1, "il_id": 1, "adi": "Kadıköy" },
  { "id": 2, "il_id": 1, "adi": "Beşiktaş" }
]
```

---

#### POST `/iller/{il_id}/ilceler`

Create a new district. **Admin only**.

**Request Body:**
```json
{
  "adi": "Kadıköy"
}
```

---

#### DELETE `/iller/{il_id}/ilceler/{ilce_id}`

Delete a district. **Admin only**.

---

### Branches (Branşlar) (`/api/v1/branşlar`)

#### GET `/branşlar`

List all medical branches.

**Response:**
```json
[
  { "id": 1, "adi": "Kardiyoloji" },
  { "id": 2, "adi": "Dahiliye" }
]
```

---

#### POST `/branşlar`

Create a new branch. **Admin only**.

**Request Body:**
```json
{
  "adi": "Kardiyoloji"
}
```

---

#### DELETE `/branşlar/{branş_id}`

Delete a branch. **Admin only**.

---

### Hospitals (Hastaneler) (`/api/v1/hastaneler`)

#### GET `/hastaneler`

List hospitals with optional filters.

**Query Parameters:**
- `il` (optional): Filter by city ID
- `ilce` (optional): Filter by district ID
- `brans` (optional): Filter by branch ID

**Example:** `GET /hastaneler?il=1&brans=2`

**Response:**
```json
[
  {
    "id": 1,
    "il_id": 1,
    "ilce_id": 1,
    "adi": "Acıbadem Hastanesi",
    "tip": "Özel",
    "adres": "Kadıköy, İstanbul"
  }
]
```

---

#### GET `/hastaneler/{hastane_id}`

Get hospital details.

---

#### POST `/hastaneler`

Create a new hospital. **Admin only**.

**Request Body:**
```json
{
  "il_id": 1,
  "ilce_id": 1,
  "adi": "Acıbadem Hastanesi",
  "tip": "Özel",
  "adres": "Kadıköy, İstanbul"
}
```

---

#### DELETE `/hastaneler/{hastane_id}`

Delete a hospital. **Admin only**.

---

### Doctors (Doktorlar) (`/api/v1/doktorlar`)

#### GET `/doktorlar`

List doctors with optional filters.

**Query Parameters:**
- `hastane` (optional): Filter by hospital ID
- `brans` (optional): Filter by branch ID

**Response:**
```json
[
  {
    "id": 1,
    "hastane_id": 1,
    "brans_id": 1,
    "adi_soyadi": "Dr. Ahmet Yılmaz"
  }
]
```

---

#### GET `/doktorlar/{doktor_id}`

Get doctor details.

---

#### POST `/doktorlar`

Create a new doctor. **Admin only**.

**Request Body:**
```json
{
  "hastane_id": 1,
  "brans_id": 1,
  "adi_soyadi": "Dr. Ahmet Yılmaz"
}
```

---

#### DELETE `/doktorlar/{doktor_id}`

Delete a doctor. **Admin only**.

---

#### GET `/doktorlar/{doktor_id}/musaitlik`

Get doctor's availability slots.

**Query Parameters:**
- `baslangic` (required): Start date (YYYY-MM-DD)
- `bitis` (required): End date (YYYY-MM-DD)

**Example:** `GET /doktorlar/1/musaitlik?baslangic=2025-01-01&bitis=2025-01-07`

**Response:**
```json
[
  {
    "tarih": "2025-01-01",
    "saat": "09:00",
    "musait": true
  },
  {
    "tarih": "2025-01-01",
    "saat": "09:30",
    "musait": false
  }
]
```

---

### Working Hours (Çalışma Saati) (`/api/v1/calisma-saati`)

#### GET `/calisma-saati`

List all working hours. **Admin/Doctor only**.

---

#### GET `/calisma-saati/doktor/{doktor_id}`

Get doctor's working hours. Public.

**Response:**
```json
[
  {
    "id": 1,
    "doktor_id": 1,
    "gun": "Pazartesi",
    "saat_bas": "09:00",
    "saat_bit": "17:00"
  }
]
```

**Valid `gun` values:** `Pazartesi`, `Salı`, `Çarşamba`, `Perşembe`, `Cuma`, `Cumartesi`, `Pazar`

---

#### POST `/calisma-saati`

Create working hours. **Admin/Doctor (own) only**.

**Request Body:**
```json
{
  "doktor_id": 1,
  "gun": "Pazartesi",
  "saat_bas": "09:00",
  "saat_bit": "17:00"
}
```

---

#### DELETE `/calisma-saati/{saat_id}`

Delete working hours. **Admin/Doctor (own) only**.

---

### Appointment Slots (Randevu Slot) (`/api/v1/randevu-slot`)

#### GET `/randevu-slot/doktor/{doktor_id}`

Get doctor's appointment slots.

**Query Parameters:**
- `baslangic` (optional): Start date
- `bitis` (optional): End date

**Response:**
```json
[
  {
    "id": 1,
    "doktor_id": 1,
    "tarih": "2025-01-01",
    "saat": "09:00",
    "dolu": 0
  }
]
```

---

#### POST `/randevu-slot`

Manually create a single slot. **Admin/Doctor (own) only**.

**Request Body:**
```json
{
  "doktor_id": 1,
  "tarih": "2025-01-01",
  "saat": "09:00"
}
```

---

#### DELETE `/randevu-slot/{slot_id}`

Delete a slot. **Admin/Doctor (own) only**.

---

#### POST `/randevu-slot/doktor/{doktor_id}/generate`

Generate appointment slots from working hours. **Admin/Doctor (own) only**.

**Query Parameters:**
- `baslangic` (required): Start date (YYYY-MM-DD)
- `bitis` (required): End date (YYYY-MM-DD)
- `slot_suresi` (optional): Slot duration in minutes, default 30

**Example:** `POST /randevu-slot/doktor/1/generate?baslangic=2025-01-01&bitis=2025-01-31&slot_suresi=30`

**Response:**
```json
{
  "success": true,
  "message": "120 randevu slotu oluşturuldu"
}
```

---

### Appointments (Randevular) (`/api/v1/randevular`)

All appointment endpoints require authentication.

#### GET `/randevular`

List current user's appointments.

**Response:**
```json
[
  {
    "id": 1,
    "hasta_id": 1,
    "doktor_id": 1,
    "tarih": "2025-01-01",
    "saat": "09:00",
    "durum": "aktif"
  }
]
```

**Durum values:** `aktif`, `iptal`, `tamamlandı`

---

#### POST `/randevular`

Create a new appointment. **User role required**.

**Request Body:**
```json
{
  "doktor_id": 1,
  "tarih": "2025-01-01",
  "saat": "09:00"
}
```

**Validations:**
- Slot must exist
- Slot must be available
- User cannot have overlapping active appointments

---

#### DELETE `/randevular/{randevu_id}`

Cancel an appointment. Only the appointment owner can cancel.

**Response:**
```json
{
  "success": true,
  "message": "Randevu iptal edildi"
}
```

**Validations:**
- Appointment must belong to current user
- Appointment must be in `aktif` status

---

## Common Usage Flows

### 1. Patient Books an Appointment

```
1. POST /auth/login → Get token
2. GET /iller → List cities
3. GET /iller/{id}/ilceler → List districts
4. GET /hastaneler?il={il_id} → List hospitals
5. GET /branşlar → List branches
6. GET /doktorlar?hastane={hastane_id}&brans={brans_id} → List doctors
7. GET /doktorlar/{doktor_id}/musaitlik?baslangic={date}&bitis={date} → Check availability
8. POST /randevular → Book appointment
```

### 2. Admin Sets Up a Doctor

```
1. POST /auth/admin/create-user → Create doctor account
2. POST /doktorlar → Create doctor profile
3. POST /calisma-saati → Set working hours
4. POST /randevu-slot/doktor/{id}/generate → Generate appointment slots
```

### 3. Doctor Manages Schedule

```
1. POST /auth/login (as doctor) → Get token
2. GET /calisma-saati/doktor/{self_id} → View working hours
3. POST /calisma-saati → Add/update working hours
4. POST /randevu-slot/doktor/{self_id}/generate → Regenerate slots
```

---

## Turkish ID (TC Kimlik) Validation

The API validates Turkish ID numbers using the official algorithm:

- Must be 11 digits
- First digit cannot be 0
- 10th digit is calculated from digits 1-9
- 11th digit is the sum of digits 1-10 mod 10

Example valid TC numbers for testing:
- `10000000146`
- `11111111110`
