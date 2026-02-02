package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.facade.FileStorageFacade;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.repository.DocumentVersionRepository;
import br.com.uds.tools.ged.repository.UserRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteByIdUserService extends AbstractUserService {


    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final FileStorageFacade fileStorageFacade;

    /**
     * Exclui o usuário, todos os documentos (e versões) dele e os arquivos físicos associados.
     * Não permite excluir o próprio usuário logado.
     */
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        UserPrincipal current = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (current.getId().equals(id)) {
            throw new IllegalArgumentException("Não é possível excluir o próprio usuário.");
        }
        List<Document> documents = documentRepository.findByOwnerId(id);
        for (Document doc : documents) {
            for (DocumentVersion version : doc.getVersions()) {
                try {
                    fileStorageFacade.deleteByFileKey(version.getFileKey());
                } catch (IOException e) {
                    System.err.println(
                            String.format("Ocorreu um erro ao excluir o arquivo %s, porém o sistema continuará a buscar os próximos, se houverem -> %s",
                                    version.getFileKey(),
                                    e.getMessage())
                    );
                }
            }
        }
        documentRepository.deleteAll(documents);
        List<DocumentVersion> versionsUploadedByUser = documentVersionRepository.findByUploadedById(id);
        for (DocumentVersion version : versionsUploadedByUser) {
            try {
                fileStorageFacade.deleteByFileKey(version.getFileKey());
            } catch (IOException e) {
                System.err.println(
                        String.format("Ocorreu um erro ao excluir o arquivo %s, porém o sistema continuará a buscar os próximos, se houverem -> %s",
                                version.getFileKey(),
                                e.getMessage())
                );
            }
        }
        documentVersionRepository.deleteAll(versionsUploadedByUser);
        userRepository.delete(user);
    }
}
