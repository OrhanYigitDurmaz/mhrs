package com.mhrs.patient.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mhrs.patient.application.command.SubmitVerificationCommand;
import com.mhrs.patient.application.command.UpdatePatientCommand;
import com.mhrs.patient.application.port.out.CurrentPatientProvider;
import com.mhrs.patient.application.port.out.PatientAppointmentRepository;
import com.mhrs.patient.application.port.out.PatientRepository;
import com.mhrs.patient.application.port.out.PatientVerificationRepository;
import com.mhrs.patient.domain.Patient;
import com.mhrs.patient.domain.PatientStatus;
import com.mhrs.patient.domain.PatientVerification;
import com.mhrs.patient.domain.VerificationStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientApplicationServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientVerificationRepository verificationRepository;

    @Mock
    private PatientAppointmentRepository appointmentRepository;

    @Mock
    private CurrentPatientProvider currentPatientProvider;

    private PatientApplicationService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
            Instant.parse("2026-01-01T00:00:00Z"),
            ZoneOffset.UTC
        );
        service = new PatientApplicationService(
            patientRepository,
            verificationRepository,
            appointmentRepository,
            currentPatientProvider,
            clock
        );
    }

    @Test
    void updateMeCreatesPatientWhenMissing() {
        when(currentPatientProvider.currentPatientId()).thenReturn("p1");
        when(currentPatientProvider.currentEmail()).thenReturn(
            "elif@example.com"
        );
        when(patientRepository.findById("p1")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        Patient updated = service.updateMe(
            new UpdatePatientCommand("Elif", "Yilmaz", null, null, null, null)
        );

        assertEquals("p1", updated.patientId());
        assertEquals("Elif", updated.firstName());
        assertEquals(PatientStatus.ACTIVE, updated.status());
        verify(patientRepository, times(2)).save(any(Patient.class));
    }

    @Test
    void submitVerificationSetsPending() {
        when(currentPatientProvider.currentPatientId()).thenReturn("p1");
        when(
            verificationRepository.save(any(), any(PatientVerification.class))
        ).thenAnswer(invocation -> invocation.getArgument(1));

        PatientVerification verification = service.submitVerification(
            new SubmitVerificationCommand(
                "ABC12345",
                "https://example.com/documents/scan-1.pdf",
                "notes"
            )
        );

        assertEquals(VerificationStatus.PENDING, verification.status());
        assertNotNull(verification.submittedAt());
    }

    @Test
    void getVerificationStatusReturnsUnverifiedWhenMissing() {
        when(currentPatientProvider.currentPatientId()).thenReturn("p1");
        when(verificationRepository.findByPatientId("p1")).thenReturn(
            Optional.empty()
        );

        PatientVerification verification = service.getVerificationStatus();

        assertEquals(VerificationStatus.UNVERIFIED, verification.status());
    }
}
