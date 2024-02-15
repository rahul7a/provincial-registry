package com.lblw.vphx.phms.domain.patient.prescription.continuation.response.hl7v3;

import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.domain.patient.prescription.continuation.request.ProvincialPrescriptionSearchContinuationCriteria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ProvincialPrescriptionSearchContinuationResponseControlAct extends ResponseControlAct {
  private ProvincialPrescriptionSearchContinuationCriteria
      provincialPrescriptionSearchContinuationCriteria;
}
