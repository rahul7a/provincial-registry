package com.lblw.vphx.phms.domain.patient.prescription.discontinuation.request;

import com.lblw.vphx.phms.domain.common.DomainRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionDiscontinuationRequest extends DomainRequest {
  private String prescriptionSystemId;
  private String prescriptionTransactionSystemId;
}
