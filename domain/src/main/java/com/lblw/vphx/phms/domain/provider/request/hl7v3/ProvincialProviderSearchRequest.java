package com.lblw.vphx.phms.domain.provider.request.hl7v3;

import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProvincialProviderSearchRequest extends Request<ProvincialProviderSearchCriteria> {}
