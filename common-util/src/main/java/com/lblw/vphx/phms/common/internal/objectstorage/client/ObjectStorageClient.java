package com.lblw.vphx.phms.common.internal.objectstorage.client;

import static com.lblw.vphx.phms.common.internal.objectstorage.client.ObjectStorageClientConfig.OBJECT_STORAGE_WEB_CLIENT_QUALIFIER;

import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ObjectStorageClient {
  private final InternalApiConfig apiConfig;
  private final WebClient webClient;

  public ObjectStorageClient(
      InternalApiConfig apiConfig,
      @Qualifier(OBJECT_STORAGE_WEB_CLIENT_QUALIFIER) WebClient webClient) {
    this.apiConfig = apiConfig;
    this.webClient = webClient;
  }

  /**
   * This method write certificate to the respective file location when it does not exist or expired
   *
   * @param certificateReferenceId {@link String}
   * @param jwt {@link String} jwt access token use for Authorization Header while calling
   * @return Mono of byte array {@link Mono} as raw certificate data
   */
  public Mono<byte[]> getCertificate(String certificateReferenceId, String jwt) {
    return webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(apiConfig.getObjectStorage().getObjectStorePath())
                    .queryParam(CommonConstants.ORIGIN, CommonConstants.CERTIFICATE_STORE)
                    .build(certificateReferenceId))
        .header(HttpHeaders.AUTHORIZATION, CommonConstants.BEARER.concat(jwt))
        .accept(MediaType.APPLICATION_OCTET_STREAM)
        .retrieve()
        .bodyToMono(byte[].class);
  }
}
