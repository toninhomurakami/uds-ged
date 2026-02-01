package br.com.uds.tools.ged.service;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.repository.DocumentVersionRepository;
import br.com.uds.tools.ged.repository.UserRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import br.com.uds.tools.ged.storage.FileStorageService;
import br.com.uds.tools.ged.dto.UserRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Nome de usuário já existe");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword( passwordEncoder.encode(request.getPassword()) );
        user.setRole(request.getRole());
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Nome de usuário já existe");
        }
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    /**
     * Cria o primeiro usuário ADMIN (setup inicial). Só permite quando não existe nenhum usuário.
     */
    @Transactional
    public UserResponse createInitialAdmin(String name, String username, String password) {
        if (userRepository.count() > 0) {
            throw new IllegalStateException("Setup inicial já foi realizado. Já existem usuários cadastrados.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nome de usuário já existe");
        }
        User admin = new User();
        admin.setName(name != null ? name.trim() : null);
        admin.setUsername(username.trim());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);
        return toResponse(admin);
    }

    /**
     * Exclui o usuário, todos os documentos (e versões) dele e os arquivos físicos associados.
     * Não permite excluir o próprio usuário logado.
     */
    @Transactional
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
                    fileStorageService.deleteByFileKey(version.getFileKey());
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
                fileStorageService.deleteByFileKey(version.getFileKey());
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

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getRole());
    }
}
