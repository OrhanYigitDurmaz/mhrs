package com.mhrs.patient.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mhrs.auth.domain.UserRole;
import com.mhrs.config.SecurityConfig;
import com.mhrs.patient.application.PatientUseCase;
import com.mhrs.patient.application.command.SubmitVerificationCommand;
import com.mhrs.patient.application.command.UpdatePatientCommand;
import com.mhrs.patient.application.query.PatientSearchQuery;
import com.mhrs.patient.domain.Gender;
import com.mhrs.patient.domain.Patient;
import com.mhrs.patient.domain.PatientStatus;
import com.mhrs.patient.domain.PatientVerification;
import com.mhrs.patient.domain.VerificationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {PatientController.class, PatientSearchController.class})
@Import(
    {
        SecurityConfig.class,
        com.mhrs.auth.infrastructure.security.JwtAuthenticationFilter.class,
    }
)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientUseCase patientUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void updateMeReturnsOk() throws Exception {
        when(
            patientUseCase.updateMe(
                new UpdatePatientCommand(
                    "Elif",
                    "Yilmaz",
                    "elif@example.com",
                    "+905001112233",
                    LocalDate.of(1990, 5, 10),
                    Gender.FEMALE
                )
            )
        ).thenReturn(mockPatient());

        mockJwt("access-1", "p1", "s1", UserRole.PATIENT, true);

        String body =
            "{" +
            "\"firstName\":\"Elif\"," +
            "\"lastName\":\"Yilmaz\"," +
            "\"email\":\"elif@example.com\"," +
            "\"phone\":\"+905001112233\"," +
            "\"dateOfBirth\":\"1990-05-10\"," +
            "\"gender\":\"FEMALE\"" +
            "}";

        mockMvc
            .perform(
                patch("/patients/me")
                    .header("Authorization", "Bearer access-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.patientId").value("p1"))
            .andExpect(jsonPath("$.gender").value("FEMALE"));

        verify(patientUseCase).updateMe(
            new UpdatePatientCommand(
                "Elif",
                "Yilmaz",
                "elif@example.com",
                "+905001112233",
                LocalDate.of(1990, 5, 10),
                Gender.FEMALE
            )
        );
    }

    @Test
    void updateMeInvalidEmailReturnsBadRequest() throws Exception {
        mockJwt("access-2", "p1", "s1", UserRole.PATIENT, true);
        String body =
            "{" +
            "\"firstName\":\"Elif\"," +
            "\"lastName\":\"Yilmaz\"," +
            "\"email\":\"invalid\"" +
            "}";

        mockMvc
            .perform(
                patch("/patients/me")
                    .header("Authorization", "Bearer access-2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitVerificationReturnsCreated() throws Exception {
        when(
            patientUseCase.submitVerification(
                new SubmitVerificationCommand(
                    "ABC12345",
                    "https://example.com/documents/scan-1.pdf",
                    "notes"
                )
            )
        ).thenReturn(
            new PatientVerification(
                VerificationStatus.PENDING,
                Instant.parse("2026-01-03T09:00:00Z"),
                null,
                "notes"
            )
        );

        mockJwt("access-3", "p1", "s1", UserRole.PATIENT, true);

        String body =
            "{" +
            "\"identityNumber\":\"ABC12345\"," +
            "\"documentUrl\":\"https://example.com/documents/scan-1.pdf\"," +
            "\"notes\":\"notes\"" +
            "}";

        mockMvc
            .perform(
                post("/patients/me/verification/submit")
                    .header("Authorization", "Bearer access-3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getMeReturnsPatient() throws Exception {
        when(patientUseCase.getMe()).thenReturn(mockPatient());
        mockJwt("access-4", "p1", "s1", UserRole.PATIENT, true);

        mockMvc
            .perform(
                get("/patients/me")
                    .header("Authorization", "Bearer access-4")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.patientId").value("p1"));
    }

    @Test
    void searchPatientsReturnsResults() throws Exception {
        when(patientUseCase.searchPatients(new PatientSearchQuery("elif", null)))
            .thenReturn(List.of(mockPatient()));
        mockJwt("access-5", "p1", "s1", UserRole.ADMIN, true);

        mockMvc
            .perform(
                get("/search/patients")
                    .param("query", "elif")
                    .header("Authorization", "Bearer access-5")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].patientId").value("p1"));
    }

    private Patient mockPatient() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new Patient(
            "p1",
            "Elif",
            "Yilmaz",
            "elif@example.com",
            "+905001112233",
            LocalDate.of(1990, 5, 10),
            Gender.FEMALE,
            PatientStatus.ACTIVE,
            now,
            now
        );
    }

    private void mockJwt(
        String token,
        String userId,
        String sessionId,
        UserRole role,
        boolean emailVerified
    ) {
        Jwt jwt = Jwt.withTokenValue(token)
            .header("alg", "HS256")
            .subject(userId)
            .claim("email", "user@example.com")
            .claim("role", role.name())
            .claim("email_verified", emailVerified)
            .claim("sid", sessionId)
            .build();
        when(jwtDecoder.decode(token)).thenReturn(jwt);
    }
}
