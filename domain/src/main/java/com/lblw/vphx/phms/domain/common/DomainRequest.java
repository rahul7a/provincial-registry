package com.lblw.vphx.phms.domain.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764531730278626&cot=14">Domain
 *     Request</a>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class DomainRequest {
  @JsonIgnore ProvincialRequestControl provincialRequestControl;
}
