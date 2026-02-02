package br.com.uds.tools.ged.facade;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageFacade {


    void deleteByFileKey(String fileKey) throws IOException;

    String store(Long documentId, Long versionId, MultipartFile file) throws IOException;

    Path resolve(String fileKey);
}
