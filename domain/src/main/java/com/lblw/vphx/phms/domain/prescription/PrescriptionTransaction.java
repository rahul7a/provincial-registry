package com.lblw.vphx.phms.domain.prescription;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lblw.vphx.phms.domain.medication.Medication;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764526944786510&cot=14">Prescription
 *     Transaction</a>
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PrescriptionTransaction {
  // TODO: rename to systemId
  private String systemIdentifier;
  private String fillStatus;
  private Pharmacy pharmacy;
  private Medication medication;
  private Prescription prescription;
  private Integer supplyDays;
  private Integer intervalDays;
  private LocalDate serviceDate;
  private String createdDate;
  private Double dispensedQuantity;
  private String dispenseSource;
  private String txNumber;
  private String dispenseType;
  private VirtualBasket virtualBasket;
  private String cancelReason;
  private List<AuditDetails> auditDetails;
  private List<AuditUserDetails> auditUserDetails;

  private Product product;

  private String workflowType;

  private String substitutionReason;

  @JsonIgnore
  public Optional<AuditUserDetails> getAuditUserDetailsByWorkflowStatusAndAction(String workflowStatus, String action) {
    return this.getAuditDetails()
            .stream()
            .filter(ad -> !StringUtils.isEmpty(workflowStatus) && ad.getWorkFlowStatus().equalsIgnoreCase(workflowStatus) && !StringUtils.isEmpty(action) && ad.getAction().equalsIgnoreCase(action))
            .findFirst()
            .flatMap(auditDetail -> this.getAuditUserDetails()
                .stream()
                .filter(adtUserDtls -> adtUserDtls.getIdpUserId().contentEquals(auditDetail.getUserId()))
                .findFirst());
  }

}
