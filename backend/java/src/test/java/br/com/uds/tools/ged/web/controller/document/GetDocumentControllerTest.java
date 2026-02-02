package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentResponse;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GetDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class GetDocumentControllerTest {

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentFacade documentFacade;

    @Test
    @WithMockUser
    void getById_returns200AndDocument() throws Exception {
        DocumentResponse doc = new DocumentResponse(1L, "Doc", "Desc", List.of(), 1L, "Owner", DocumentStatus.PUBLISHED, null, null, true);
        when(documentFacade.findById(1L)).thenReturn(doc);

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Doc"));

        verify(documentFacade).findById(1L);
    }
}
