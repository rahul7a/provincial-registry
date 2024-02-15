package com.lblw.vphx.phms.domain.patient.prescription.transaction.response;

import com.lblw.vphx.phms.domain.common.response.PageList;
import com.lblw.vphx.phms.domain.patient.prescription.ProvincialPrescriptionTransaction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProvincialPrescriptionTransactionList
    extends PageList<ProvincialPrescriptionTransaction> {}
