package com.lblw.vphx.phms.common.internal.dataprotection.client;

import static com.lblw.vphx.phms.common.internal.dataprotection.configuration.DataProtectionClientConfiguration.DATA_PROTECTION_WEB_CLIENT_QUALIFIER;

import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.common.security.services.OAuthClientService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * A client for the Data Protection API
 *
 * @see <a
 *     href="https://vph-lwr.banting.lblw.cloud/dev/dataprotection/webjars/swagger-ui/index.html?configUrl=/dev/dataprotection/v3/api-docs/swagger-config">Data
 *     Protection Service</a>
 */
@Component
public class DataProtectionClient {
  private final WebClient webClient;
  private final InternalApiConfig internalApiConfig;
  private final OAuthClientService oAuthClientService;

  /**
   * A constructor
   *
   * @param webClient WebClient configured for Data Protection REST APIs
   * @param internalApiConfig {@link InternalApiConfig}
   * @param oAuthClientService {@link OAuthClientService}
   */
  public DataProtectionClient(
      @Qualifier(DATA_PROTECTION_WEB_CLIENT_QUALIFIER) WebClient webClient,
      InternalApiConfig internalApiConfig,
      OAuthClientService oAuthClientService) {
    this.webClient = webClient;
    this.internalApiConfig = internalApiConfig;
    this.oAuthClientService = oAuthClientService;
  }

  /**
   * Takes a blob of text in Base64, sends it to the deidentification service, and returns the
   * deidentified blob in Base64
   *
   * @see <a
   *     href="https://vph-lwr.banting.lblw.cloud/dev/dataprotection/webjars/swagger-ui/index.html?configUrl=/dev/dataprotection/v3/api-docs/swagger-config#/deidentify-api-controller/deidentifyBlob">Deidentify
   *     Blob</a>
   * @param blob The blob to deidentify.
   * @return A Mono<String> of deidentified blob
   */
  public Mono<String> deidentifyBlob(String blob) {
    return this.oAuthClientService
        .getJWT()
        .flatMap(
            jwt ->
                webClient
                    .post()
                    .uri(internalApiConfig.getDps().getPaths().getDeidentifyBlobPath())
                    .header(HttpHeaders.AUTHORIZATION, CommonConstants.BEARER.concat(jwt))
                    .contentType(MediaType.TEXT_PLAIN)
                    .accept(MediaType.TEXT_PLAIN)
                    .bodyValue(blob)
                    .retrieve()
                    .bodyToMono(String.class));
  }

  /**
   * Takes a blob of text in Base64, sends it to the reidentification service, and returns the
   * reidentified blob in Base64
   *
   * @see <a
   *     href="https://vph-lwr.banting.lblw.cloud/dev/dataprotection/webjars/swagger-ui/index.html?configUrl=/dev/dataprotection/v3/api-docs/swagger-config#/reidentify-api-controller/reidentifyBlob">Reidentify
   *     Blob</a>
   * @param blob The blob to reidentify.
   * @return A Mono<String> of reidentified blob
   */
  public Mono<String> reidentifyBlob(String blob) {
    return this.oAuthClientService
        .getJWT()
        .flatMap(
            jwt ->
                webClient
                    .post()
                    .uri(internalApiConfig.getDps().getPaths().getReidentifyBlobPath())
                    .header(HttpHeaders.AUTHORIZATION, CommonConstants.BEARER.concat(jwt))
                    .contentType(MediaType.TEXT_PLAIN)
                    .accept(MediaType.TEXT_PLAIN)
                    .bodyValue(blob)
                    .retrieve()
                    .bodyToMono(String.class));
  }
}
