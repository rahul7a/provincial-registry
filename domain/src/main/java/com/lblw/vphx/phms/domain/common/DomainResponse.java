package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ResponseClassification;
import com.lblw.vphx.phms.domain.common.response.ResponseStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764531731629716&cot=14">Domain
 *     Response</a>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DomainResponse<R> {
  ResponseStatus responseStatus;
  ResponseClassification responseClassification;
  List<OperationOutcome> operationOutcomes;
  R result;
}
