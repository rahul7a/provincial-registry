package com.lblw.vphx.phms.common.internal.dataprotection.configuration;

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

/**
 * WebClient Configuration for Data Protection Service
 *
 * @see <a
 *     href="https://vph-lwr.banting.lblw.cloud/dev/dataprotection/webjars/swagger-ui/index.html?configUrl=/dev/dataprotection/v3/api-docs/swagger-config">Data
 *     Protection Service</a>
 */
@Configuration
public class DataProtectionClientConfiguration {

  public static final String DATA_PROTECTION_WEB_CLIENT_QUALIFIER = "dataProtectionWebClient";

  /**
   * Bean definition for qualified Data Protection WebClient
   *
   * @return a WebClient configured for DataProtection base URL
   */
  @Bean
  @Qualifier(value = DATA_PROTECTION_WEB_CLIENT_QUALIFIER)
  public WebClient getDataProtectionWebClient(InternalApiConfig internalApiConfig) {
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
        .baseUrl(internalApiConfig.getDps().getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}
