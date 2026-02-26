# API

```txt
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
POST /auth/forgot-password
POST /auth/reset-password
POST /auth/verify-email
GET /auth/me
GET /auth/sessions
DELETE /auth/sessions/{sessionId}/revoke
POST /auth/logout-all

GET /clinics
GET /clinics/{clinicId}
POST /clinics
PATCH /clinics/{clinicId}
DELETE /clinics/{clinicId}
GET /departments
POST /departments
PATCH /departments/{departmentId}
DELETE /departments/{departmentId}
GET /specialties
POST /specialties
PATCH /specialties/{specialtyId}
DELETE /specialties/{specialtyId}
GET /doctors
GET /doctors/{doctorId}
POST /doctors
PATCH /doctors/{doctorId}/set-status
PATCH /doctors/{doctorId}

POST /doctors/{id}/schedules
GET /doctors/{id}/schedules
PATCH /doctors/{id}/schedules/{scheduleId}
DELETE /doctors/{id}/schedules/{scheduleId}
POST /doctors/{id}/leaves
GET /doctors/{id}/leaves
PATCH /doctors/{id}/leaves/{leaveId}
DELETE /doctors/{id}/leaves/{leaveId}
GET /doctors/{id}/slots?date=...

POST /appointments
GET /appointments
GET /appointments/{id}
PATCH /appointments/{id}/confirm
PATCH /appointments/{id}/cancel
PATCH /appointments/{id}/reschedule
PATCH /appointments/{id}/complete
PATCH /appointments/{id}/no-show
POST /appointments/{id}/admin-override

GET /patients/me
PATCH /patients/me
GET /patients/me/appointments
GET /patients/me/verification-status
POST /patients/me/verification/submit
GET /patients
GET /patients/{patientId}
GET /patients/{patientId}/appointments

GET /notifications/deliveries
GET /notifications/deliveries/{deliveryId}
GET /notifications/dead-letters
POST /notifications/templates
PATCH /notifications/templates/{templateId}

GET /search/doctors
GET /search/patients
POST /search/reindex-jobs/start
GET /search/reindex-jobs/{jobId}

GET /admin/audit-logs
GET /admin/reports
POST /admin/schedule-conflict-cases/open
GET /admin/schedule-conflict-cases/{caseId}
POST /admin/schedule-conflict-cases/{caseId}/apply-action

POST /waitlist/entries
GET /waitlist/entries
GET /waitlist/entries?status=...&clinicId=...&departmentId=...&specialtyId=...
GET /waitlist/entries/{entryId}
PATCH /waitlist/entries/{entryId}
DELETE /waitlist/entries/{entryId}
POST /waitlist/entries/{entryId}/rejoin
GET /waitlist/offers
GET /waitlist/offers/{offerId}
POST /waitlist/offers/{offerId}/accept
POST /waitlist/offers/{offerId}/decline
```

## Admin Doctor Management v0.1 (Draft)

This section defines admin-only endpoints to add and remove doctors.

### Endpoints

POST /admin/doctors

Request:

```json
{
  "clinicId": "c1",
  "departmentId": "dep1",
  "specialtyId": "sp1",
  "firstName": "Aylin",
  "lastName": "Kaya",
  "title": "Dr.",
  "email": "aylin.kaya@example.com",
  "phone": "+905001112233",
  "status": "ACTIVE"
}
```

Response: Doctor. Status code: 201.

DELETE /admin/doctors/{doctorId}

Response: 204 No Content.

### Notes

Admin doctor creation uses the same validation rules as `POST /doctors`.

Removing a doctor is **soft delete only**: set `status` to `INACTIVE`. **No hard deletes.**

## Doctor Module v0.1 (Draft)

This section defines a minimal contract so the doctor module can be implemented now.

### Data Types

DoctorStatus: `ACTIVE`, `INACTIVE`, `SUSPENDED`

DayOfWeek: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

Time format: `HH:mm` (24h). Date format: `YYYY-MM-DD`. Timestamp: ISO-8601.

### Doctor

Response shape:

```json
{
  "doctorId": "d1",
  "clinicId": "c1",
  "departmentId": "dep1",
  "specialtyId": "sp1",
  "firstName": "Aylin",
  "lastName": "Kaya",
  "title": "Dr.",
  "email": "aylin.kaya@example.com",
  "phone": "+905001112233",
  "status": "ACTIVE",
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-02T00:00:00Z"
}
```

### Schedule

Response shape:

```json
{
  "scheduleId": "sch1",
  "dayOfWeek": "MONDAY",
  "startTime": "09:00",
  "endTime": "17:00",
  "slotMinutes": 20,
  "timezone": "Europe/Istanbul",
  "active": true
}
```

### Leave

Response shape:

```json
{
  "leaveId": "lv1",
  "startDate": "2026-03-01",
  "endDate": "2026-03-03",
  "reason": "Conference",
  "active": true
}
```

### Slots

Response shape:

```json
[
  {
    "date": "2026-03-10",
    "startTime": "09:00",
    "endTime": "09:20",
    "available": true,
    "timezone": "Europe/Istanbul"
  }
]
```

### Endpoints

GET /doctors

Query params (all optional): `clinicId`, `departmentId`, `specialtyId`, `status`

Response: list of Doctor.

GET /doctors/{doctorId}

Response: Doctor.

POST /doctors

Request:

```json
{
  "clinicId": "c1",
  "departmentId": "dep1",
  "specialtyId": "sp1",
  "firstName": "Aylin",
  "lastName": "Kaya",
  "title": "Dr.",
  "email": "aylin.kaya@example.com",
  "phone": "+905001112233",
  "status": "ACTIVE"
}
```

Response: Doctor. Status code: 201.

PATCH /doctors/{doctorId}

Request (all optional):

```json
{
  "clinicId": "c1",
  "departmentId": "dep1",
  "specialtyId": "sp1",
  "firstName": "Aylin",
  "lastName": "Kaya",
  "title": "Prof.",
  "email": "aylin.kaya@example.com",
  "phone": "+905001112233"
}
```

Response: Doctor.

PATCH /doctors/{doctorId}/set-status

Request:

```json
{
  "status": "SUSPENDED"
}
```

Response: Doctor.

POST /doctors/{id}/schedules

Request:

```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "09:00",
  "endTime": "17:00",
  "slotMinutes": 20,
  "timezone": "Europe/Istanbul"
}
```

Response: Schedule. Status code: 201.

GET /doctors/{id}/schedules

Response: list of Schedule.

PATCH /doctors/{id}/schedules/{scheduleId}

Request (all optional):

```json
{
  "startTime": "10:00",
  "endTime": "16:00",
  "slotMinutes": 15,
  "timezone": "Europe/Istanbul",
  "active": true
}
```

Response: Schedule.

DELETE /doctors/{id}/schedules/{scheduleId}

Response: 204 No Content.

POST /doctors/{id}/leaves

Request:

```json
{
  "startDate": "2026-03-01",
  "endDate": "2026-03-03",
  "reason": "Conference"
}
```

Response: Leave. Status code: 201.

GET /doctors/{id}/leaves

Response: list of Leave.

PATCH /doctors/{id}/leaves/{leaveId}

Request (all optional):

```json
{
  "startDate": "2026-03-02",
  "endDate": "2026-03-04",
  "reason": "Updated reason",
  "active": true
}
```

Response: Leave.

DELETE /doctors/{id}/leaves/{leaveId}

Response: 204 No Content.

GET /doctors/{id}/slots?date=YYYY-MM-DD

Response: list of Slots.

GET /search/doctors

Query params (all optional): `query`, `clinicId`, `departmentId`, `specialtyId`, `status`

Response: list of Doctor.

### Validation Rules

Email must be valid format if provided. Phone length 7-20 chars if provided.

`firstName` and `lastName` required on create, min length 1.

`slotMinutes` must be >= 5 and <= 180.

`startTime` must be before `endTime`.

`startDate` must be on or before `endDate`.

`timezone` must be a valid IANA timezone string.

## Appointment Module v0.1 (Draft)

This section defines a minimal contract so the appointment module can be implemented now.

### Data Types

AppointmentStatus: `PENDING`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED`, `NO_SHOW`

Date format: `YYYY-MM-DD`. Time format: `HH:mm` (24h). Timestamp: ISO-8601.

### Appointment

Response shape:

```json
{
  "appointmentId": "a1",
  "patientId": "p1",
  "doctorId": "d1",
  "clinicId": "c1",
  "departmentId": "dep1",
  "specialtyId": "sp1",
  "date": "2026-03-10",
  "startTime": "09:00",
  "endTime": "09:20",
  "status": "CONFIRMED",
  "reason": "Routine checkup",
  "notes": "Bring previous reports",
  "createdAt": "2026-03-01T10:00:00Z",
  "updatedAt": "2026-03-02T11:00:00Z"
}
```

### Endpoints

POST /appointments

Request:

```json
{
  "patientId": "p1",
  "doctorId": "d1",
  "clinicId": "c1",
  "departmentId": "dep1",
  "specialtyId": "sp1",
  "date": "2026-03-10",
  "startTime": "09:00",
  "endTime": "09:20",
  "reason": "Routine checkup",
  "notes": "Bring previous reports"
}
```

Response: Appointment. Status code: 201.

GET /appointments

Query params (all optional): `patientId`, `doctorId`, `clinicId`, `departmentId`, `specialtyId`, `status`, `dateFrom`, `dateTo`

Response: list of Appointment.

GET /appointments/{id}

Response: Appointment.

PATCH /appointments/{id}/confirm

Response: Appointment.

PATCH /appointments/{id}/cancel

Request (optional):

```json
{
  "reason": "Patient requested cancellation"
}
```

Response: Appointment.

PATCH /appointments/{id}/reschedule

Request:

```json
{
  "date": "2026-03-12",
  "startTime": "10:00",
  "endTime": "10:20",
  "reason": "Scheduling conflict"
}
```

Response: Appointment.

PATCH /appointments/{id}/complete

Response: Appointment.

PATCH /appointments/{id}/no-show

Response: Appointment.

POST /appointments/{id}/admin-override

Request:

```json
{
  "status": "CONFIRMED",
  "notes": "Admin override applied"
}
```

Response: Appointment.

### Validation Rules

`patientId`, `doctorId`, `clinicId`, `departmentId`, `specialtyId` required on create.

`date`, `startTime`, `endTime` required on create.

`startTime` must be before `endTime`.

Reschedule requires `date`, `startTime`, `endTime`.

`reason` length 1-500 if provided. `notes` length 0-1000 if provided.

## Patient Module v0.1 (Draft)

This section defines a minimal contract so the patient module can be implemented now.

### Data Types

PatientStatus: `ACTIVE`, `INACTIVE`

VerificationStatus: `UNVERIFIED`, `PENDING`, `VERIFIED`, `REJECTED`

Gender: `FEMALE`, `MALE`, `OTHER`, `UNKNOWN`

Date format: `YYYY-MM-DD`. Timestamp: ISO-8601.

### Patient

Response shape:

```json
{
  "patientId": "p1",
  "firstName": "Elif",
  "lastName": "Yilmaz",
  "email": "elif.yilmaz@example.com",
  "phone": "+905001112233",
  "dateOfBirth": "1990-05-10",
  "gender": "FEMALE",
  "status": "ACTIVE",
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-02T00:00:00Z"
}
```

### Verification

Response shape:

```json
{
  "status": "PENDING",
  "submittedAt": "2026-01-03T09:00:00Z",
  "reviewedAt": null,
  "notes": null
}
```

### Appointment Summary (for patient views)

Response shape:

```json
{
  "appointmentId": "a1",
  "doctorId": "d1",
  "clinicId": "c1",
  "departmentId": "dep1",
  "specialtyId": "sp1",
  "scheduledAt": "2026-03-10T09:00:00Z",
  "status": "SCHEDULED"
}
```

### Endpoints

GET /patients/me

Response: Patient.

PATCH /patients/me

Request (all optional):

```json
{
  "firstName": "Elif",
  "lastName": "Yilmaz",
  "email": "elif.yilmaz@example.com",
  "phone": "+905001112233",
  "dateOfBirth": "1990-05-10",
  "gender": "FEMALE"
}
```

Response: Patient.

GET /patients/me/appointments

Response: list of Appointment Summary.

GET /patients/me/verification-status

Response: Verification.

POST /patients/me/verification/submit

Request:

```json
{
  "identityNumber": "11111111111",
  "documentUrl": "https://example.com/documents/scan-1.pdf",
  "notes": "Optional note"
}
```

Response: Verification. Status code: 201.

GET /patients

Query params (all optional): `status`, `query`

Response: list of Patient.

GET /patients/{patientId}

Response: Patient.

GET /patients/{patientId}/appointments

Response: list of Appointment Summary.

GET /search/patients

Query params (all optional): `query`, `status`

Response: list of Patient.

### Validation Rules

Email must be valid format if provided. Phone length 7-20 chars if provided.

`firstName` and `lastName` required on create (system-managed for now) and min length 1 if provided on update.

`dateOfBirth` cannot be in the future.

`identityNumber` must be 6-20 chars (numeric or alphanumeric).

`documentUrl` must be a valid URL.
