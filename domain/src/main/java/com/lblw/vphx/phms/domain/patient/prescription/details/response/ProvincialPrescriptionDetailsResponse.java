package com.lblw.vphx.phms.domain.patient.prescription.details.response;

import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.patient.prescription.response.ProvincialPrescription;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionDetailsResponse extends ProvincialResponse {
  private ProvincialPrescription provincialPrescription;
}
