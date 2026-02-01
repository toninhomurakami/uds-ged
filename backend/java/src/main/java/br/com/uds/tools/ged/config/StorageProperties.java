package br.com.uds.tools.ged.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "ged.storage")
@Data
public class StorageProperties {

    private String basePath = "/var/ged/data/storage";

    public Path getBasePathAsPath() {
        return Paths.get(basePath).toAbsolutePath().normalize();
    }
}
