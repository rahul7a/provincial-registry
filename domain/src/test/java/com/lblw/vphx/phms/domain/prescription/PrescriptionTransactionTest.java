package com.lblw.vphx.phms.domain.prescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrescriptionTransactionTest {

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class GetOrNullTests {
        private Stream<Arguments> workFlowAndAction() {
            return Stream.of(
                    arguments("CANCELLED", "APPROVE", "firstName"),
                    arguments("CLINICAL_VERIFICATION", "APPROVE", "firstName"),
                    arguments(null, "APPROVE", null),
                    arguments("", "APPROVE", null),
                    arguments("CLINICAL_VERIFICATION", "", null),
                    arguments("CLINICAL_VERIFICATION", null, null),
                    arguments("", "", null),
                    arguments(null, null, null)

            );
        }

        @ParameterizedTest
        @MethodSource("workFlowAndAction")
        void getAuditUserDetailsByWorkflowStatusAndAction(String workflowStatus, String action, String expectedFirstName) {
            AuditUserDetails mockAuditUserDetails = AuditUserDetails.builder()
                    .firstName("firstName")
                    .lastName("lastName")
                    .licenceProvince("licenceProvince")
                    .licenceNumber("licenceNumber")
                    .idpUserId("userId")
                    .state("ACTIVE")
                    .build();
            PrescriptionTransaction prescriptionTransaction = PrescriptionTransaction.builder()
                    .auditDetails(
                            List.of(
                                    AuditDetails.builder()
                                            .workFlowStatus(workflowStatus)
                                            .action(action)
                                            .userId("userId")
                                            .auditDateTime("2022-07-26T00:00:00.00Z")
                                            .build())
                    )
                    .auditUserDetails(List.of(mockAuditUserDetails)).build();

            Optional<AuditUserDetails> auditUserDetails = prescriptionTransaction
                    .getAuditUserDetailsByWorkflowStatusAndAction(workflowStatus, action);
            String responseFirstName = auditUserDetails.isPresent() ? auditUserDetails.get().getFirstName() : null;
            assertEquals(responseFirstName, expectedFirstName);
        }
    }

}