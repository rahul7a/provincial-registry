package com.lblw.vphx.phms.common.internal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.exceptions.ProvincialProcessorException;
import com.lblw.vphx.phms.common.internal.vocab.constants.VocabConstants;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.User;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacist;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ProvincialRequestControlEnrichService.class, InternalLookupServiceImpl.class})
class ProvincialRequestControlEnrichServiceTest {
  private static final String BEARER_TOKEN = "bearerToken";
  @MockBean InternalLookupService internalLookupService;
  @Autowired ProvincialRequestControlEnrichService provincialRequestControlEnrichService;
  private Context commonRequestContext;
  private ProvincialRequestControl provincialRequestControl;

  @BeforeEach
  void beforeEach() {
    provincialRequestControl =
        ProvincialRequestControl.builder()
            .requestId("requestId")
            .province(Province.QC)
            .pharmacy(Pharmacy.builder().id("pharmacyId").storeNumber("0")
                    .supervisingPharmacist(Pharmacist.builder()
                            .firstName("first")
                            .build()).build())
            .build();

    commonRequestContext =
        Context.of(
            ProvincialRequestControl.class,
            provincialRequestControl,
            MessageProcess.class,
            MessageProcess.PRESCRIPTION_SEARCH_CONTINUATION,
            HttpHeaders.AUTHORIZATION,
            BEARER_TOKEN);
  }

  @Test
  void whenEnrichWithProvincialLocationAndEnrichWithProvincialProvider_doNothing() {
    var pharmacy =
        Pharmacy.builder()
            .name("N")
            .provincialLocation(
                Pharmacy.ProvincialLocation.builder()
                    .identifier(Pharmacy.Identifier.builder().value("1123465789").build())
                    .name("N")
                    .locationType(Pharmacy.LocationType.builder().province(Province.QC).build())
                    .telecom(Pharmacy.Telecom.builder().number("1234567890").build())
                    .address(
                        Pharmacy.Address.builder()
                            .streetAddressLine1("streetLineAddress")
                            .streetAddressLine2("streetAddressLine2")
                            .postalCode("postalCode")
                            .city("city")
                            .country("country")
                            .build())
                    .build())
            .build();

    var user =
        User.builder()
            .firstName("ROGERE")
            .lastName("MOQUIN")
            .permitNumber("19650214")
            .provincialId("CPN.00000128.QC.PRS")
            .providerRole(
                Coding.builder()
                    .code("1940000")
                    .province(Province.ALL)
                    .system(VocabConstants.SYSTEM)
                    .display(
                        new LocalizedText[] {
                          LocalizedText.builder()
                              .text("PHYSICIAN")
                              .language(LanguageCode.ENG)
                              .build(),
                          LocalizedText.builder().text("MÉDECIN").language(LanguageCode.FRA).build()
                        })
                    .build())
            .build();
    when(internalLookupService.fetchPharmacy("pharmacyId")).thenReturn(Mono.just(pharmacy));

    provincialRequestControlEnrichService.enrich().contextWrite(commonRequestContext).subscribe();
    // match user
    assertEquals(user, provincialRequestControl.getUser());
    // match pharmacy
    assertEquals("pharmacyId", provincialRequestControl.getPharmacy().getId());
    assertEquals("0", provincialRequestControl.getPharmacy().getStoreNumber());
    assertEquals(pharmacy.getName(), provincialRequestControl.getPharmacy().getName());
    assertEquals(pharmacy.getSupervisingPharmacist().getFirstName(), provincialRequestControl.getPharmacy().getSupervisingPharmacist().getFirstName());

    assertEquals(
        pharmacy.getCertificate(), provincialRequestControl.getPharmacy().getCertificate());
    assertEquals(
        pharmacy.getProvincialLocation(),
        provincialRequestControl.getPharmacy().getProvincialLocation());
  }

  @Test
  void whenEnrichWithProvincialLocation_throwError_doNothing() {

    when(internalLookupService.fetchPharmacy(anyString()))
        .thenReturn(Mono.error(new ProvincialProcessorException("fail to fetch pharmacy details")));

    StepVerifier.create(
            provincialRequestControlEnrichService.enrich().contextWrite(commonRequestContext))
        .verifyErrorMessage("fail to fetch pharmacy details");
    StepVerifier.create(
            provincialRequestControlEnrichService.enrich().contextWrite(commonRequestContext))
        .verifyError(ProvincialProcessorException.class);
  }
}
