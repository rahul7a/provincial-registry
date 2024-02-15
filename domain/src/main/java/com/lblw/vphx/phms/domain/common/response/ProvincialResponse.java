package com.lblw.vphx.phms.domain.common.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class ProvincialResponse {
  @JsonIgnore private ProvincialResponseAcknowledgement provincialResponseAcknowledgement;
}
