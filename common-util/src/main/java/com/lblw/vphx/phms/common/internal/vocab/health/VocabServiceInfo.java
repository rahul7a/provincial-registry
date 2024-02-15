package com.lblw.vphx.phms.common.internal.vocab.health;

import com.lblw.vphx.phms.common.internal.vocab.VocabService;
import com.lblw.vphx.phms.common.internal.vocab.runner.VocabBuilderRunner;
import com.lblw.vphx.phms.domain.coding.CodeableConceptType;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.Province;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

/**
 * Actuator End Point controller for Vocab Service
 *
 * <p>Accessible at GET: /actuator/vocab-service-info
 */
@Configuration
@RestControllerEndpoint(id = "vocab-service-info")
@AllArgsConstructor
@Slf4j
public class VocabServiceInfo {

  private final VocabService vocabService;

  private final VocabBuilderRunner vocabBuilderRunner;

  /**
   * Retrieves All Populated Provincial Codes
   *
   * @return {@link ResponseEntity}
   */
  @GetMapping(value = "/")
  public Mono<ResponseEntity<Map<String, Object>>> getStatus() {
    return Mono.defer(
        () -> {
          Map<String, Object> status = new HashMap<>();

          status.put(
              "totalAvailableProvincialVocabularies",
              vocabService.getSystemCodesToProvincialCodingsMap().size());

          Map<String, List<Map<String, Integer>>> provincialCodeStatsByProvince = new HashMap<>();

          vocabService.getSystemCodesToProvincialCodingsMap().keySet().stream()
              .map(Pair::getSecond)
              .collect(Collectors.toList())
              .forEach(
                  province -> {
                    List<Map<String, Integer>> statsByVocabName = new ArrayList<>();
                    vocabService.getSystemCodesToProvincialCodingsMap().entrySet().stream()
                        .filter(
                            pairListEntry -> pairListEntry.getKey().getSecond().equals(province))
                        .forEach(
                            pairListEntry ->
                                statsByVocabName.add(
                                    Map.of(
                                        pairListEntry.getKey().getFirst().name(),
                                        pairListEntry.getValue().size())));
                    provincialCodeStatsByProvince.put(province.name(), statsByVocabName);
                  });

          status.put("provincialVocabulariesDetailsByProvince", provincialCodeStatsByProvince);

          status.put(
              "totalAvailableSystemVocabularies",
              vocabService.getProvincialCodesToSystemCodingsMap().size());

          Map<String, List<Map<String, Integer>>> systemCodeStatsByProvince = new HashMap<>();

          vocabService.getSystemCodesToProvincialCodingsMap().keySet().stream()
              .map(Pair::getSecond)
              .collect(Collectors.toList())
              .forEach(
                  province -> {
                    List<Map<String, Integer>> statsByVocabName = new ArrayList<>();
                    vocabService.getProvincialCodesToSystemCodingsMap().entrySet().stream()
                        .filter(
                            pairListEntry -> pairListEntry.getKey().getSecond().equals(province))
                        .forEach(
                            pairListEntry ->
                                statsByVocabName.add(
                                    Map.of(
                                        pairListEntry.getKey().getFirst().name(),
                                        pairListEntry.getValue().size())));
                    systemCodeStatsByProvince.put(province.name(), statsByVocabName);
                  });

          status.put("systemVocabulariesDetailsByProvince", systemCodeStatsByProvince);

          return Mono.just(new ResponseEntity<>(status, HttpStatus.OK));
        });
  }

  /**
   * Retrieves All Populated System Codes for given vocabulary and province
   *
   * @return {@link ResponseEntity}
   */
  @GetMapping("/system-codes")
  public @ResponseBody Mono<
          ResponseEntity<
              Map<Pair<CodeableConceptType, Province>, ConcurrentHashMap<String, Coding>>>>
      getSystemCodes() {

    return Mono.just(
        new ResponseEntity<>(vocabService.getProvincialCodesToSystemCodingsMap(), HttpStatus.OK));
  }

  /**
   * Retrieves All Populated Provincial Codes
   *
   * @return {@link ResponseEntity}
   */
  @GetMapping("/provincial-codes")
  public @ResponseBody Mono<
          ResponseEntity<
              Map<Pair<CodeableConceptType, Province>, ConcurrentHashMap<String, Coding>>>>
      getProvincialCodes() {

    return Mono.just(
        new ResponseEntity<>(vocabService.getSystemCodesToProvincialCodingsMap(), HttpStatus.OK));
  }
}
