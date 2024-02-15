package com.lblw.vphx.phms.domain.common.response;

import com.lblw.vphx.phms.domain.common.Service;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764522330065737&cot=14">Issue</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Issue {
  @Schema(description = "Issue ID")
  private String issueId;

  @Schema(description = "Classification of resulting issue", example = "SYSTEM")
  private ResponseClassification classification;

  @Schema(
      description =
          "Client error or System error code. [Reference](https://confluence.lblw.cloud/display/VP/PHMS+Errors)",
      example = "EM.173")
  private String code;

  @Schema(description = "Indicates the priority of the issue")
  private IssuePriority priority;

  @Schema(description = "Indicates the severity and description of the issue")
  private IssueSeverity severity;

  @Schema(description = "Indicates the source of the issue")
  private IssueSource source;

  @Schema(description = "issue description")
  private String description;

  @Schema(description = "Service object that represents the service that triggered this issue")
  private Service triggeredBy;

  @Schema(description = "Issue details")
  private String details;

  @Schema(description = "Indicates the ID of the issue in the management system")
  private String issueManagementId;

  @Schema(description = "Indicates whether the issue is closed or not")
  private Boolean issueClosedFlag;

  @Schema(description = "Indicates whether the issue is acknowledged or not")
  private Boolean issueAcknowledgementFlag;

  @Schema(description = "Indicates whether the issue requires management or not")
  private Boolean issueManagementRequiredFlag;
}
