package com.lblw.vphx.phms.common.internal.dataprotection;

import reactor.core.publisher.Mono;

/** Interface that wraps async methods for encrypting/decrypting data */
public interface DataProtectionService {
  /**
   * Return a Mono that emits the encrypted data of the given unencrypted data.
   *
   * @param unencrypted The unencrypted data to be encrypted.
   * @return A Mono of a byte array of encrypted data.
   */
  Mono<byte[]> encrypt(byte[] unencrypted);

  /**
   * Return a Mono that emits the decrypted data of the given encrypted data.
   *
   * @param encrypted The encrypted data to be unencrypted.
   * @return A Mono of a byte array of decrypted data.
   */
  Mono<byte[]> decrypt(byte[] encrypted);
}
