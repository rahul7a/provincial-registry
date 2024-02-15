package com.lblw.vphx.phms.domain.patient.request.hl7v3;

import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProvincialPatientSearchRequest extends Request<ProvincialPatientSearchCriteria> {}
