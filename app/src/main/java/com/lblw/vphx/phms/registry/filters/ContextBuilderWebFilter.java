package com.lblw.vphx.phms.registry.filters;

import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.*;
import static com.lblw.vphx.phms.registry.configurations.MessageLogConfig.COMPRESSION_ENABLED_MESSAGE_PROCESS;
import static com.lblw.vphx.phms.registry.configurations.MessageLogConfig.ENCRYPTION_ENABLED_MESSAGE_PROCESS;

import com.lblw.vphx.phms.common.constants.APIConstants;
import com.lblw.vphx.phms.common.constants.HeaderConstants;
import com.lblw.vphx.phms.common.constants.MessageLogsConstants;
import com.lblw.vphx.phms.common.utils.ControllerUtils;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplate;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.ServiceCode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@Order
@Slf4j
public class ContextBuilderWebFilter implements WebFilter {
  private final ControllerUtils controllerUtils;
  private final WebFluxProperties webFluxProperties;

  private final ProvincialRequestProperties provincialRequestProperties;
  /**
   * class constructor
   *
   * @param controllerUtils {@link ControllerUtils}
   * @param webFluxProperties {@link WebFluxProperties}
   * @param provincialRequestProperties {@link ProvincialRequestProperties}
   */
  public ContextBuilderWebFilter(
      ControllerUtils controllerUtils,
      WebFluxProperties webFluxProperties,
      ProvincialRequestProperties provincialRequestProperties) {
    this.controllerUtils = controllerUtils;
    this.webFluxProperties = webFluxProperties;
    this.provincialRequestProperties = provincialRequestProperties;
  }

  @Override
  @NonNull
  public Mono<Void> filter(@NonNull ServerWebExchange ex, @NonNull WebFilterChain chain) {
    var path = ex.getRequest().getPath();
    boolean validTransactionEndpoint =
        APIConstants.DSQ_ENDPOINTS.stream()
            .map(endPoint -> webFluxProperties.getBasePath().concat(endPoint))
            .collect(Collectors.toList())
            .contains(path.toString());
    if (!validTransactionEndpoint) {
      return chain.filter(ex);
    }
    ex.getResponse().beforeCommit(() -> enrichResponseHeaders(ex.getResponse()));
    return chain.filter(ex).contextWrite(ctx -> enrichContext(ex.getRequest(), ctx));
  }

  /**
   * Enriches Request Scoped Reactive Context
   *
   * @param request {@link ServerHttpRequest}
   * @param context {@link Context}
   */
  private Context enrichContext(final ServerHttpRequest request, final Context context) {
    var provincialRequestControl = controllerUtils.buildProvincialRequestControl(request);
    var bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    controllerUtils.checkSupportedProvince(provincialRequestControl.getProvince());

    MessageProcess messageProcess = null;
    MessagePayloadTemplate requestMessagePayloadTemplate = null;
    Service source =
        Service.builder().code(ServiceCode.UNKNOWN).serviceOutageStatus(Strings.EMPTY).build();

    Service target =
        Service.builder().code(ServiceCode.VPHX_PHMS).serviceOutageStatus(Strings.EMPTY).build();

    HttpHeaders httpHeaders = request.getHeaders();
    if (httpHeaders.get(HttpHeaders.REFERER) != null) {
      log.info("Referer", httpHeaders.get(HttpHeaders.REFERER));
      // TODO: source need to fetch from HttpHeaders.Referer or any other approach we can
      // check to get source
    }

    String endPoint = request.getPath().toString();
    endPoint = endPoint.replace(webFluxProperties.getBasePath(), "");

    boolean messageLogEncryptionEnabled = false;
    boolean messageLogCompressionEnabled = false;

    switch (endPoint) {
      case APIConstants.PROVIDER_SEARCH_URI:
        {
          messageProcess = MessageProcess.PROVIDER_SEARCH;
          break;
        }
      case APIConstants.PATIENT_SEARCH_URI:
        {
          messageProcess = MessageProcess.PATIENT_SEARCH;
          break;
        }
      case APIConstants.PATIENT_CONSENT_URI:
        {
          messageProcess = MessageProcess.PATIENT_CONSENT;
          break;
        }
      case APIConstants.LOCATION_SEARCH_URI:
        {
          messageProcess = MessageProcess.LOCATION_SEARCH;
          break;
        }
      case APIConstants.LOCATION_DETAILS_URI:
        {
          messageProcess = MessageProcess.LOCATION_DETAILS;
          break;
        }
      default:
        break;
    }
    if (Objects.nonNull(messageProcess)) {
      requestMessagePayloadTemplate = getProvincialRequestMessagePayloadTemplate(messageProcess);
    }

    messageLogEncryptionEnabled = ENCRYPTION_ENABLED_MESSAGE_PROCESS.contains(messageProcess);
    messageLogCompressionEnabled = COMPRESSION_ENABLED_MESSAGE_PROCESS.contains(messageProcess);

    return context.putAll(
        Context.of(
                Map.ofEntries(
                    Map.entry(ProvincialRequestControl.class, provincialRequestControl),
                    Map.entry(HttpHeaders.AUTHORIZATION, bearerToken),
                    Map.entry(MessageProcess.class, messageProcess),
                    Map.entry(MessageLogsConstants.LOG_SOURCE_CONTEXT, source),
                    Map.entry(MessageLogsConstants.LOG_TARGET_CONTEXT, target),
                    Map.entry(
                        MESSAGE_PAYLOAD_TEMPLATE_REQUEST_CONTEXT, requestMessagePayloadTemplate),
                    Map.entry(LOG_ENCRYPTION_ENABLED_CONTEXT, messageLogEncryptionEnabled),
                    Map.entry(LOG_COMPRESSION_ENABLED_CONTEXT, messageLogCompressionEnabled)))
            .readOnly());
  }

  /**
   * Enriches Response Headers using Reactive Context
   *
   * @param response {@link ServerHttpResponse}
   */
  private Mono<Void> enrichResponseHeaders(final ServerHttpResponse response) {
    return Mono.deferContextual(Mono::just)
        .doOnNext(
            ctx -> {
              var optionalRequestId =
                  ctx.<ProvincialRequestControl>getOrEmpty(ProvincialRequestControl.class)
                      .map(ProvincialRequestControl::getRequestId);

              optionalRequestId.ifPresent(
                  requestId ->
                      response
                          .getHeaders()
                          .put(HeaderConstants.X_RESPONSE_ID_HEADER, List.of(requestId)));
            })
        .then();
  }

  /**
   * Get MessagePayloadTemplate from provincialRequestProperties request {@link
   * ProvincialRequestProperties}
   *
   * @param messageProcess {@link MessageProcess}
   * @return messagePayloadTemplate {@link MessagePayloadTemplate}
   */
  private MessagePayloadTemplate getProvincialRequestMessagePayloadTemplate(
      MessageProcess messageProcess) {
    return provincialRequestProperties
        .getRequest()
        .getTransaction()
        .get(messageProcess.getName())
        .getMessagePayloadTemplate();
  }
}
