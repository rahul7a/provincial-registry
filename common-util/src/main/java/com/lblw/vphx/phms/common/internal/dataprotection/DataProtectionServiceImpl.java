package com.lblw.vphx.phms.common.internal.dataprotection;

import com.lblw.vphx.phms.common.internal.dataprotection.client.DataProtectionClient;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** Implementation of DataProtectionService that utilises Data Protection Service REST APIs */
@Slf4j
@Service
public class DataProtectionServiceImpl implements DataProtectionService {
  private final DataProtectionClient dataProtectionClient;
  private final Base64.Encoder encoder;
  private final Base64.Decoder decoder;

  /**
   * A constructor
   *
   * @param dataProtectionClient {@link DataProtectionClient}
   */
  public DataProtectionServiceImpl(DataProtectionClient dataProtectionClient) {
    this.dataProtectionClient = dataProtectionClient;
    this.encoder = Base64.getEncoder();
    this.decoder = Base64.getDecoder();
  }

  @Override
  public Mono<byte[]> encrypt(byte[] unencrypted) {
    return dataProtectionClient.deidentifyBlob(new String(unencrypted)).map(decoder::decode);
  }

  @Override
  public Mono<byte[]> decrypt(byte[] encrypted) {
    return dataProtectionClient
        .reidentifyBlob(encoder.encodeToString(encrypted))
        .map(String::getBytes);
  }
}
