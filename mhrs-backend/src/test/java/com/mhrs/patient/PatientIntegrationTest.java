package com.mhrs.patient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mhrs.TestcontainersConfiguration;
import com.mhrs.patient.application.port.out.PatientRepository;
import com.mhrs.patient.domain.Gender;
import com.mhrs.patient.domain.Patient;
import com.mhrs.patient.domain.PatientStatus;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnabledIfEnvironmentVariable(named = "ENABLE_TESTCONTAINERS", matches = "true")
class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        patientRepository.save(
            new Patient(
                "p1",
                "Elif",
                "Yilmaz",
                "elif@example.com",
                "+905001112233",
                LocalDate.of(1990, 5, 10),
                Gender.FEMALE,
                PatientStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
            )
        );
    }

    @Test
    void listPatientsReturnsData() throws Exception {
        mockMvc
            .perform(get("/patients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].patientId").value("p1"))
            .andExpect(jsonPath("$[0].firstName").value("Elif"));
    }
}
