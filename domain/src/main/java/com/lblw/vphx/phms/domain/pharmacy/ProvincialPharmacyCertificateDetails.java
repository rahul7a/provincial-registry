package com.lblw.vphx.phms.domain.pharmacy;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProvincialPharmacyCertificateDetails {
  private String certificateReferenceId;
  private String password;
  // TODO: private passwords to be stored elsewhere
  private String privateKeyPassword;
  private String alias;
  private Instant expirationDateTime;
  private String version;
  private String certificateFileLocation;
}
