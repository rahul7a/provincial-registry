package com.lblw.vphx.phms.common.province.config;

import com.lblw.vphx.phms.common.exceptions.ProvincialProcessorException;
import com.lblw.vphx.phms.common.security.services.SecurityService;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** Configuration for a WebClient instance specific for provincial calls */
@Slf4j
@Configuration
public class ProvincialWebClientConfiguration {

  private static final String SECURITY_SERVICE_ERROR_MESSAGE =
      "Exception occurred while calling SecurityService.";
  private static final String EXCEPTION_FROM_SECURITY_SERVICE =
      "Exception being thrown from SecurityService.";

  /**
   * Provides a WebClient that uses a custom SSLContext and sets the connection, read, and write
   * timeouts from application properties
   *
   * @param securityService This is a service that provides the SSL context for the web client.
   * @param properties {@link ProvincialRequestProperties} Properties configuration for the
   *     application
   * @return A WebClient object.
   */
  @Bean
  @Qualifier("provincial")
  public WebClient webClient(
      SecurityService securityService, ProvincialRequestProperties properties) {

    SslContext sslContext;
    try {
      sslContext = securityService.getSslContext();
    } catch (UnrecoverableKeyException
        | CertificateException
        | KeyStoreException
        | IOException
        | NoSuchAlgorithmException ex) {
      log.error(SECURITY_SERVICE_ERROR_MESSAGE, ex);
      throw new ProvincialProcessorException(EXCEPTION_FROM_SECURITY_SERVICE + ex);
    }

    SslContext finalSslContext = sslContext;
    var clientConnector =
        new ReactorClientHttpConnector(
            HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(finalSslContext))
                .responseTimeout(
                    Duration.ofMillis(properties.getRequest().getWebclient().getResponseTimeOut()))
                .option(
                    ChannelOption.CONNECT_TIMEOUT_MILLIS,
                    properties.getRequest().getWebclient().getConnectionTimeOut())
                .doOnConnected(
                    connection ->
                        connection
                            .addHandlerLast(
                                new ReadTimeoutHandler(
                                    properties.getRequest().getWebclient().getReadTimeOut(),
                                    TimeUnit.MILLISECONDS))
                            .addHandlerLast(
                                new WriteTimeoutHandler(
                                    properties.getRequest().getWebclient().getWriteTimeOut(),
                                    TimeUnit.MILLISECONDS))));

    return WebClient.builder()
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(
                    clientCodecConfigurer ->
                        clientCodecConfigurer
                            .defaultCodecs()
                            .maxInMemorySize(
                                properties
                                    .getRequest()
                                    .getWebclient()
                                    .getResponseMemoryLimitInBytes()))
                .build())
        .clientConnector(clientConnector)
        .build();
  }
}
