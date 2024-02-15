package com.lblw.vphx.phms.transformation.services;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.request.RequestTransformerEngine;
import com.lblw.vphx.phms.transformation.response.ResponseTransformerEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Thymeleaf-backed Transformation Service that converts an object to HL7 V3 XML and vice versa (not
 * using thymeleaf)
 */
@Service
@Slf4j
public class TransformationService<
    R extends Response<? extends ResponseControlAct, ? extends ProvincialResponse>> {

  private final RequestTransformerEngine requestTransformerEngine;
  private final ResponseTransformerEngine<R> responseTransformerEngine;

  /**
   * Creates a new Thymeleaf-backed Transformation Service that converts an object to HL7 V3 XML
   *
   * @param requestTransformerEngine {@link RequestTransformerEngine} autowired to sign and return
   *     HL7 V3 XML
   * @param responseTransformerEngine {@link ResponseTransformerEngine} autowired to sign and return
   *     Response object
   */
  public TransformationService(
      RequestTransformerEngine requestTransformerEngine,
      ResponseTransformerEngine<R> responseTransformerEngine) {
    this.requestTransformerEngine = requestTransformerEngine;
    this.responseTransformerEngine = responseTransformerEngine;
  }
  /**
   * Returns a String representation of a signed HL7 V3 XML request translated from a PHMS Request
   * object as indicated by the transaction type
   *
   * @param request {@link Request} object to be translated into HL7 V3 XML. If request is null, the
   *     service will generate an empty request String file, will will generate an empty request
   *     String
   * @return {@link String} signed xml
   */
  public Mono<String> transformRequest(Request<? extends ProvincialRequest> request)
      throws TransformationException {

    return this.requestTransformerEngine.transform(request);
  }

  /**
   * Returns a Response object of transformed Hl7 V3 response when String response xml of hl7 V3 is
   * provided
   *
   * @param response {@link String} object of hl7 V3 xml
   * @return {@link Response} transformed response
   */
  public Mono<R> transformResponse(String response) {
    return responseTransformerEngine.transform(response);
  }
}
