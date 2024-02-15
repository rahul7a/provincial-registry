package com.lblw.vphx.phms.domain.common.logs;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530746338472&cot=14">DomainEntity</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DomainEntity {
  private String systemIdentifier;
  private Integer number;
  private DomainEntityType domainEntityType;
}
