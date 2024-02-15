package com.lblw.vphx.phms.domain.patient.prescription.response;

import com.lblw.vphx.phms.domain.common.response.PageList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProvincialPrescriptionPageList extends PageList<ProvincialPrescription> {}
