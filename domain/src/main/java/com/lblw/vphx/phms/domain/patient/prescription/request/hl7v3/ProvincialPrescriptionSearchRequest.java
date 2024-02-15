package com.lblw.vphx.phms.domain.patient.prescription.request.hl7v3;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.patient.prescription.request.ProvincialPrescriptionSearchCriteria;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Request specialisation for Provincial Patient Prescription Search */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class ProvincialPrescriptionSearchRequest
    extends Request<ProvincialPrescriptionSearchCriteria> {
  /** Not expected from client */
  @JsonIgnore private String requestTimestamp;
}
