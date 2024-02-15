package com.lblw.vphx.phms.domain.patient.prescription;

import com.lblw.vphx.phms.domain.common.response.Status;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@RequiredArgsConstructor
@SuperBuilder
public class PrescriptionResult {
    private String hdaRequestId;
    private String storeId;
    private String eventDateTime;
    private String classification;
    private Status hwResponseStatus;
    private String hwEventName;
    private String domainProcessName;
    private String messageLanguageCode;
    private String routingKey;
    private String eventType;
    private String entityId;
    private String entityType;
    private Payload payload;
}
