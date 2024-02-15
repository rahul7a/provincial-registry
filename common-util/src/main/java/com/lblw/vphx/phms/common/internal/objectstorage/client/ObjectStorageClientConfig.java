package com.lblw.vphx.phms.common.internal.objectstorage.client;

import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ObjectStorageClientConfig {
  public static final String OBJECT_STORAGE_WEB_CLIENT_QUALIFIER = "objectStorageWebClient";
  private final InternalApiConfig apiConfig;

  public ObjectStorageClientConfig(InternalApiConfig apiConfig) {
    this.apiConfig = apiConfig;
  }

  /**
   * Bean definition for qualified ObjectStorage WebClient.Builder
   *
   * @return a WebClient {@link WebClient} configured for ObjectStorage base URL
   */
  @Bean
  @Qualifier(value = OBJECT_STORAGE_WEB_CLIENT_QUALIFIER)
  public WebClient getObjectStorageWebClient(InternalApiConfig internalApiConfig) {
    // timeout configuration
    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, internalApiConfig.getConnectTimeout())
            .responseTimeout(Duration.ofMillis(internalApiConfig.getResponseTimeout()))
            .doOnConnected(
                conn ->
                    conn.addHandlerLast(
                            new ReadTimeoutHandler(
                                internalApiConfig.getResponseTimeout(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(
                            new WriteTimeoutHandler(
                                internalApiConfig.getWriteTimeout(), TimeUnit.MILLISECONDS)));

    return WebClient.builder()
        .baseUrl(apiConfig.getObjectStorage().getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}
