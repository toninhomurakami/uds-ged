package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.facade.DocumentFacade;
import br.com.uds.tools.ged.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreateDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CreateDocumentControllerTest {

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentFacade documentFacade;

    @Test
    @WithMockUser
    void create_whenValid_returns201() throws Exception {
        DocumentResponse created = new DocumentResponse(1L, "Doc", "Desc", List.of(), 1L, "Owner", DocumentStatus.DRAFT, null, null, false);
        when(documentFacade.create(any(DocumentRequest.class))).thenReturn(created);

        DocumentRequest request = new DocumentRequest();
        request.setTitle("Doc");
        request.setDescription("Desc");

        mockMvc.perform(post("/api/documents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Doc"));

        verify(documentFacade).create(any(DocumentRequest.class));
    }
}
