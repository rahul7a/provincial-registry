package com.lblw.vphx.phms.transformation.services;

import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestHeader;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;

public class TransformRequestsMockFactory {
  public static ProvincialPatientSearchRequest provincialPatientSearchRequestBuilder() {
    var provincialPatientSearchRequest =
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
    return provincialPatientSearchRequest;
  }
}
