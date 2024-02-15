package com.lblw.vphx.phms.transformation.response.transformers;

import static com.lblw.vphx.phms.common.utils.XmlParsingUtils.*;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.Telecom;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.location.LocationAddress;
import com.lblw.vphx.phms.domain.location.ProvincialLocation;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponseControlAct;
import com.lblw.vphx.phms.transformation.response.ResponseTransformer;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseTransformerHelper;
import java.io.IOException;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

/**
 * This class helps transformation service to transform response for
 * ProvincialLocationDetailsResponse
 */
@Component
@Slf4j
public class LocationDetailsResponseTransformer
    implements ResponseTransformer<ProvincialLocationDetailsResponse> {

  private final CommonResponseTransformerHelper commonResponseTransformerHelper;
  private final CodeableConceptService codeableConceptService;

  /**
   * Public constructor.
   *
   * @param commonResponseTransformerHelper {@link CommonResponseTransformerHelper}
   * @param codeableConceptService {@link CodeableConceptService}
   */
  public LocationDetailsResponseTransformer(
      CommonResponseTransformerHelper commonResponseTransformerHelper,
      CodeableConceptService codeableConceptService) {
    this.commonResponseTransformerHelper = commonResponseTransformerHelper;
    this.codeableConceptService = codeableConceptService;
  }
  /**
   * Transform response into domain object. Response could be xml, hl7-v2 or hl7-v3 etc.
   *
   * @param response Response could be xml, hl7-v2 or hl7-v3 etc. In current implementation only
   *     hl7-v3 is implemented
   * @return {@link ProvincialLocationDetailsResponse}
   */
  @Override
  public Mono<ProvincialLocationDetailsResponse> transform(String response) {
    return Mono.fromCallable(
        () -> {
          if (StringUtils.isBlank(response)) {
            log.error("Null/empty/blank response body for location details response.");
            throw new IllegalArgumentException(
                "Response body is required to transform location details response");
          }

          return transformXMLPayloadToLocationDetailsResponse(response);
        });
  }
  /**
   * Transforms XML response to ProvincialLocationDetailsResponse
   *
   * @param response XML {@link String}
   * @return {@link ProvincialLocationDetailsResponse} transformed response
   */
  private ProvincialLocationDetailsResponse transformXMLPayloadToLocationDetailsResponse(
      String response) {

    Element envelope = null;
    try {
      envelope = createRootResponseEnvelope(response);
    } catch (ParserConfigurationException | SAXException | IOException e) {

      log.error(
          String.format(
              ErrorConstants.EXCEPTION_MESSAGE_TRANSFORM_XML_RESPONSE,
              LocationDetailsResponseTransformer.class.getName()),
          e);
      ExceptionUtils.rethrow(e);
    }

    var controlActEvent = getDeepestDescendantElementTree(envelope, HL7Constants.CONTROL_ACT_EVENT);
    String extensionRootId =
        getAttributeValueFromTheGivenTree(controlActEvent, HL7Constants.EXTENSION, HL7Constants.ID);

    final Element serviceDeliveryLocationElement =
        getDeepestDescendantElementTree(
            controlActEvent,
            HL7Constants.SUBJECT,
            HL7Constants.REGISTRATION_REQUEST,
            HL7Constants.SERVICE_DELIVERY_LOCATION);

    return ProvincialLocationDetailsResponse.builder()
        .responseHeader(commonResponseTransformerHelper.buildResponseHeaderBuilder(envelope))
        .responseBodyTransmissionWrapper(
            commonResponseTransformerHelper.buildTransmissionWrapper(envelope))
        .queryAcknowledgement(commonResponseTransformerHelper.buildQueryAck(envelope))
        .detectedIssues(commonResponseTransformerHelper.buildDetectedIssue(envelope))
        .responseControlAct(
            buildProvincialLocationDetailsResponseControlAct(controlActEvent, extensionRootId))
        .provincialResponsePayload(
            buildProvincialLocationDetails(serviceDeliveryLocationElement, extensionRootId))
        .build();
  }

  /**
   * Prepares an instance of {@link ProvincialLocationDetails} from the given Service Delivery
   * Location element.
   *
   * @param serviceDeliveryLocationElement Service Delivery Location element.
   * @param extensionRootId Extension Root id.
   * @return Prepared {@link ProvincialLocationDetails}
   */
  @Nullable
  private ProvincialLocationDetails buildProvincialLocationDetails(
      @Nullable Element serviceDeliveryLocationElement, String extensionRootId) {
    if (serviceDeliveryLocationElement == null) {
      return ProvincialLocationDetails.builder()
          .provincialResponseAcknowledgement(
              ProvincialResponseAcknowledgement.builder()
                  .auditEvent(
                      AuditEvent.builder()
                          .provincialRequestControl(
                              ProvincialRequestControl.builder()
                                  .province(getProvince())
                                  .requestId(extensionRootId)
                                  .build())
                          .build())
                  .build())
          .build();
    }
    String locationName =
        getElementTextFromTheGivenTree(serviceDeliveryLocationElement, HL7Constants.NAME);
    var provincialLocationTypeCode =
        Optional.ofNullable(
                getAttributeValueFromTheGivenTree(
                    serviceDeliveryLocationElement, HL7Constants.CODE, HL7Constants.CODE))
            .or(
                () ->
                    Optional.ofNullable(
                        getAttributeValueFromTheGivenTree(
                            serviceDeliveryLocationElement,
                            HL7Constants.NULL_FLAVOR,
                            HL7Constants.CODE)));
    String region =
        getAttributeValueFromTheGivenTree(
            serviceDeliveryLocationElement,
            HL7Constants.EXTENSION,
            HL7Constants.INDIRECT_AUTHORITY,
            HL7Constants.TERRITORIAL_AUTHORITY,
            HL7Constants.ID);

    // Building permit, pharmacyBilling and publicHealthInsurancePermit
    SystemIdentifier permit = null;
    SystemIdentifier pharmacyBilling = null;
    SystemIdentifier publicHealthInsurancePermit = null;

    String identifierLocationExtensionId =
        getAttributeValueFromTheGivenTree(
            serviceDeliveryLocationElement,
            HL7Constants.EXTENSION,
            HL7Constants.LOCATION,
            HL7Constants.AS_IDENTIFIED_LOCATION,
            HL7Constants.ID);
    String assigningIdentifierOrgName =
        getElementTextFromTheGivenTree(
            serviceDeliveryLocationElement,
            HL7Constants.LOCATION,
            HL7Constants.AS_IDENTIFIED_LOCATION,
            HL7Constants.ASSIGNING_IDENTIFIER_ORGANIZATION,
            HL7Constants.NAME);
    if (!StringUtils.isAnyBlank(identifierLocationExtensionId, assigningIdentifierOrgName)) {
      permit = buildPermitNumber(assigningIdentifierOrgName, identifierLocationExtensionId);
      pharmacyBilling =
          buildPharmacyBilling(assigningIdentifierOrgName, identifierLocationExtensionId);
      publicHealthInsurancePermit =
          buildPublicHeathInsurancePermit(
              assigningIdentifierOrgName, identifierLocationExtensionId);
    }

    String identifierString =
        getAttributeValueFromTheGivenTree(
            serviceDeliveryLocationElement, HL7Constants.EXTENSION, HL7Constants.ID);
    SystemIdentifier identifier = null;
    if (StringUtils.isNotBlank(identifierString)) {
      identifier =
          buildSystemIdentifier(
              SystemIdentifier.IDENTIFIER_TYPE.LOCATION, identifierString, HL7Constants.NIU_O);
    }
    ProvincialLocation parentLocation = buildParentLocation(serviceDeliveryLocationElement);

    Telecom telecom = null;
    String telecomString =
        getAttributeValueFromTheGivenTree(
            serviceDeliveryLocationElement,
            HL7Constants.VALUE,
            HL7Constants.DIRECT_AUTHORITY_OVER,
            HL7Constants.CONTACT_PARTY,
            HL7Constants.TELECOM);
    if (StringUtils.isNotBlank(telecomString)) {
      telecom = Telecom.builder().number(telecomString).build();
    }
    ProvincialLocation provincialLocation =
        ProvincialLocation.builder()
            .address(buildLocationAddresses(serviceDeliveryLocationElement))
            .locationType(
                provincialLocationTypeCode
                    .flatMap(
                        locationTypeCode ->
                            codeableConceptService.findSystemLocationTypeCodingByProvincialCode(
                                getProvince(), locationTypeCode))
                    .orElse(null))
            .parentLocation(parentLocation)
            .telecom(telecom)
            .identifier(identifier)
            .name(locationName)
            .region(region)
            .permit(permit)
            .pharmacyBilling(pharmacyBilling)
            .publicHealthInsurancePermit(publicHealthInsurancePermit)
            .build();

    return ProvincialLocationDetails.builder()
        .provincialLocation(provincialLocation)
        .provincialResponseAcknowledgement(
            ProvincialResponseAcknowledgement.builder()
                .auditEvent(
                    AuditEvent.builder()
                        .provincialRequestControl(
                            ProvincialRequestControl.builder()
                                .province(getProvince())
                                .requestId(extensionRootId)
                                .build())
                        .build())
                .build())
        .build();
  }

  /**
   * Prepares Parent location from provided serviceDeliveryLocationElement
   *
   * @param serviceDeliveryLocationElement {@link Element}
   * @return parentLocation {@link ProvincialLocation}
   */
  private ProvincialLocation buildParentLocation(Element serviceDeliveryLocationElement) {
    String parentLocationId =
        getAttributeValueFromTheGivenTree(
            serviceDeliveryLocationElement,
            HL7Constants.EXTENSION,
            HL7Constants.SERVICE_PROVIDER_ORGANIZATION,
            HL7Constants.ID);
    SystemIdentifier parentLocationIdentifier =
        buildSystemIdentifier(
            SystemIdentifier.IDENTIFIER_TYPE.PARENT_LOCATION, parentLocationId, HL7Constants.NIU_O);
    String parentLocationName =
        getElementTextFromTheGivenTree(
            serviceDeliveryLocationElement,
            HL7Constants.SERVICE_PROVIDER_ORGANIZATION,
            HL7Constants.NAME);
    if (parentLocationName != null) {
      parentLocationName = "null".equals(parentLocationName.trim()) ? "" : parentLocationName;
    }

    return ProvincialLocation.builder()
        .identifier(parentLocationId != null ? parentLocationIdentifier : null)
        .name(parentLocationName)
        .build();
  }

  /**
   * Prepares a Pharmacy Billing Identifier from the given extension id.
   *
   * @param assigningIdentifierOrgName assigning identifier org name
   * @param extensionId Value to be populated.
   * @return Returns null if assigningIdentifierOrgName is not RAMQ or is not of expected format
   *     length
   */
  @Nullable
  private SystemIdentifier buildPharmacyBilling(
      @NonNull String assigningIdentifierOrgName, @NonNull String extensionId) {
    if (!assigningIdentifierOrgName.equals(HL7Constants.RAMQ)
        || extensionId.length() != HL7Constants.RAMQ_PHARMACY_BILLING_EXT_FORMAT.length()) {
      return null;
    }

    return buildSystemIdentifier(
        SystemIdentifier.IDENTIFIER_TYPE.LOCATION_PHARMACY_BILLING_NUMBER,
        extensionId,
        HL7Constants.RAMQ);
  }

  /**
   * Prepares a health care permit Identifier from the given extension id.
   *
   * @param assigningIdentifierOrgName assigning identifier org name
   * @param extensionId Value to be populated.
   * @return Returns null if assigningIdentifierOrgName is not MSSS or is not of expected format
   *     length
   */
  @Nullable
  private SystemIdentifier buildPublicHeathInsurancePermit(
      @NonNull String assigningIdentifierOrgName, @NonNull String extensionId) {
    if (!assigningIdentifierOrgName.equals(HL7Constants.MSSS)
        || extensionId.length() != HL7Constants.MSSS_PERMIT_NUMBER_EXT_FORMAT.length()) {
      return null;
    }

    return buildSystemIdentifier(
        SystemIdentifier.IDENTIFIER_TYPE.LOCATION_PUBLIC_HEALTHCARE_PERMIT_NUMBER,
        extensionId,
        HL7Constants.MSSS);
  }

  /**
   * Prepares a Permit Number Identifier from the given extension id.
   *
   * @param assigningIdentifierOrgName assigning identifier org name
   * @param extensionId Value to be populated.
   * @return Returns null if assigningIdentifierOrgName is not RAMQ or is not of expected format
   *     length
   */
  @Nullable
  private SystemIdentifier buildPermitNumber(
      @NonNull String assigningIdentifierOrgName, @NonNull String extensionId) {
    if (!assigningIdentifierOrgName.equals(HL7Constants.RAMQ)
        || extensionId.length() != HL7Constants.RAMQ_PERMIT_NUMBER_EXT_FORMAT.length()) {
      return null;
    }
    return buildSystemIdentifier(
        SystemIdentifier.IDENTIFIER_TYPE.LOCATION_PERMIT_NUMBER, extensionId, HL7Constants.RAMQ);
  }

  /**
   * Prepares an instance of {@link LocationAddress} from the given Service Delivery Location
   * element.
   *
   * @param serviceDeliveryLocationElement Service Delivery Location element.
   * @return Prepared instance of {@link LocationAddress}. Returns null if Service Delivery Location
   *     element is null.
   */
  @Nullable
  private LocationAddress buildLocationAddresses(@Nullable Element serviceDeliveryLocationElement) {
    if (serviceDeliveryLocationElement == null) {
      return null;
    }
    final Element addrElement =
        getDeepestDescendantElementTree(serviceDeliveryLocationElement, HL7Constants.ADDR);
    String postalCode = getElementTextFromTheGivenTree(addrElement, HL7Constants.POSTAL_CODE);
    String city = getElementTextFromTheGivenTree(addrElement, HL7Constants.CITY);
    String streetAddressLine1 =
        getElementTextFromTheGivenTree(addrElement, HL7Constants.STREET_ADDRESS_LINE);
    String streetAddressLine2 =
        getElementTextFromTheGivenTree(addrElement, HL7Constants.ADDITIONAL_LOCATOR);

    return LocationAddress.builder()
        .postalCode(postalCode)
        .city(city)
        .streetAddressLine1(streetAddressLine1)
        .streetAddressLine2(streetAddressLine2)
        .build();
  }

  /**
   * Prepares an instance of {@link SystemIdentifier} of given {@link
   * SystemIdentifier.IDENTIFIER_TYPE} and value
   *
   * @param identifierType {@link SystemIdentifier.IDENTIFIER_TYPE}
   * @param value value with which the {@link SystemIdentifier} must be populated.
   * @param system system with which the {@link SystemIdentifier} must be populated
   * @return Prepared {@link SystemIdentifier}
   */
  private SystemIdentifier buildSystemIdentifier(
      SystemIdentifier.IDENTIFIER_TYPE identifierType, String value, String system) {
    return SystemIdentifier.builder()
        .system(system)
        .type(identifierType)
        .value(value)
        .assigner(HL7Constants.QC)
        .build();
  }

  /**
   * Prepares an instance of {@link ProvincialLocationDetailsResponseControlAct} from the given
   * Control Act Event element.
   *
   * @param controlActEvent Control Act Event element.
   * @param extensionRootId Extension Root id.
   * @return Prepared {@link ProvincialLocationDetailsResponseControlAct}
   */
  private ProvincialLocationDetailsResponseControlAct
      buildProvincialLocationDetailsResponseControlAct(
          Element controlActEvent, String extensionRootId) {
    String rootId =
        getAttributeValueFromTheGivenTree(controlActEvent, HL7Constants.ROOT, HL7Constants.ID);
    var parameterListSubTree =
        getDeepestDescendantElementTree(controlActEvent, HL7Constants.PARAMETER_LIST);
    String identifier =
        getAttributeValueFromTheGivenTree(
            parameterListSubTree,
            HL7Constants.EXTENSION,
            HL7Constants.RECORD_ID,
            HL7Constants.VALUE);

    return ProvincialLocationDetailsResponseControlAct.builder()
        .eventCorrelationId(extensionRootId)
        .eventRoot(rootId)
        .provincialLocationDetailsCriteria(
            ProvincialLocationDetailsCriteria.builder().identifier(identifier).build())
        .build();
  }

  @Override
  public Province getProvince() {
    return Province.QC;
  }
}
