package com.lblw.vphx.phms.transformation.request;

import static org.mockito.Mockito.*;

import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.internal.mock.MockInternalLookupService;
import com.lblw.vphx.phms.common.internal.objectstorage.service.ObjectStorageService;
import com.lblw.vphx.phms.common.security.services.SecurityService;
import com.lblw.vphx.phms.common.utils.XmlParsingUtils;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestHeader;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.pharmacy.ProvincialPharmacyCertificateDetails;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.request.preprocessors.BaseTemplateRequestTemplatePreProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.w3c.dom.Document;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({
  MockInternalLookupService.class,
  XmlParsingUtils.class,
  SpringTemplateEngine.class,
  SecurityService.class,
  SecurityService.class,
  RequestContextFactory.class,
  BaseTemplateRequestTemplatePreProcessor.class,
  ObjectStorageService.class,
  ProvincialRequestProperties.class,
  RequestTransformerEngine.class
})
class RequestTransformEngineTest {
  @MockBean XmlParsingUtils xmlParsingUtils;
  @MockBean SecurityService securityService;
  @MockBean RequestContextFactory requestContextFactory;
  @MockBean BaseTemplateRequestTemplatePreProcessor baseTemplateRequestPreProcessor;
  @MockBean SpringTemplateEngine springTemplateEngine;
  @MockBean ObjectStorageService objectStorageService;

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  ProvincialRequestProperties provincialRequestProperties;

  @Autowired RequestTransformerEngine requestTransformerEngine;
  private Request request;
  private MessageProcess messageProcess;
  private reactor.util.context.Context requestContext;

  @BeforeEach
  public void beforeEach() {
    when(provincialRequestProperties.getRequest().getSecurity().getCertificate().getLocation())
        .thenReturn("target/certs");

    requestContext =
        reactor.util.context.Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH);
    requestTransformerEngine =
        new RequestTransformerEngine(
            springTemplateEngine,
            securityService,
            xmlParsingUtils,
            requestContextFactory,
            baseTemplateRequestPreProcessor,
            objectStorageService,
            provincialRequestProperties);

    request =
        ProvincialPatientSearchRequest.builder()
            .requestHeader(RequestHeader.builder().build())
            .requestControlAct(
                RequestControlAct.builder()
                    .eventRoot("2.16.840.1.113883.3.40.4.1")
                    .eventCorrelationId("564654")
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                    .transmissionCreationDateTime("20090905100144")
                    .processingCode("D")
                    .senderRoot("2.16.840.1.113883.3.40.5.2")
                    .senderApplicationName("TMPT-RI")
                    .build())
            .provincialRequestPayload(
                ProvincialPatientSearchCriteria.builder()
                    .provincialHealthNumber("LACM30010115")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .pharmacy(Pharmacy.builder().id("123").build())
                            .province(Province.QC)
                            .requestId("564654")
                            .build())
                    .build())
            .build();
    messageProcess = MessageProcess.PATIENT_SEARCH;
  }

  @Test
  void whenTransformingUnsignedRequestThrowsTemplateException_ThenTransformationException() {

    when(springTemplateEngine.process(Mockito.any(String.class), Mockito.any(Context.class)))
        .thenThrow(TemplateInputException.class);

    Context context = new Context();
    context.setVariable("phmsRequest", request);
    when(baseTemplateRequestPreProcessor.preProcess(request)).thenReturn(Mono.just(request));
    when(requestContextFactory.createContext(request, messageProcess)).thenReturn(context);

    StepVerifier.create(requestTransformerEngine.transform(request).contextWrite(requestContext))
        .expectErrorMatches(
            e -> {
              return e instanceof TransformationException
                  && e.getMessage()
                      .equals(
                          String.format(
                              ErrorConstants.EXCEPTION_MESSAGE_TRANSFORM_PHMS_REQUEST,
                              messageProcess));
            })
        .verify();
  }

  @Test
  void whenUnsignedRequestIsBlank_ThenTransformationException() {
    messageProcess = MessageProcess.PATIENT_SEARCH;
    when(springTemplateEngine.process(Mockito.any(String.class), Mockito.any(Context.class)))
        .thenReturn("");

    Context context = new Context();
    context.setVariable("phmsRequest", request);
    when(baseTemplateRequestPreProcessor.preProcess(request)).thenReturn(Mono.just(request));
    when(requestContextFactory.createContext(request, messageProcess)).thenReturn(context);

    StepVerifier.create(requestTransformerEngine.transform(request).contextWrite(requestContext))
        .expectErrorMatches(
            e -> {
              return e instanceof TransformationException
                  && e.getCause()
                      .getMessage()
                      .equals("HL7V3 XML request is blank for [" + messageProcess + "]");
            })
        .verify();
  }

  @Test
  void whenUnsignedRequestIsCorrect_ThenVerifySecurityServiceIsCalled() throws Exception {
    Document xmlDocument = mock(Document.class);
    messageProcess = MessageProcess.PATIENT_SEARCH;
    when(springTemplateEngine.process(Mockito.any(String.class), Mockito.any(Context.class)))
        .thenReturn("Request");
    Context context = new Context();
    context.setVariable("phmsRequest", request);
    String certificateKeyStore = "certificateKeyStore";
    when(baseTemplateRequestPreProcessor.preProcess(request)).thenReturn(Mono.just(request));
    when(requestContextFactory.createContext(request, messageProcess)).thenReturn(context);

    when(objectStorageService.fetchCertificateKeyStore(any(), any()))
        .thenReturn(Mono.just("certificateKeyStore"));

    when(securityService.signRequestUsingCertificate(any(), any())).thenReturn(xmlDocument);
    when(securityService.signRequest(anyString(), anyBoolean())).thenReturn(xmlDocument);
    when(xmlParsingUtils.convertXMLDocumentToString(xmlDocument)).thenReturn("data");

    StepVerifier.create(requestTransformerEngine.transform(request).contextWrite(requestContext))
        .assertNext(
            response -> {
              try {
                verify(securityService).signRequest(anyString(), anyBoolean());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .verifyComplete();
  }

  @Test
  void whenUnsignedRequestIsCorrect_ThenXmlUtilsIsCalled() throws Exception {
    Document document = mock(Document.class);
    String certificateKeyStore = "certificateKeyStore";
    messageProcess = MessageProcess.PATIENT_SEARCH;
    when(springTemplateEngine.process(Mockito.any(String.class), Mockito.any(Context.class)))
        .thenReturn("Request");
    Context context = new Context();
    context.setVariable("phmsRequest", request);

    when(baseTemplateRequestPreProcessor.preProcess(request)).thenReturn(Mono.just(request));
    when(requestContextFactory.createContext(request, messageProcess)).thenReturn(context);

    when(objectStorageService.fetchCertificateKeyStore(any(), any()))
        .thenReturn(Mono.just(certificateKeyStore));
    when(securityService.signRequestUsingCertificate(any(), any())).thenReturn(document);
    when(securityService.signRequest(anyString(), anyBoolean())).thenReturn(document);
    when(xmlParsingUtils.convertXMLDocumentToString(document)).thenReturn("data");

    StepVerifier.create(requestTransformerEngine.transform(request).contextWrite(requestContext))
        .assertNext(
            response -> {
              try {
                verify(xmlParsingUtils).convertXMLDocumentToString(document);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .verifyComplete();
  }

  @Test
  void whenCertifiicateDetailsIsCachable_ThensignRequestUsingCertificateIsCalled()
      throws Exception {
    Document document = mock(Document.class);
    String certificateKeyStore = "certificateKeyStore";
    messageProcess = MessageProcess.PATIENT_SEARCH;
    request
        .getProvincialRequestPayload()
        .getProvincialRequestControl()
        .getPharmacy()
        .setCertificate(
            ProvincialPharmacyCertificateDetails.builder()
                .certificateFileLocation("location")
                .build());
    when(springTemplateEngine.process(Mockito.any(String.class), Mockito.any(Context.class)))
        .thenReturn("Request");
    Context context = new Context();
    context.setVariable("phmsRequest", request);

    when(baseTemplateRequestPreProcessor.preProcess(request)).thenReturn(Mono.just(request));
    when(requestContextFactory.createContext(request, messageProcess)).thenReturn(context);

    when(objectStorageService.fetchCertificateKeyStore(any(), any()))
        .thenReturn(Mono.just(certificateKeyStore));
    when(securityService.signRequestUsingCertificate(any(), any())).thenReturn(document);
    when(securityService.signRequest(anyString(), anyBoolean())).thenReturn(document);
    when(xmlParsingUtils.convertXMLDocumentToString(document)).thenReturn("data");

    StepVerifier.create(requestTransformerEngine.transform(request).contextWrite(requestContext))
        .assertNext(
            response -> {
              try {
                verify(securityService).signRequestUsingCertificate(anyString(), any());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .verifyComplete();
  }
}
