package com.lblw.vphx.phms.common.internal.compression;

import reactor.core.publisher.Mono;

/** Interface that wraps async methods for compression/decompression of data */
public interface CompressionService {
  /**
   * Compress binary data
   *
   * @param data The data to compress.
   * @return A Mono<byte[]> of compressed binary data
   */
  Mono<byte[]> compress(byte[] data);

  /**
   * Decompress compressed binary data
   *
   * @param data The data to decompress.
   * @return A Mono<byte[]> of decompressed binary data
   */
  Mono<byte[]> decompress(byte[] data);
}
