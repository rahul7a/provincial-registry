package com.lblw.vphx.phms.domain.common.response;

import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Service;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.*;
import org.apache.logging.log4j.util.Strings;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764518875508673&cot=14">Operation
 *     Outcome</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class OperationOutcome {
  public static final String TIMEOUT_ERROR = "System Timeout Error";
  public static final String INTERNAL_SYSTEM_ERROR = "Internal System Error";
  private static final String EM_093 = "EM.093";
  private static final String EM_075 = "EM.075";

  @Schema(description = "Status for the operation outcome")
  private ResponseStatus status;

  @Schema(description = "Reason for the operation outcome")
  private String reason;

  @Schema(description = "Code for an issue request", example = "SYSTEM")
  private ResponseClassification classification;

  @Schema(description = "A list of issues associated with the action", example = "ERROR")
  private List<Issue> issues;

  @Schema(description = "API name", example = "PATIENT_SEARCH")
  private MessageProcess messageProcess;

  /**
   * Creates {@link OperationOutcome} of EM.075 error code for unspecified internal system error.
   *
   * @param source {@link Source}
   * @param triggeredBy {@link Service}
   * @param messageProcess {@link MessageProcess}
   * @param details
   * @return provincial response outcome {@link OperationOutcome}
   */
  public static OperationOutcome buildOperationOutcomeForInternalSystemError(
      Source source, ServiceCode triggeredBy, MessageProcess messageProcess, String details) {

    // TODO - We may need to address the null and empty fields in future.
    return OperationOutcome.builder()
        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
        .reason(null)
        .classification(ResponseClassification.SYSTEM)
        .messageProcess(messageProcess)
        .issues(
            List.of(
                Issue.builder()
                    .classification(ResponseClassification.SYSTEM)
                    .code(EM_075)
                    .priority(
                        IssuePriority.builder().code(Priority.ERROR).text(Strings.EMPTY).build())
                    .severity(
                        IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                    .source(IssueSource.builder().code(source).text(Strings.EMPTY).build())
                    .description(Strings.EMPTY)
                    .triggeredBy(
                        Service.builder()
                            .code(triggeredBy)
                            .serviceOutageStatus(Strings.EMPTY)
                            .build())
                    .details(details)
                    .issueManagementId(null)
                    .issueClosedFlag(null)
                    .issueAcknowledgementFlag(null)
                    .issueManagementRequiredFlag(null)
                    .build()))
        .build();
  }

  /**
   * Creates {@link OperationOutcome} of EM.093 error code for system timeout error.
   *
   * @param source {@link Source}
   * @param triggeredBy {@link Service}
   * @param messageProcess {@link MessageProcess}
   * @param details
   * @return provincial response outcome {@link OperationOutcome}
   */
  public static OperationOutcome buildOperationOutcomeForSystemTimeoutError(
      Source source, ServiceCode triggeredBy, MessageProcess messageProcess, String details) {
    return OperationOutcome.builder()
        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
        .reason(null)
        .classification(ResponseClassification.SYSTEM)
        .messageProcess(messageProcess)
        .issues(
            List.of(
                Issue.builder()
                    .classification(ResponseClassification.SYSTEM)
                    .code(EM_093)
                    .priority(
                        IssuePriority.builder().code(Priority.ERROR).text(Strings.EMPTY).build())
                    .severity(
                        IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                    .source(IssueSource.builder().code(source).text(Strings.EMPTY).build())
                    .description(Strings.EMPTY)
                    .triggeredBy(
                        Service.builder()
                            .code(triggeredBy)
                            .serviceOutageStatus(Strings.EMPTY)
                            .build())
                    .details(details)
                    .issueManagementId(null)
                    .issueClosedFlag(null)
                    .issueAcknowledgementFlag(null)
                    .issueManagementRequiredFlag(null)
                    .build()))
        .build();
  }
}
