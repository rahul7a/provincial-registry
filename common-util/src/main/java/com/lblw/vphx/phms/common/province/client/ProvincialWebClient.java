package com.lblw.vphx.phms.common.province.client;

import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.exceptions.ProvincialProcessorException;
import com.lblw.vphx.phms.common.exceptions.ProvincialTimeoutException;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
/** This class is used to retrieve information of Provincial WebClient */
public class ProvincialWebClient {
  private static final String PROVINCIAL_ERROR_MESSAGE =
      "Exception occurred when calling Provincial system. Status code:";
  private final WebClient webClient;

  public ProvincialWebClient(@Qualifier("provincial") WebClient webClient) {
    this.webClient = webClient;
  }

  /**
   * Post soap request to Province.
   *
   * @param uri of Province
   * @param headers specific to each transaction type
   * @param soapRequest signed HL7 SOAP request
   * @return {@link Mono} of {@link ResponseEntity} with HL7 SOAP response
   */
  public Mono<ResponseEntity<String>> callClientRegistry(
      String uri, HttpHeaders headers, String soapRequest) {
    return this.webClient
        .post()
        .uri(uri)
        .contentType(MediaType.TEXT_XML)
        .headers(httpHeaders -> httpHeaders.putAll(headers))
        .body(Mono.just(soapRequest), String.class)
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),
            response ->
                Mono.error(
                    new ProvincialProcessorException(
                        PROVINCIAL_ERROR_MESSAGE + response.statusCode())))
        .toEntity(String.class)
        .onErrorMap(
            throwable -> {
              var cause = throwable.getCause();
              log.error(ErrorConstants.EXCEPTION_OCCURRED_WHILE_CALLING_PROVINCE, cause);
              return (cause instanceof ReadTimeoutException
                      || cause instanceof WriteTimeoutException
                      || cause instanceof ConnectTimeoutException)
                  ? new ProvincialTimeoutException(cause.getMessage(), throwable)
                  : new ProvincialProcessorException(
                      ErrorConstants.EXCEPTION_OCCURRED_WHILE_CALLING_PROVINCE, throwable);
            })
        .subscribeOn(Schedulers.boundedElastic());
  }
}
