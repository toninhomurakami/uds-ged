package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.facade.DocumentFacade;
import br.com.uds.tools.ged.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class UploadDocumentControllerTest {

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentFacade documentFacade;

    @Test
    @WithMockUser
    void uploadVersion_whenValid_returns201() throws Exception {
        DocumentVersionResponse version = new DocumentVersionResponse(10L, "1/10/file.pdf", Instant.now(), 1L, "User");
        when(documentFacade.uploadVersion(eq(1L), any(MultipartFile.class))).thenReturn(version);

        mockMvc.perform(multipart("/api/documents/1/versions")
                        .file("file", "content".getBytes())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(documentFacade).uploadVersion(eq(1L), any(MultipartFile.class));
    }
}
