package com.lblw.vphx.phms.common.internal.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Implementation of CompressionService via Gzip algorithm */
@Slf4j
@Service
public class GzipCompressionService implements CompressionService {
  /**
   * Compress binary data via gzip
   *
   * @param data The data to compress.
   * @return A Mono<byte[]> of compressed binary data
   */
  @Override
  public Mono<byte[]> compress(byte[] data) {
    return Mono.defer(
            () -> {
              try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
                try (var gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                  gzipOutputStream.write(data);
                }
                return Mono.just(byteArrayOutputStream.toByteArray());
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            })
        .subscribeOn(Schedulers.parallel());
  }

  /**
   * Decompress gzip compressed binary data
   *
   * @param data The data to decompress.
   * @return A Mono<byte[]> of decompressed binary data
   */
  @Override
  public Mono<byte[]> decompress(byte[] data) {
    return Mono.defer(
            () -> {
              if (isCompressed(data)) {
                try (var byteArrayInputStream = new ByteArrayInputStream(data)) {
                  try (var gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
                    return Mono.just(gzipInputStream.readAllBytes());
                  }
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              }
              return Mono.just(data);
            })
        .subscribeOn(Schedulers.parallel());
  }

  /**
   * To check whether provided bytes is compressed or not
   *
   * @param bytes
   * @return boolean value
   */
  private boolean isCompressed(byte[] bytes) {
    return (bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
        && (bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
  }
}
