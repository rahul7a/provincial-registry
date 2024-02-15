package com.lblw.vphx.phms.domain.patient.prescription.details.response.hl7v3;

import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.domain.patient.prescription.details.response.ProvincialPrescriptionDetailsResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class ProvincialPrescriptionDetailsSearchResponse
    extends Response<ResponseControlAct, ProvincialPrescriptionDetailsResponse> {}
