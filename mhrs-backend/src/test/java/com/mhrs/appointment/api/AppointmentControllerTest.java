package com.mhrs.appointment.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mhrs.appointment.application.AppointmentUseCase;
import com.mhrs.appointment.application.command.CancelAppointmentCommand;
import com.mhrs.appointment.application.command.CreateAppointmentCommand;
import com.mhrs.appointment.application.command.RescheduleAppointmentCommand;
import com.mhrs.appointment.application.query.AppointmentSearchQuery;
import com.mhrs.appointment.domain.Appointment;
import com.mhrs.appointment.domain.AppointmentStatus;
import com.mhrs.auth.domain.UserRole;
import com.mhrs.config.SecurityConfig;
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

@WebMvcTest(controllers = {AppointmentController.class})
@Import(
    {
        SecurityConfig.class,
        com.mhrs.auth.infrastructure.security.JwtAuthenticationFilter.class,
    }
)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentUseCase appointmentUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createAppointmentReturnsCreated() throws Exception {
        when(
            appointmentUseCase.createAppointment(
                new CreateAppointmentCommand(
                    "p1",
                    "d1",
                    "c1",
                    "dep1",
                    "sp1",
                    LocalDate.of(2026, 3, 10),
                    LocalTime.of(9, 0),
                    LocalTime.of(9, 20),
                    "Routine checkup",
                    "Bring previous reports"
                )
            )
        ).thenReturn(mockAppointment(AppointmentStatus.PENDING));

        mockJwt("access-1", "u1", "s1", UserRole.PATIENT, true);

        String body =
            "{" +
            "\"patientId\":\"p1\"," +
            "\"doctorId\":\"d1\"," +
            "\"clinicId\":\"c1\"," +
            "\"departmentId\":\"dep1\"," +
            "\"specialtyId\":\"sp1\"," +
            "\"date\":\"2026-03-10\"," +
            "\"startTime\":\"09:00\"," +
            "\"endTime\":\"09:20\"," +
            "\"reason\":\"Routine checkup\"," +
            "\"notes\":\"Bring previous reports\"" +
            "}";

        mockMvc
            .perform(
                post("/appointments")
                    .header("Authorization", "Bearer access-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.appointmentId").value("a1"))
            .andExpect(jsonPath("$.status").value("PENDING"));

        verify(appointmentUseCase).createAppointment(
            new CreateAppointmentCommand(
                "p1",
                "d1",
                "c1",
                "dep1",
                "sp1",
                LocalDate.of(2026, 3, 10),
                LocalTime.of(9, 0),
                LocalTime.of(9, 20),
                "Routine checkup",
                "Bring previous reports"
            )
        );
    }

    @Test
    void listAppointmentsReturnsResults() throws Exception {
        when(
            appointmentUseCase.listAppointments(
                new AppointmentSearchQuery(
                    "p1",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        ).thenReturn(List.of(mockAppointment(AppointmentStatus.CONFIRMED)));

        mockJwt("access-2", "u1", "s1", UserRole.ADMIN, true);

        mockMvc
            .perform(
                get("/appointments")
                    .param("patientId", "p1")
                    .header("Authorization", "Bearer access-2")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].appointmentId").value("a1"))
            .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void cancelAppointmentReturnsOk() throws Exception {
        when(
            appointmentUseCase.cancelAppointment(
                "a1",
                new CancelAppointmentCommand("Patient requested cancellation")
            )
        ).thenReturn(mockAppointment(AppointmentStatus.CANCELLED));

        mockJwt("access-3", "u1", "s1", UserRole.PATIENT, true);

        String body = "{\"reason\":\"Patient requested cancellation\"}";

        mockMvc
            .perform(
                patch("/appointments/a1/cancel")
                    .header("Authorization", "Bearer access-3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.appointmentId").value("a1"))
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void rescheduleAppointmentReturnsOk() throws Exception {
        when(
            appointmentUseCase.rescheduleAppointment(
                "a1",
                new RescheduleAppointmentCommand(
                    LocalDate.of(2026, 3, 12),
                    LocalTime.of(10, 0),
                    LocalTime.of(10, 20),
                    "Scheduling conflict"
                )
            )
        ).thenReturn(mockAppointment(AppointmentStatus.RESCHEDULED));

        mockJwt("access-4", "u1", "s1", UserRole.PATIENT, true);

        String body =
            "{" +
            "\"date\":\"2026-03-12\"," +
            "\"startTime\":\"10:00\"," +
            "\"endTime\":\"10:20\"," +
            "\"reason\":\"Scheduling conflict\"" +
            "}";

        mockMvc
            .perform(
                patch("/appointments/a1/reschedule")
                    .header("Authorization", "Bearer access-4")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.appointmentId").value("a1"))
            .andExpect(jsonPath("$.status").value("RESCHEDULED"));
    }

    private Appointment mockAppointment(AppointmentStatus status) {
        Instant now = Instant.parse("2026-03-01T10:00:00Z");
        return new Appointment(
            "a1",
            "p1",
            "d1",
            "c1",
            "dep1",
            "sp1",
            LocalDate.of(2026, 3, 10),
            LocalTime.of(9, 0),
            LocalTime.of(9, 20),
            status,
            "Routine checkup",
            "Bring previous reports",
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
