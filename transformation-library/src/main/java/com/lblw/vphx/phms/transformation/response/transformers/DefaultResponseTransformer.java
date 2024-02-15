package com.lblw.vphx.phms.transformation.response.transformers;

import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.transformation.response.ResponseTransformer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * This class helps transformation service to return DefaultResponse Transformer when Query is not
 * matched
 */
@Component
public class DefaultResponseTransformer
    implements ResponseTransformer<
        Response<? extends ResponseControlAct, ? extends ProvincialResponse>> {
  /**
   * This method helps transform response when Transaction Type is not matched
   *
   * @param hl7Response {@link String} Response is hl7-v3
   * @return null {@link Response}
   */
  @Override
  public Mono<Response<? extends ResponseControlAct, ? extends ProvincialResponse>> transform(
      String hl7Response) {
    // TODO: will be updated.
    return null;
  }

  @Override
  public Province getProvince() {
    return Province.QC;
  }
}
