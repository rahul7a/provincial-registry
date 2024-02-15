package com.lblw.vphx.phms.domain.patient.prescription.transaction.request.hl7v3;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.patient.prescription.transaction.request.ProvincialPrescriptionTransactionSearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString(callSuper = true)
public class ProvincialPrescriptionTransactionSearchRequest
    extends Request<ProvincialPrescriptionTransactionSearchCriteria> {
  /** Not expected from client */
  @JsonIgnore private String requestTimestamp;

  /** Not expected from client */
  @JsonIgnore private String requestDataVersion;
}
