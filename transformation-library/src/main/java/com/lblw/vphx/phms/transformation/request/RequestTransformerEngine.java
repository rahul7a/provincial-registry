package com.lblw.vphx.phms.transformation.request;

import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.internal.objectstorage.service.ObjectStorageService;
import com.lblw.vphx.phms.common.security.services.SecurityService;
import com.lblw.vphx.phms.common.utils.XmlParsingUtils;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.pharmacy.ProvincialPharmacyCertificateDetails;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.request.preprocessors.BaseTemplateRequestTemplatePreProcessor;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import reactor.core.publisher.Mono;

/** This class helps to transform rhe request in to request Xml */
@Component
@Slf4j
public class RequestTransformerEngine {

  private static final String TEMPLATE_REQUEST_SUFFIX = "request.xml";
  private final BaseTemplateRequestTemplatePreProcessor baseTemplateRequestPreProcessor;
  private final SpringTemplateEngine templateEngine;
  private final SecurityService securityService;
  private final XmlParsingUtils xmlParsingUtils;
  private final RequestContextFactory requestContextFactory;
  private final ObjectStorageService objectStorageService;
  private final ProvincialRequestProperties provincialRequestProperties;
  /**
   * public constructor
   *
   * @param templateEngine {@link org.thymeleaf.TemplateEngine }
   * @param securityService {@link SecurityService}
   * @param xmlParsingUtils {@link XmlParsingUtils}
   * @param requestContextFactory {@link RequestContextFactory}
   * @param baseTemplateRequestPreProcessor {@link BaseTemplateRequestTemplatePreProcessor}
   * @param objectStorageService {@link ObjectStorageService}
   */
  public RequestTransformerEngine(
      SpringTemplateEngine templateEngine,
      SecurityService securityService,
      XmlParsingUtils xmlParsingUtils,
      RequestContextFactory requestContextFactory,
      BaseTemplateRequestTemplatePreProcessor baseTemplateRequestPreProcessor,
      ObjectStorageService objectStorageService,
      ProvincialRequestProperties provincialRequestProperties) {
    this.templateEngine = templateEngine;
    this.securityService = securityService;
    this.xmlParsingUtils = xmlParsingUtils;
    this.requestContextFactory = requestContextFactory;
    this.baseTemplateRequestPreProcessor = baseTemplateRequestPreProcessor;
    this.objectStorageService = objectStorageService;
    this.provincialRequestProperties = provincialRequestProperties;
  }

  /**
   * This method transforms request to xml and signs the request
   *
   * @param request {@link Request} object which needs to transform
   * @return {@link String} transformed request from provided transaction type.
   */
  public Mono<String> transform(Request<? extends ProvincialRequest> request) {

    return Mono.deferContextual(
        contextView -> {
          var messageProcess = contextView.get(MessageProcess.class);

          return baseTemplateRequestPreProcessor
              .preProcess(request)
              .flatMap(
                  preProcessedRequest -> {
                    Pharmacy pharmacy =
                        preProcessedRequest
                            .getProvincialRequestPayload()
                            .getProvincialRequestControl()
                            .getPharmacy();

                    ProvincialPharmacyCertificateDetails requestCertificateDetails =
                        pharmacy.getCertificate();

                    return createUnsignedRequest(preProcessedRequest)
                        .flatMap(
                            unsignedRequest -> {
                              if (StringUtils.isBlank(unsignedRequest)) {
                                var errorMessage =
                                    String.format(
                                        ErrorConstants.HL7V3_XML_REQUEST_IS_BLANK, messageProcess);
                                var exception = new IllegalArgumentException(errorMessage);
                                log.error(errorMessage, exception);
                                throw exception;
                              }
                              var fallback =
                                  Mono.defer(() -> Mono.just(signRequest(unsignedRequest)));

                              if (isCertificateDetailsCacheable(requestCertificateDetails)) {
                                return fetchCertificate(requestCertificateDetails, pharmacy)
                                    .map(
                                        certificateLocation -> {
                                          var updatedRequestCertificateDetails =
                                              requestCertificateDetails.toBuilder()
                                                  .certificateFileLocation(certificateLocation)
                                                  .build();
                                          return signRequest(
                                              updatedRequestCertificateDetails, unsignedRequest);
                                        })
                                    .onErrorResume(
                                        exception -> {
                                          log.error(
                                              ErrorConstants
                                                  .EXCEPTION_WHILE_FETCHING_PHARMACY_DETAILS,
                                              exception);
                                          return fallback;
                                        });
                              }
                              return fallback;
                            })
                        .onErrorMap(
                            e -> {
                              var exceptionMessage =
                                  String.format(
                                      ErrorConstants.EXCEPTION_MESSAGE_TRANSFORM_PHMS_REQUEST,
                                      messageProcess);
                              var exception = new TransformationException(exceptionMessage, e);
                              log.error(exceptionMessage, exception);
                              return exception;
                            });
                  });
        });
  }

  /**
   * * This method check whether certificate details is cacheable or not
   *
   * @param requestCertificateDetails {@link ProvincialPharmacyCertificateDetails}
   * @return boolean
   */
  private boolean isCertificateDetailsCacheable(
      ProvincialPharmacyCertificateDetails requestCertificateDetails) {
    return getCertificateLocation() != null && requestCertificateDetails != null;
  }

  /**
   * This method returns a certificate key store
   *
   * @param pharmacy {@link Pharmacy}
   * @param requestCertificateDetails {@link ProvincialPharmacyCertificateDetails}
   * @return Mono<ProvincialPharmacyCertificateDetails> {@link ProvincialPharmacyCertificateDetails}
   */
  public Mono<String> fetchCertificate(
      ProvincialPharmacyCertificateDetails requestCertificateDetails, Pharmacy pharmacy) {
    return objectStorageService.fetchCertificateKeyStore(
        getCertificateLocation().concat("/").concat(pharmacy.getId()), requestCertificateDetails);
  }

  /**
   * Returns a Signed String representation of an HL7 V3 XML request from a Unsigned XML String
   * request and Pharmacy Certificate Details
   *
   * @param requestCertificateDetails {@link ProvincialPharmacyCertificateDetails }
   * @param unsignedRequest
   * @return signedRequest
   */
  private String signRequest(
      ProvincialPharmacyCertificateDetails requestCertificateDetails, String unsignedRequest) {
    String signedRequest = null;
    try {
      signedRequest =
          this.xmlParsingUtils.convertXMLDocumentToString(
              this.securityService.signRequestUsingCertificate(
                  unsignedRequest, requestCertificateDetails));
    } catch (Exception e) {
      log.error(ErrorConstants.EXCEPTION_OCCURRED_WHILE_SIGNING_REQUEST, e);
      ExceptionUtils.rethrow(e);
    }
    return signedRequest;
  }

  /**
   * Returns a Signed String representation of an HL7 V3 XML request from a Unsigned XML String *
   * request
   *
   * @param unsignedRequest {@link String}
   * @return signedRequest {@link String}
   */
  private String signRequest(String unsignedRequest) {
    String signedRequest = null;
    try {
      signedRequest =
          this.xmlParsingUtils.convertXMLDocumentToString(
              this.securityService.signRequest(unsignedRequest, false));
    } catch (Exception e) {
      log.error(ErrorConstants.EXCEPTION_OCCURRED_WHILE_SIGNING_REQUEST, e);
      ExceptionUtils.rethrow(e);
    }
    return signedRequest;
  }

  /**
   * Returns a unSigned String representation of an HL7 V3 XML request translated from a PHMS
   * Request object as indicated by the template key
   *
   * @param request {@link Request} object to be translated into an unsigned HL7 V3 XML request
   *     request. If request is null, the service will generate an empty string
   * @return string {@link String} of transformed xml
   */
  protected Mono<String> createUnsignedRequest(Request<? extends ProvincialRequest> request) {

    return Mono.deferContextual(Mono::just)
        .map(
            contextView -> {
              var messageProcess = contextView.get(MessageProcess.class);
              Context context = requestContextFactory.createContext(request, messageProcess);
              return this.templateEngine.process(
                  request
                      .getProvincialRequestPayload()
                      .getProvincialRequestControl()
                      .getProvince()
                      .name()
                      .toLowerCase()
                      .concat(File.separator)
                      .concat(String.valueOf(messageProcess))
                      .concat("-")
                      .concat(TEMPLATE_REQUEST_SUFFIX),
                  context);
            });
  }

  /**
   * This method Get CertificateLocation form configuration
   *
   * @return String {@link String} return a certificateLocation
   */
  private String getCertificateLocation() {
    return this.provincialRequestProperties
        .getRequest()
        .getSecurity()
        .getCertificate()
        .getLocation();
  }
}
