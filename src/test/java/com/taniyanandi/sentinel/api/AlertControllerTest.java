
package com.taniyanandi.sentinel.api;

import com.taniyanandi.sentinel.persistence.AlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertRepository repository;

    @Test
    void shouldReturnAlertCountForEntity() throws Exception {
        when(repository.countByEntityId("merchant-42")).thenReturn(3L);

        mockMvc.perform(get("/v1/alerts/entity/merchant-42/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId").value("merchant-42"))
                .andExpect(jsonPath("$.alertCount").value(3));
    }

    @Test
    void shouldReturnZeroWhenEntityHasNoAlerts() throws Exception {
        when(repository.countByEntityId("merchant-99")).thenReturn(0L);

        mockMvc.perform(get("/v1/alerts/entity/merchant-99/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId").value("merchant-99"))
                .andExpect(jsonPath("$.alertCount").value(0));
    }
}
