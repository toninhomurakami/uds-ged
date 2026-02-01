package br.com.uds.tools.ged.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GED API")
                        .description("API REST do sistema GED (Gerenciamento Eletrônico de Documentos). Autenticação via JWT: use POST /api/auth/login para obter o token e clique em 'Authorize' para informá-lo.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Informe o token JWT obtido em POST /api/auth/login")));
    }
}
