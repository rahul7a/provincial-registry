package com.lblw.vphx.phms.domain.common.response;

import static com.lblw.vphx.phms.domain.common.response.OperationOutcome.buildOperationOutcomeForInternalSystemError;
import static com.lblw.vphx.phms.domain.common.response.OperationOutcome.buildOperationOutcomeForSystemTimeoutError;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Service;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(OperationOutcome.class)
class OperationOutcomeTest {

  @Autowired OperationOutcome operationOutcome;

  @Test
  void buildOperationOutcomeForInternalSystemErrorTest() {

    var operationOutcome =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
            .reason(null)
            .classification(ResponseClassification.SYSTEM)
            .messageProcess(MessageProcess.LOCATION_DETAILS)
            .issues(
                List.of(
                    Issue.builder()
                        .classification(ResponseClassification.SYSTEM)
                        .code("EM.075")
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .description(Strings.EMPTY)
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus(Strings.EMPTY)
                                .build())
                        .details("details")
                        .issueManagementId(null)
                        .issueClosedFlag(null)
                        .issueAcknowledgementFlag(null)
                        .issueManagementRequiredFlag(null)
                        .build()))
            .build();

    assertEquals(
        operationOutcome,
        buildOperationOutcomeForInternalSystemError(
            Source.INTERNAL, ServiceCode.VPHX_PHMS, MessageProcess.LOCATION_DETAILS, "details"));
  }

  @Test
  void buildOperationOutcomeForSystemTimeoutErrorTest() {

    var operationOutcome =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
            .reason(null)
            .classification(ResponseClassification.SYSTEM)
            .messageProcess(MessageProcess.LOCATION_DETAILS)
            .issues(
                List.of(
                    Issue.builder()
                        .classification(ResponseClassification.SYSTEM)
                        .code("EM.093")
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .description(Strings.EMPTY)
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus(Strings.EMPTY)
                                .build())
                        .details("details")
                        .issueManagementId(null)
                        .issueClosedFlag(null)
                        .issueAcknowledgementFlag(null)
                        .issueManagementRequiredFlag(null)
                        .build()))
            .build();

    assertEquals(
        operationOutcome,
        buildOperationOutcomeForSystemTimeoutError(
            Source.INTERNAL, ServiceCode.VPHX_PHMS, MessageProcess.LOCATION_DETAILS, "details"));
  }
}
