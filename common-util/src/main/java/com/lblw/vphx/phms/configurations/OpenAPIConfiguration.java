package com.lblw.vphx.phms.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * OpenAPI specification for Swagger
 *
 * <p>For configuration
 *
 * @see <a href="https://swagger.io/docs/specification/authentication/bearer-authentication/">
 *     Swagger Bearer Authentication Configuration</a>
 */
@Configuration
public class OpenAPIConfiguration {
  public static final String OPEN_API_SECURITY_KEY = "access_token";
  private static final String OPEN_API_SCHEME = "bearer";
  private static final String OPEN_API_BEARER_FORMAT = "JWT";
  private static final String OPEN_API_NAME = "Authorization";
  private static final String OPEN_API_DESCRIPTION =
      "Requires token with environment specific credentials";

  @Primary
  @Bean("phmsOpenAPIConfiguration")
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(
            new Components()
                .addSecuritySchemes(
                    OPEN_API_SECURITY_KEY,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(OPEN_API_SCHEME)
                        .name(OPEN_API_NAME)
                        .description(OPEN_API_DESCRIPTION)
                        .bearerFormat(OPEN_API_BEARER_FORMAT)));
  }
}
