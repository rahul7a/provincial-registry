package com.lblw.vphx.phms.common.internal.compression;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GzipCompressionServiceTest {
  CompressionService compressionService = new GzipCompressionService();

  @Test
  void givenSome_whenCompressedAndUncompressed_thenReturnsOriginalData() {
    var data = "{\"test\": \"josn\"}";
    var result =
        compressionService
            .compress(data.getBytes())
            .flatMap(compressionService::decompress)
            .map(String::new)
            .block();
    Assertions.assertEquals(data, result);
  }

  @Test
  void givenSome_whenUncompressed_thenReturnsOriginalData() {
    var data = "{\"test\": \"josn\"}";
    var bytes = data.getBytes();
    var result = compressionService.decompress(bytes).block();

    Assertions.assertEquals(bytes, result);
  }
}
