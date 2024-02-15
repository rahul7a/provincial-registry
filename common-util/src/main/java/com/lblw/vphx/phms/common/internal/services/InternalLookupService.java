package com.lblw.vphx.phms.common.internal.services;

import com.lblw.vphx.phms.domain.medication.Medication;
import com.lblw.vphx.phms.domain.patient.Patient;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacist;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.prescriber.Prescriber;
import com.lblw.vphx.phms.domain.prescription.Prescription;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import reactor.core.publisher.Mono;

/** This interface is used to retrieve information of pharmacy using pharmacy id */
public interface InternalLookupService {

  /**
   * This Method is used to retrieve information of phamracy using system Pharmacy Id
   *
   * @param pharmacyId {@link String}
   * @return {@link Pharmacy}
   */
  Mono<Pharmacy> fetchPharmacy(String pharmacyId);

  /**
   * This Method is used to retrieve information about prescriber using system Prescriber Id
   *
   * @param prescriberId {@link String}
   * @return {@link Prescriber}
   */
  Prescriber fetchPrescriber(String prescriberId);

  /**
   * This Method is used to retrieve information about patient using system Patient Id
   *
   * @param patientId {@link String}
   * @return {@link Patient}
   */
  Patient fetchPatient(String patientId);

  /**
   * This Method is used to retrieve information about medication using system Medication Id
   *
   * @param medicationId {@link String}
   * @return {@link Medication}
   */
  Medication fetchMedication(String medicationId);

  /**
   * Search for a prescription by its system prescription id
   *
   * @param prescriptionId The system prescription id of the prescription you want to search for.
   * @return A {@link Prescription} object.
   */
  Prescription fetchPrescription(String prescriptionId);

  /**
   * It searches for a prescription transaction by its system id.
   *
   * @param prescriptionTransactionId The system prescription transaction id of the prescription
   *     transaction you want to search for.
   * @return A Mono of {@link PrescriptionTransaction} object.
   */
  Mono<PrescriptionTransaction> fetchPrescriptionTransaction(String prescriptionTransactionId);

  /**
   * This method is used to retrieve about Pharmacy store Information using Pharmacy Id
   *
   * @param pharmacyId
   * @return
   * @throws KeyStoreException
   * @throws CertificateException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  Map<String, Object> retrieveStoreObjects(String pharmacyId)
      throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException;

  /**
   * This Method is used to retrieve information of Supervising pharmacist using system Pharmacy Id
   * and work station host name
   *
   * @param pharmacyId {@link String}
   * @param workstationHostname {@link String}
   * @return {@link Pharmacy}
   */
  Mono<Pharmacist> fetchSupervisingPharmacist(String pharmacyId, String workstationHostname);

  /**
   * This Method is used to retrieve information of default pharmacist using Pharmacy Id
   *
   * @param pharmacyId {@link String}
   * @return {@link Pharmacist}
   */
  Mono<Pharmacist> fetchDefaultPharmacist(String pharmacyId);
}
