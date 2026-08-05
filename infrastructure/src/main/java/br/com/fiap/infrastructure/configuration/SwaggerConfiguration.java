package br.com.fiap.infrastructure.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

    @Value("${auth.lambda.url:}")
    private String authLambdaUrl = "";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/").description("Local — http://localhost:8087")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT obtido via POST /auth/login. Informe: Bearer <token>")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .info(new Info()
                        .title("FIAP X - Notification MS")
                        .version("1.0.0")
                        .description("""
                                FIAP - 14 SOAT - Arquitetura de Software (Turma Outubro de 2025)
                                Tech Challenge - Fase 5 (Hackathon)

                                Worker assíncrono — consome SQS video-events e envia notificações
                                por e-mail para eventos VIDEO_FAILED e VIDEO_PROCESSED.

                                **Como autenticar:**
                                1. Expanda a secao **Authentication** abaixo
                                2. Execute `POST /auth/login` com suas credenciais
                                3. Copie o `token` da resposta
                                4. Clique em **Authorize** (🔒) e informe: `Bearer <token>`
                                """));
    }

    @Bean
    public OpenApiCustomizer authLoginServerOverride() {
        return openApi -> {
            if (!StringUtils.hasText(authLambdaUrl) || openApi.getPaths() == null) {
                return;
            }
            var authPath = openApi.getPaths().get("/auth/login");
            if (authPath != null) {
                authPath.servers(List.of(new Server().url(authLambdaUrl).description("Auth Lambda — API Gateway")));
            }
        };
    }
}
