package com.lblw.vphx.phms.transformation.response.helpers;

import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.BindOne;
import com.lblw.vphx.phms.domain.common.response.hl7v3.DetectedIssue;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseHeader;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.DetectedIssueMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.QueryAcknowledgementMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseBodyTransmissionWrapperMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseHeaderMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Helper class to handle common retrievals from HL7V3 response. */
@Component
@Slf4j
public class CommonResponseXPathTransformerHelper {

  /**
   * Builds an instance of {@link ResponseHeader} from given {@code s:Header} xml element.
   *
   * @param binder {@link BindOne} represents XML tree for {@code s:Header}
   * @return Prepared {@link ResponseHeader} from the {@code header}
   */
  public ResponseHeader buildResponseHeaderBuilder(BindOne<ResponseHeader> binder) {
    if (Objects.isNull(binder)) {
      return null;
    }
    return binder.apply(ResponseHeaderMapper::new);
  }

  /**
   * Builds an instance of {@link ResponseBodyTransmissionWrapper} from given {@code
   * acknowledgement} xml element.
   *
   * @param binder {@link BindOne} represents XML tree in memory for {@code acknowledgement}
   * @return Prepared {@link ResponseBodyTransmissionWrapper} from the {@code acknowledgement}.
   */
  public ResponseBodyTransmissionWrapper buildTransmissionWrapper(
      BindOne<ResponseBodyTransmissionWrapper> binder) {

    if (Objects.isNull(binder)) {
      return null;
    }
    return binder.apply(ResponseBodyTransmissionWrapperMapper::new);
  }

  /**
   * Builds an instance of {@link QueryAcknowledgement} from given {@code controlActEvent} xml
   * element.
   *
   * @param binder {@link BindOne} represents XML tree in memory for {@code <controlActEvent>}
   * @return Prepared {@link QueryAcknowledgement}
   */
  public QueryAcknowledgement buildQueryAck(BindOne<QueryAcknowledgement> binder) {
    if (Objects.isNull(binder)) {
      return null;
    }
    return binder.apply(QueryAcknowledgementMapper::new);
  }

  /**
   * Builds a list of {@link DetectedIssue} from given {@code subjectElement} xml element
   *
   * @param binder {@link BindMany} represents XML tree in memory for {@code <controlActEvent>}
   * @return Prepared list of {@link DetectedIssue}
   */
  public List<DetectedIssue> buildDetectedIssue(BindMany<DetectedIssue> binder) {
    if (Objects.isNull(binder)) {
      return null;
    }
    return binder.apply(DetectedIssueMapper::new).collect(Collectors.toList());
  }
}
