-- Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    username VARCHAR(15) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Documents
CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    owner_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_document_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE document_tags (
    document_id BIGINT NOT NULL,
    tag VARCHAR(25) NOT NULL,
    PRIMARY KEY (document_id, tag),
    CONSTRAINT fk_tags_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

-- Document versions
CREATE TABLE document_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    file_key VARCHAR(512) NOT NULL,
    uploaded_at TIMESTAMP(6) NOT NULL,
    uploaded_by_id BIGINT NOT NULL,
    CONSTRAINT fk_version_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_version_uploaded_by FOREIGN KEY (uploaded_by_id) REFERENCES users(id)
);

CREATE INDEX idx_documents_owner_status ON documents(owner_id, status);
CREATE INDEX idx_documents_updated_at ON documents(updated_at DESC);
CREATE INDEX idx_document_versions_document_id ON document_versions(document_id);
