package com.mhrs.admin.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mhrs.admin.application.AdminDoctorUseCase;
import com.mhrs.admin.application.command.AdminCreateDoctorCommand;
import com.mhrs.auth.domain.UserRole;
import com.mhrs.config.SecurityConfig;
import com.mhrs.doctor.domain.Doctor;
import com.mhrs.doctor.domain.DoctorStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AdminDoctorController.class})
@Import(
    {
        SecurityConfig.class,
        com.mhrs.auth.infrastructure.security.JwtAuthenticationFilter.class,
    }
)
class AdminDoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminDoctorUseCase adminDoctorUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createDoctorReturnsCreated() throws Exception {
        when(
            adminDoctorUseCase.createDoctor(
                new AdminCreateDoctorCommand(
                    "clinic-1",
                    "dept-1",
                    "spec-1",
                    "Aylin",
                    "Kaya",
                    "Dr.",
                    "aylin@example.com",
                    "+905001112233",
                    DoctorStatus.ACTIVE
                )
            )
        ).thenReturn(mockDoctor());

        mockJwt("access-1", "u1", "s1", UserRole.ADMIN, true);

        String body =
            "{" +
            "\"clinicId\":\"clinic-1\"," +
            "\"departmentId\":\"dept-1\"," +
            "\"specialtyId\":\"spec-1\"," +
            "\"firstName\":\"Aylin\"," +
            "\"lastName\":\"Kaya\"," +
            "\"title\":\"Dr.\"," +
            "\"email\":\"aylin@example.com\"," +
            "\"phone\":\"+905001112233\"," +
            "\"status\":\"ACTIVE\"" +
            "}";

        mockMvc
            .perform(
                post("/admin/doctors")
                    .header("Authorization", "Bearer access-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.doctorId").value("d1"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(adminDoctorUseCase).createDoctor(
            new AdminCreateDoctorCommand(
                "clinic-1",
                "dept-1",
                "spec-1",
                "Aylin",
                "Kaya",
                "Dr.",
                "aylin@example.com",
                "+905001112233",
                DoctorStatus.ACTIVE
            )
        );
    }

    @Test
    void removeDoctorReturnsNoContent() throws Exception {
        mockJwt("access-2", "u1", "s1", UserRole.ADMIN, true);

        mockMvc
            .perform(
                delete("/admin/doctors/{doctorId}", "d1")
                    .header("Authorization", "Bearer access-2")
            )
            .andExpect(status().isNoContent());

        verify(adminDoctorUseCase).removeDoctor("d1");
    }

    private Doctor mockDoctor() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new Doctor(
            "d1",
            "clinic-1",
            "dept-1",
            "spec-1",
            "Aylin",
            "Kaya",
            "Dr.",
            "aylin@example.com",
            "+905001112233",
            DoctorStatus.ACTIVE,
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
