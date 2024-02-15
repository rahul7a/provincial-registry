package com.lblw.vphx.phms.domain.patient.consent.response.hl7v3;

import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class ProvincialPatientConsentResponse
    extends Response<ProvincialPatientConsentResponseControlAct, ProvincialPatientConsent> {}
