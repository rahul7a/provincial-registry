package com.lblw.vphx.phms.transformation.request;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import java.util.EnumMap;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

/**
 * Creates and delegates setting of {@link Context} used in Thymeleaf template to an implementation
 * of {@link RequestContextBuilder} specific to a {@link MessageProcess}. Any kind of complex data
 * enrichment should be implemented in {@link RequestContextBuilder}. Current implementation
 */
@Component
public class RequestContextFactory {

  public static final String PHMS_REQUEST = "phmsRequest";
  private final CodeableConceptService codeableConceptService;

  /**
   * Maintains a list of {@link RequestContextBuilder} implementations for {@link MessageProcess)
   */
  private final EnumMap<MessageProcess, RequestContextBuilder> requestContextMap =
      new EnumMap<>(MessageProcess.class);

  public RequestContextFactory(CodeableConceptService codeableConceptService) {
    this.codeableConceptService = codeableConceptService;
  }

  /**
   * Creates an instance of {@link Context}. Also finds an implementation of {@link
   * RequestContextBuilder} specific to a {@link MessageProcess} and calls {@link
   * RequestContextBuilder#setContext(Context, Request)}
   *
   * @param request an instance of {@link Request}
   * @param messageProcess {@link MessageProcess}
   * @return thymeleaf template {@link Context}
   */
  public Context createContext(
      Request<? extends ProvincialRequest> request, MessageProcess messageProcess) {
    Context context = new Context();
    context.setVariable(PHMS_REQUEST, request);

    RequestContextBuilder requestContext = requestContextMap.get(messageProcess);
    if (requestContext != null) {
      requestContext.setContext(context, request);
    }

    return context;
  }

  /** Adds all available implementation of {@link RequestContextBuilder} */
  @PostConstruct
  private void populateContexts() {
    requestContextMap.put(
        MessageProcess.PROVIDER_SEARCH,
        new ProvincialProviderSearchContextBuilder(codeableConceptService));
    requestContextMap.put(
        MessageProcess.LOCATION_SEARCH,
        new ProvincialLocationSummarySearchContextBuilder(codeableConceptService));
    requestContextMap.put(
        MessageProcess.PATIENT_SEARCH, new ProvincialPatientSearchContextBuilder());
  }
}
