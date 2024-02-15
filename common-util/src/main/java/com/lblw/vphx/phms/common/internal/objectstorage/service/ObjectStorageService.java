package com.lblw.vphx.phms.common.internal.objectstorage.service;

import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.exceptions.InternalProcessorException;
import com.lblw.vphx.phms.common.internal.objectstorage.client.ObjectStorageClient;
import com.lblw.vphx.phms.common.security.services.OAuthClientService;
import com.lblw.vphx.phms.domain.pharmacy.ProvincialPharmacyCertificateDetails;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** ObjectStorage Service to retrieve the certificate for a given for given Vocabulary */
@Service
@Slf4j
public class ObjectStorageService {

  private final OAuthClientService oAuthClientService;
  private final ObjectStorageClient objectStorageClient;

  public ObjectStorageService(
      OAuthClientService oAuthClientService, ObjectStorageClient objectStorageClient) {
    this.oAuthClientService = oAuthClientService;
    this.objectStorageClient = objectStorageClient;
  }

  /**
   * This method saves the certificate data in a temporary file location for message signing purpose
   *
   * @param certificateFolder {@link String} the temporary folder to save this certificate
   * @param provincialPharmacyCertificateDetails {@link ProvincialPharmacyCertificateDetails} the
   *     source metadata of the certificate
   * @return Mono of fileLocation {@link Mono<String>} with updated certificate full path
   */
  public Mono<String> fetchCertificateKeyStore(
      String certificateFolder,
      ProvincialPharmacyCertificateDetails provincialPharmacyCertificateDetails) {
    return Mono.defer(
            () -> {
              Path certificatePath = Path.of(certificateFolder);
              if (!Files.exists(certificatePath)) {
                createCertificateDirectory(certificatePath);
              }

              String fileLocation =
                  certificateFolder
                      .concat("/")
                      .concat(provincialPharmacyCertificateDetails.getCertificateReferenceId())
                      .concat(CommonConstants.CERTIFICATE_FILE_EXT);

              if (provincialPharmacyCertificateDetails.getExpirationDateTime() != null
                  && provincialPharmacyCertificateDetails
                      .getExpirationDateTime()
                      .isBefore(Instant.now())) {
                deleteCertificate(Path.of(fileLocation));
              }

              if (Files.exists(Path.of(fileLocation))) {
                return Mono.just(fileLocation);
              }

              return writeCertificateToFileLocation(
                  fileLocation, provincialPharmacyCertificateDetails.getCertificateReferenceId());
            })
        .onErrorMap(
            error -> {
              var errorMessage = ErrorConstants.EXCEPTION_WHILE_FETCHING_CERTIFICATE;
              var internalProcessorException = new InternalProcessorException(errorMessage, error);
              log.error(errorMessage, internalProcessorException);
              return internalProcessorException;
            });
  }

  /**
   * This method write certificate to the respective file location when it does not exist or expired
   *
   * @param fileLocation {@link String} the temporary folder to save this certificate
   * @param certificateReferenceId {@link String}
   * @return Mono of fileLocation {@link Mono<String>} with updated certificate full path
   */
  private Mono<String> writeCertificateToFileLocation(
      String fileLocation, String certificateReferenceId) {
    return Mono.defer(
        () ->
            oAuthClientService
                .getJWT()
                .flatMap(
                    jwt ->
                        objectStorageClient
                            .getCertificate(certificateReferenceId, jwt)
                            .map(
                                response -> {
                                  persistCertificate(
                                      fileLocation, certificateReferenceId, response);
                                  return fileLocation;
                                })));
  }

  /**
   * This method create certificate directory from given file location ,it's called when directory
   * does not exists
   *
   * @param certificatePath {@link Path}
   */
  private void createCertificateDirectory(Path certificatePath) {
    try {
      Files.createDirectories(certificatePath);
    } catch (IOException ex) {
      var errorMessage =
          String.format(
              "Exception while creating directory  %s, message: %s",
              certificatePath, ex.getMessage());
      log.error(errorMessage);
      throw new InternalProcessorException(errorMessage, ex);
    }
  }

  /**
   * This method delete certificate from given file location , when it's expired
   *
   * @param certificateLocation {@link Path}
   */
  private void deleteCertificate(Path certificateLocation) {
    try {
      Files.deleteIfExists(certificateLocation);
    } catch (IOException ex) {
      var errorMessage =
          String.format(
              "Exception while deleting file %s, message: %s",
              certificateLocation, ex.getMessage());
      log.error(errorMessage);
      throw new InternalProcessorException(errorMessage, ex);
    }
  }

  /**
   * This method create certificate directory from given file location ,it's called when directory
   * does not exists
   *
   * @param fileLocation {@link String}
   * @param certificateReferenceId {@link String}
   * @param certificateResponse this is byte array
   */
  private void persistCertificate(
      String fileLocation, String certificateReferenceId, byte[] certificateResponse) {
    try {
      Files.write(Path.of(fileLocation), certificateResponse);
    } catch (IOException ex) {
      var errorMessage =
          String.format(
              ErrorConstants.EXCEPTION_WHILE_FETCHING_CERTIFICATE_FROM_OBJECT_STORE,
              certificateReferenceId);
      log.error(errorMessage);
      throw new InternalProcessorException(errorMessage, ex);
    }
  }
}
