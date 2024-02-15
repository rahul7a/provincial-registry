package com.lblw.vphx.phms.registry.processor.mappers;

import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.DateFormat;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * This is a helper to {@link ClientRegistryPatientConsentRequestResponseMapper} and helps convert
 * request objects into their equivalent HL7 Soap request
 */
@Slf4j
@Component
public class ClientRegistryPatientConsentRequestResponseMapper {
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
  public ClientRegistryPatientConsentRequestResponseMapper(
      ProvincialRequestProperties provincialRequestProperties, UUIDGenerator uuidGenerator) {
    this.provincialRequestProperties = provincialRequestProperties;
    this.uuidGenerator = uuidGenerator;
  }
  /**
   * This transforms {@link ProvincialPatientConsentCriteria} to its equivalent {@link *
   * ProvincialPatientConsentRequest}
   *
   * @param provincialPatientConsentCriteria An instance of {@link ProvincialPatientConsentCriteria}
   *     that must be converted.
   * @return provincialPatientConsentRequest {@link ProvincialPatientConsentRequest} containing
   *     search criteria
   */
  public @NonNull ProvincialPatientConsentRequest convertSearchCriteriaToRequest(
      @NonNull ProvincialPatientConsentCriteria provincialPatientConsentCriteria) {

    ProvincialPatientConsentRequest.ProvincialPatientConsentRequestBuilder<?, ?>
        provincialPatientConsentRequestBuilder = ProvincialPatientConsentRequest.builder();

    provincialPatientConsentRequestBuilder.requestBodyTransmissionWrapper(
        buildHl7v3RequestBodyTransmissionWrapper());

    setRequestBodyControlAct(
        provincialPatientConsentCriteria, provincialPatientConsentRequestBuilder);
    provincialPatientConsentRequestBuilder.provincialRequestPayload(
        provincialPatientConsentCriteria);

    return provincialPatientConsentRequestBuilder.build();
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
   * ProvincialPatientConsentRequest} object
   *
   * @param provincialPatientConsentCriteria
   * @param provincialPatientConsentRequestBuilder {@link
   *     ProvincialPatientConsentRequest.ProvincialPatientConsentRequestBuilder} RequestControlAct
   */
  private void setRequestBodyControlAct(
      ProvincialPatientConsentCriteria provincialPatientConsentCriteria,
      ProvincialPatientConsentRequest.ProvincialPatientConsentRequestBuilder<?, ?>
          provincialPatientConsentRequestBuilder) {
    provincialPatientConsentRequestBuilder.requestControlAct(
        RequestControlAct.builder()
            .eventRoot(provincialRequestProperties.getRequest().getSender().getControlActRoot())
            .eventCorrelationId(
                provincialPatientConsentCriteria.getProvincialRequestControl().getRequestId())
            .build());
  }
}
