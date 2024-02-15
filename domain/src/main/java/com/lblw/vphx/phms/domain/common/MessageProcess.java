package com.lblw.vphx.phms.domain.common;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530747486621&cot=14">Message
 *     Process</a>
 */
// TODO: Need to move this class to common.context package
public enum MessageProcess {
  PATIENT_SEARCH("findCandidateQuery"),
  PROVIDER_SEARCH("findProviderQuery"),
  LOCATION_SEARCH("findLocationSummary"),
  LOCATION_DETAILS("findLocationDetails"),
  PATIENT_CONSENT("getPatientConsent"),
  PRESCRIPTION_SEARCH("provincialPatientPrescription"),
  PRESCRIPTION_SEARCH_CONTINUATION("provincialPatientPrescriptionContinuation"),
  PRESCRIPTION_TRANSACTION_SEARCH("provincialPatientPrescriptionTransaction"),
  PRESCRIPTION_TRANSACTION_SEARCH_CONTINUATION(
      "provincialPatientPrescriptionTransactionContinuation"),
  PRESCRIPTION_CREATION("provincialPatientPrescriptionCreation"),
  PRESCRIPTION_TRANSACTION_CREATION("provincialPatientPrescriptionTransactionCreation"),
  PRESCRIPTION_DETAILS("provincialPatientPrescriptionDetails"),
  PRESCRIPTION_CANCELLATION("provincialPatientPrescriptionCancellation"),
  PRESCRIPTION_DISCONTINUE("provincialPatientPrescriptionDiscontinuation"),
  PRESCRIPTION_TRANSACTION_CANCELLATION("provincialPatientPrescriptionTransactionCancellation"),
  NON_PRESCRIBED_PRESCRIPTION_CREATION("provincialPatientNonPrescribedPrescriptionCreation");
  private final String name;

  MessageProcess(String name) {
    this.name = name;
  }
  /**
   * @return name of transaction
   */
  public String getName() {
    return this.name;
  }
}
