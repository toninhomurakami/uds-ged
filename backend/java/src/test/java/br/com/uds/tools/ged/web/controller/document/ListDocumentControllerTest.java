package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import br.com.uds.tools.ged.facade.DocumentFacade;
import br.com.uds.tools.ged.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListDocumentControllerTest {

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentFacade documentFacade;

    @Test
    @WithMockUser
    void list_returns200AndPage() throws Exception {
        PageResponse<DocumentResponse> page = new PageResponse<>(List.of(), 0, 10, 0, 0, true, true);
        when(documentFacade.findAll(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/documents")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(documentFacade).findAll(any(), any(), any());
    }

    @Test
    @WithMockUser
    void listVersions_returns200AndList() throws Exception {
        when(documentFacade.listVersions(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/documents/1/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(documentFacade).listVersions(1L);
    }
}
