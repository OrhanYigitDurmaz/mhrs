package com.mhrs.doctor.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mhrs.auth.domain.UserRole;
import com.mhrs.config.SecurityConfig;
import com.mhrs.doctor.application.DoctorUseCase;
import com.mhrs.doctor.application.command.CreateDoctorCommand;
import com.mhrs.doctor.application.command.CreateScheduleCommand;
import com.mhrs.doctor.application.query.DoctorSearchQuery;
import com.mhrs.doctor.domain.Doctor;
import com.mhrs.doctor.domain.DoctorSchedule;
import com.mhrs.doctor.domain.DoctorSlot;
import com.mhrs.doctor.domain.DoctorStatus;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

@WebMvcTest(controllers = {DoctorController.class, DoctorSearchController.class})
@Import(
    {
        SecurityConfig.class,
        com.mhrs.auth.infrastructure.security.JwtAuthenticationFilter.class,
    }
)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorUseCase doctorUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createDoctorReturnsCreated() throws Exception {
        when(
            doctorUseCase.createDoctor(
                new CreateDoctorCommand(
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
                post("/doctors")
                    .header("Authorization", "Bearer access-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.doctorId").value("d1"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(doctorUseCase).createDoctor(
            new CreateDoctorCommand(
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
    void createDoctorInvalidEmailReturnsBadRequest() throws Exception {
        mockJwt("access-2", "u1", "s1", UserRole.ADMIN, true);
        String body =
            "{" +
            "\"clinicId\":\"clinic-1\"," +
            "\"departmentId\":\"dept-1\"," +
            "\"specialtyId\":\"spec-1\"," +
            "\"firstName\":\"Aylin\"," +
            "\"lastName\":\"Kaya\"," +
            "\"email\":\"invalid\"," +
            "\"status\":\"ACTIVE\"" +
            "}";

        mockMvc
            .perform(
                post("/doctors")
                    .header("Authorization", "Bearer access-2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void createScheduleReturnsCreated() throws Exception {
        when(
            doctorUseCase.createSchedule(
                "d1",
                new CreateScheduleCommand(
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0),
                    20,
                    "Europe/Istanbul"
                )
            )
        ).thenReturn(
            new DoctorSchedule(
                "sch1",
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                20,
                "Europe/Istanbul",
                true
            )
        );

        mockJwt("access-3", "u1", "s1", UserRole.ADMIN, true);

        String body =
            "{" +
            "\"dayOfWeek\":\"MONDAY\"," +
            "\"startTime\":\"09:00\"," +
            "\"endTime\":\"17:00\"," +
            "\"slotMinutes\":20," +
            "\"timezone\":\"Europe/Istanbul\"" +
            "}";

        mockMvc
            .perform(
                post("/doctors/d1/schedules")
                    .header("Authorization", "Bearer access-3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.scheduleId").value("sch1"))
            .andExpect(jsonPath("$.slotMinutes").value(20));
    }

    @Test
    void listSlotsReturnsSlots() throws Exception {
        when(doctorUseCase.listSlots("d1", LocalDate.of(2026, 3, 10)))
            .thenReturn(
                List.of(
                    new DoctorSlot(
                        LocalDate.of(2026, 3, 10),
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 20),
                        true,
                        "Europe/Istanbul"
                    )
                )
            );

        mockJwt("access-4", "u1", "s1", UserRole.PATIENT, true);

        mockMvc
            .perform(
                get("/doctors/d1/slots")
                    .param("date", "2026-03-10")
                    .header("Authorization", "Bearer access-4")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].startTime").value("09:00"))
            .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void searchDoctorsReturnsResults() throws Exception {
        when(
            doctorUseCase.searchDoctors(
                new DoctorSearchQuery(
                    "aylin",
                    null,
                    null,
                    null,
                    null
                )
            )
        ).thenReturn(List.of(mockDoctor()));

        mockJwt("access-5", "u1", "s1", UserRole.ADMIN, true);

        mockMvc
            .perform(
                get("/search/doctors")
                    .param("query", "aylin")
                    .header("Authorization", "Bearer access-5")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].doctorId").value("d1"));
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
