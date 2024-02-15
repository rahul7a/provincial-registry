package com.lblw.vphx.phms.domain.location.details.response.hl7v3;

import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class ProvincialLocationDetailsResponse
    extends Response<ProvincialLocationDetailsResponseControlAct, ProvincialLocationDetails> {}
