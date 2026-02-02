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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UpdateDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class UpdateDocumentControllerTest {

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
    void update_whenValid_returns200() throws Exception {
        DocumentResponse updated = new DocumentResponse(1L, "New Title", "Desc", List.of(), 1L, "Owner", DocumentStatus.DRAFT, null, null, false);
        when(documentFacade.update(eq(1L), any(DocumentRequest.class))).thenReturn(updated);

        DocumentRequest request = new DocumentRequest();
        request.setTitle("New Title");
        request.setDescription("Desc");

        mockMvc.perform(put("/api/documents/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));

        verify(documentFacade).update(eq(1L), any(DocumentRequest.class));
    }

    @Test
    @WithMockUser
    void publish_returns200() throws Exception {
        DocumentResponse published = new DocumentResponse(1L, "Doc", "Desc", List.of(), 1L, "Owner", DocumentStatus.PUBLISHED, null, null, true);
        when(documentFacade.publish(1L)).thenReturn(published);

        mockMvc.perform(put("/api/documents/1/publish").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(documentFacade).publish(1L);
    }

    @Test
    @WithMockUser
    void archive_returns200() throws Exception {
        DocumentResponse archived = new DocumentResponse(1L, "Doc", "Desc", List.of(), 1L, "Owner", DocumentStatus.ARCHIVED, null, null, true);
        when(documentFacade.archive(1L)).thenReturn(archived);

        mockMvc.perform(put("/api/documents/1/archive").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        verify(documentFacade).archive(1L);
    }
}
