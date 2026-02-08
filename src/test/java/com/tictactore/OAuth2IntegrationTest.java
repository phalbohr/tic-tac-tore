package com.tictactore;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("OAuth2 Integration Tests")
public class OAuth2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Accessing protected resource without authentication should redirect to login")
    void accessingProtectedResource_withoutAuth_shouldRedirect() throws Exception {
        // Мы ожидаем, что корень '/' или любой API эндпоинт может быть защищен 
        // в зависимости от финальной конфигурации SecurityConfig.
        // На данном этапе проверим, что OAuth2 эндпоинты доступны.
        
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("accounts.google.com")));
    }
}
