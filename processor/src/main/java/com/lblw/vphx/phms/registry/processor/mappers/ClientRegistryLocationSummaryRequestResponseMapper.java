package com.lblw.vphx.phms.registry.processor.mappers;

import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.DateFormat;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * This is a helper to {@link ClientRegistryLocationSummaryRequestResponseMapper} and helps convert
 * request objects into their equivalent HL7 Soap request
 */
@Slf4j
@Component
public class ClientRegistryLocationSummaryRequestResponseMapper {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern(DateFormat.QC_DATE_TIME_FORMAT);
  private final ProvincialRequestProperties provincialRequestProperties;
  private final UUIDGenerator uuidGenerator;
  /**
   * public constructor
   *
   * @param provincialRequestProperties {@link ProvincialRequestProperties}
   * @param uuidGenerator {@link UUIDGenerator}
   */
  public ClientRegistryLocationSummaryRequestResponseMapper(
      ProvincialRequestProperties provincialRequestProperties, UUIDGenerator uuidGenerator) {
    this.provincialRequestProperties = provincialRequestProperties;
    this.uuidGenerator = uuidGenerator;
  }
  /**
   * This transforms {@link ProvincialLocationSearchCriteria} to its equivalent {@link
   * ProvincialLocationDetailsRequest}
   *
   * @param provincialLocationSearchCriteria An instance of {@link ProvincialLocationSearchCriteria}
   *     that must be converted.
   * @return provincialLocationDetailsRequest {@link ProvincialLocationDetailsRequest} containing
   *     search criteria
   */
  public @NonNull ProvincialLocationSearchRequest convertSearchCriteriaToRequest(
      @NonNull ProvincialLocationSearchCriteria provincialLocationSearchCriteria) {

    ProvincialLocationSearchRequest.ProvincialLocationSearchRequestBuilder<?, ?>
        provincialLocationSearchRequestBuilder = ProvincialLocationSearchRequest.builder();

    provincialLocationSearchRequestBuilder.requestBodyTransmissionWrapper(
        buildHl7v3RequestBodyTransmissionWrapper());

    setRequestBodyControlAct(
        provincialLocationSearchCriteria, provincialLocationSearchRequestBuilder);
    provincialLocationSearchRequestBuilder.provincialRequestPayload(
        provincialLocationSearchCriteria);

    return provincialLocationSearchRequestBuilder.build();
  }

  /**
   * Creates a {@link RequestBodyTransmissionWrapper} instance
   *
   * @return a {@link RequestBodyTransmissionWrapper}
   */
  private RequestBodyTransmissionWrapper buildHl7v3RequestBodyTransmissionWrapper() {

    String transmissionUniqueIdentifier = uuidGenerator.generateUUID();

    // Setting creation time in UTC.
    ZonedDateTime transmissionCreationDateTimeInUtc = ZonedDateTime.now(ZoneId.of("UTC"));
    final String transmissionCreationDateTimeInUtcString =
        transmissionCreationDateTimeInUtc.format(DATE_TIME_FORMATTER);

    return RequestBodyTransmissionWrapper.builder()
        .senderRoot(provincialRequestProperties.getRequest().getSender().getRoot())
        .senderApplicationName(provincialRequestProperties.getRequest().getSender().getExtension())
        .senderApplicationId(provincialRequestProperties.getRequest().getSender().getName())
        .processingCode(provincialRequestProperties.getRequest().getSender().getProcessingCode())
        .transmissionUniqueIdentifier(transmissionUniqueIdentifier)
        .transmissionCreationDateTime(transmissionCreationDateTimeInUtcString)
        .build();
  }

  /**
   * Creates a {@link RequestControlAct} instance and sets it in the {@link
   * ProvincialLocationDetailsRequest} object
   *
   * @param provincialLocationSearchCriteria
   * @param provincialLocationSearchRequestBuilder {@link
   *     ProvincialLocationDetailsRequest.ProvincialLocationDetailsRequestBuilder} RequestControlAct
   */
  private void setRequestBodyControlAct(
      ProvincialLocationSearchCriteria provincialLocationSearchCriteria,
      ProvincialLocationSearchRequest.ProvincialLocationSearchRequestBuilder<?, ?>
          provincialLocationSearchRequestBuilder) {
    provincialLocationSearchRequestBuilder.requestControlAct(
        RequestControlAct.builder()
            .eventRoot(provincialRequestProperties.getRequest().getSender().getControlActRoot())
            .eventCorrelationId(
                provincialLocationSearchCriteria.getProvincialRequestControl().getRequestId())
            .build());
  }
}
