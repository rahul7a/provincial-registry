package com.lblw.vphx.phms.domain.location.details.request.hl7v3;

import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
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
public class ProvincialLocationDetailsRequest extends Request<ProvincialLocationDetailsCriteria> {}
