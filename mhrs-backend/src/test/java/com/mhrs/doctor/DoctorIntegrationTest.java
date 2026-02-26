package com.mhrs.doctor;

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
class DoctorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
        new com.fasterxml.jackson.databind.ObjectMapper();

    @Test
    void createAndGetDoctorFlowWorks() throws Exception {
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

        MvcResult result = mockMvc
            .perform(
                post("/doctors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn();

        JsonNode json = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );
        String doctorId = json.get("doctorId").asText();

        mockMvc
            .perform(get("/doctors/{doctorId}", doctorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.doctorId").value(doctorId))
            .andExpect(jsonPath("$.firstName").value("Aylin"));
    }
}
