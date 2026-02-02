package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.facade.DocumentFacade;
import br.com.uds.tools.ged.security.JwtService;
import br.com.uds.tools.ged.service.FileDownload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DownloadDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DownloadDocumentControllerTest {

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentFacade documentFacade;

    @Test
    @WithMockUser
    void downloadCurrent_returns200WithContent() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        FileDownload download = new FileDownload(content, "doc.pdf");
        when(documentFacade.downloadCurrent(1L)).thenReturn(download);

        mockMvc.perform(get("/api/documents/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(content));

        verify(documentFacade).downloadCurrent(1L);
    }

    @Test
    @WithMockUser
    void downloadVersion_returns200WithContent() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        FileDownload download = new FileDownload(content, "doc-v2.pdf");
        when(documentFacade.downloadVersion(1L, 10L)).thenReturn(download);

        mockMvc.perform(get("/api/documents/1/versions/10/download"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(content));

        verify(documentFacade).downloadVersion(1L, 10L);
    }
}
