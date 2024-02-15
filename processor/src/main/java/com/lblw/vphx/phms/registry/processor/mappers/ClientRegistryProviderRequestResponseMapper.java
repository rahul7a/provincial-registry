package com.lblw.vphx.phms.registry.processor.mappers;

import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.DateFormat;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * This is a helper to {@link ClientRegistryPatientRequestResponseMapper} and helps convert request
 * objects into their equivalent HL7 Soap request
 */
@Slf4j
@Component
public class ClientRegistryProviderRequestResponseMapper {
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
  public ClientRegistryProviderRequestResponseMapper(
      ProvincialRequestProperties provincialRequestProperties, UUIDGenerator uuidGenerator) {
    this.provincialRequestProperties = provincialRequestProperties;
    this.uuidGenerator = uuidGenerator;
  }
  /**
   * This transforms {@link ProvincialProviderSearchCriteria} to its equivalent {@link *
   * ProvincialProviderSearchRequest}
   *
   * @param provincialProviderSearchCriteria An instance of {@link ProvincialProviderSearchCriteria}
   *     that must be converted.
   * @return provincialProviderSearchRequest {@link ProvincialProviderSearchRequest} containing
   *     search criteria
   */
  public @NonNull ProvincialProviderSearchRequest convertSearchCriteriaToRequest(
      @NonNull ProvincialProviderSearchCriteria provincialProviderSearchCriteria) {

    ProvincialProviderSearchRequest.ProvincialProviderSearchRequestBuilder<?, ?>
        provincialProviderSearchRequestBuilder = ProvincialProviderSearchRequest.builder();

    provincialProviderSearchRequestBuilder.requestBodyTransmissionWrapper(
        buildHl7v3RequestBodyTransmissionWrapper());

    setRequestBodyControlAct(
        provincialProviderSearchCriteria, provincialProviderSearchRequestBuilder);
    provincialProviderSearchRequestBuilder.provincialRequestPayload(
        provincialProviderSearchCriteria);

    return provincialProviderSearchRequestBuilder.build();
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
   * ProvincialProviderSearchRequest} object
   *
   * @param provincialProviderSearchCriteria
   * @param provincialProviderSearchRequestBuilder {@link
   *     ProvincialProviderSearchRequest.ProvincialProviderSearchRequestBuilder} RequestControlAct
   */
  private void setRequestBodyControlAct(
      ProvincialProviderSearchCriteria provincialProviderSearchCriteria,
      ProvincialProviderSearchRequest.ProvincialProviderSearchRequestBuilder<?, ?>
          provincialProviderSearchRequestBuilder) {
    provincialProviderSearchRequestBuilder.requestControlAct(
        RequestControlAct.builder()
            .eventRoot(provincialRequestProperties.getRequest().getSender().getControlActRoot())
            .eventCorrelationId(
                provincialProviderSearchCriteria.getProvincialRequestControl().getRequestId())
            .build());
  }
}
