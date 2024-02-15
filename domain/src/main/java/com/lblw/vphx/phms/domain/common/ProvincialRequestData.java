package com.lblw.vphx.phms.domain.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764531789962448&cot=14">Provincial
 *     Request Data</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public abstract class ProvincialRequestData<D extends DomainRequest> {
  @JsonIgnore Instant domainRequestTimestamp;
  @JsonIgnore Instant provincialRequestTimestamp;
  @JsonIgnore Instant originalRequestTimestamp;
  D request;
}
