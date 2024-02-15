package com.lblw.vphx.phms.domain.prescription;

import com.lblw.vphx.phms.domain.patient.Patient;
import com.lblw.vphx.phms.domain.prescriber.Prescriber;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Prescription {
  private String systemIdentifier;
  private Prescriber prescriber;
  private Patient patient;
  private String rxNumber;
  private String prescriberProvincialLocationIdentifier;
  private String prescriberProvincialLocationName;
  private LocalDate writtenDate;
  private LocalDate refillUntilDate;
  private LocalDate expiryDate;
  private String prescriptionSource;
  private String workflowType;
  private String unit;
  private Double prescribedQuantity;
  private Double totalAuthorizedQuantity;
  private String createdBy;
  private PrescriptionSig sig;
  private List<String> prescriptionStatus;
  private Boolean trialSupplyFlag;
  private String previousFillStatusCode;
  private String fillSubStatus;
  private String refills;
  private String remainingRefills;
  private Boolean authoritativeFlag;
  private String prescriptionStatusReason;
  private String remainingQuantity;
  private Boolean legacyFlag;
  private String provincialPrescriptionId;
  private String productSelection;
}
