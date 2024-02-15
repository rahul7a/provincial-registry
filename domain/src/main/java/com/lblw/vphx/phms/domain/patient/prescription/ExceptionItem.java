package com.lblw.vphx.phms.domain.patient.prescription;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class ExceptionItem {
    private String id;
    private String classification;
    private String hwCode;
    private String hwDescription;
    private String hwDescriptionFrench;
    private String details;
    private String detailsFrench;
    private String priority;
    private String priorityText;
    private String priorityTextFrench;
    private String severity;
    private String severityText;
    private String severityTextFrench;
    private String source;
    private String sourceTextFrench;
    private String issueManagementId;
    private Boolean managementRequired;
    private Boolean isClosed;
    private Boolean acknowledged;
}
