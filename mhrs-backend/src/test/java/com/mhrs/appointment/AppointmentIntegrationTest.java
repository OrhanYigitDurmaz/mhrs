package com.mhrs.appointment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.mhrs.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnabledIfEnvironmentVariable(named = "ENABLE_TESTCONTAINERS", matches = "true")
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
        new com.fasterxml.jackson.databind.ObjectMapper();

    @Test
    void createAndGetAppointmentFlowWorks() throws Exception {
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

        MvcResult result = mockMvc
            .perform(
                post("/appointments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();

        JsonNode json = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );
        String appointmentId = json.get("appointmentId").asText();

        mockMvc
            .perform(get("/appointments/{id}", appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.appointmentId").value(appointmentId))
            .andExpect(jsonPath("$.doctorId").value("d1"))
            .andExpect(jsonPath("$.patientId").value("p1"));
    }
}
