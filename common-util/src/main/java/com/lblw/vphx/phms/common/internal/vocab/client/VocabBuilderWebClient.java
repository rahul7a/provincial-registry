package com.lblw.vphx.phms.common.internal.vocab.client;

import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.exceptions.InternalAPIClientException;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.common.internal.vocab.configuration.VocabWebClientConfiguration;
import com.lblw.vphx.phms.common.internal.vocab.constants.VocabConstants;
import com.lblw.vphx.phms.common.internal.vocab.dto.VocabResponse;
import com.lblw.vphx.phms.common.internal.vocab.dto.VocabSchema;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.Province;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/** VocabBuilderWebClient to invoke reference data service */
@Slf4j
@Component
public final class VocabBuilderWebClient {

  private final WebClient rdsVocabBuilderWebClient;
  private final InternalApiConfig internalApiConfig;

  /**
   * public constructor
   *
   * @param rdsVocabBuilderWebClient {@link WebClient}
   * @param internalApiConfig {@link InternalApiConfig}
   */
  public VocabBuilderWebClient(
      @Qualifier(VocabWebClientConfiguration.VOCAB_BUILDER_WEB_CLIENT_QUALIFIER)
          WebClient rdsVocabBuilderWebClient,
      InternalApiConfig internalApiConfig) {
    this.rdsVocabBuilderWebClient = rdsVocabBuilderWebClient;
    this.internalApiConfig = internalApiConfig;
  }

  /**
   * Calls the reference data service for Vocabulary value look up
   *
   * @param vocabType Vocab Type
   * @param province Province
   * @return {@link ResponseEntity}
   */
  public Mono<VocabResponse> callReferenceDataService(String vocabType, String province) {

    return rdsVocabBuilderWebClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .queryParam(VocabConstants.Params.PROVINCE_QUERY_PARAM, province)
                    .build(vocabType))
        .retrieve()
        .onStatus(
            httpStatus -> !httpStatus.is2xxSuccessful(),
            clientResponse -> {
              var errorMessage =
                  String.format(
                      ErrorConstants.EXCEPTION_WHILE_FETCHING_VOCAB_BUILDER_VALUE,
                      vocabType,
                      province);
              log.error(
                  errorMessage
                      .concat(ErrorConstants.WITH_STATUS_CODE)
                      .concat(clientResponse.statusCode().toString()));
              throw new InternalAPIClientException(errorMessage);
            })
        .bodyToMono(VocabResponse.class)
        .onErrorMap(
            cause -> {
              var errorMessage =
                  String.format(
                      ErrorConstants.EXCEPTION_WHILE_FETCHING_VOCAB_BUILDER_VALUE,
                      vocabType,
                      province);
              log.error(errorMessage);
              return new InternalAPIClientException(errorMessage, cause);
            });
  }

  /**
   * This function calls the reference data service to get the coding object for a given province,
   * code type and code when fetchCodingByCerxCode is false
   *
   * @param vocabType The vocabulary type, e.g. "TreatmentType"
   * @param province The province (e.g. QC, BC, AB, etc.)
   * @param cerxCode The province code you want to look up.
   * @return An Optional<Coding> object.
   */
  public Optional<Coding> getHwCodingByCerxCode(
      String vocabType, String province, String cerxCode) {
    return callRefDataForGivenCode(
        vocabType,
        province,
        cerxCode,
        VocabConstants.Params.CERX_CODE_QUERY_PARAM,
        vocabSchema -> {
          if (StringUtils.isBlank(vocabSchema.getHwCode())) {
            return null;
          }

          return Coding.builder()
              .province(Province.ALL)
              .code(vocabSchema.getHwCode())
              .display(
                  new LocalizedText[] {
                    LocalizedText.builder()
                        .text(vocabSchema.getHwDescription())
                        .language(LanguageCode.ENG)
                        .build(),
                    LocalizedText.builder()
                        .text(vocabSchema.getHwDescriptionFrench())
                        .language(LanguageCode.FRA)
                        .build()
                  })
              .system(VocabConstants.SYSTEM)
              .build();
        });
  }

  /**
   * This function calls the reference data service to get the coding object for a given province,
   * code type and code when fetchCodingByCerxCode is true
   *
   * @param vocabType The vocabulary type, e.g. "TreatmentType"
   * @param province The province (e.g. QC, BC, AB, etc.)
   * @param cerxCode The province code you want to look up.
   * @return An Optional<Coding> object mapping to unknown/nullFlavor coding
   */
  public Optional<Coding> getCerxCodingByCerxCode(
      String vocabType, Province province, String cerxCode) {
    return callRefDataForGivenCode(
        vocabType,
        province.name(),
        cerxCode,
        VocabConstants.Params.CERX_CODE_QUERY_PARAM,
        vocabSchema ->
            Coding.builder()
                .province(province)
                .code(vocabSchema.getCerxCode())
                .display(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text(vocabSchema.getCerxDisplayName())
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text(vocabSchema.getCerxDisplayNameFrench())
                          .language(LanguageCode.FRA)
                          .build()
                    })
                .system(VocabConstants.SYSTEM)
                .build());
  }

  /**
   * This function calls the reference data service for a given code type and code value when
   * fetchCodingByCerxCode is false
   *
   * @param vocabType The vocabulary type. For example, "ProviderRoleType" or "RouteOfAdmin".
   * @param province The province for which the code is being looked up.
   * @param hwCode The VPHX/HW code that you want to look up in the reference data.
   * @return Optional<Coding>
   */
  public Optional<Coding> getCerxCodingByHwCode(
      String vocabType, Province province, String hwCode) {
    return callRefDataForGivenCode(
        vocabType,
        province.name(),
        hwCode,
        VocabConstants.Params.HW_CODE_QUERY_PARAM,
        vocabSchema -> {
          if (StringUtils.isBlank(vocabSchema.getHwCode())) {
            return null;
          }

          return Coding.builder()
              .province(province)
              .code(vocabSchema.getCerxCode())
              .display(
                  new LocalizedText[] {
                    LocalizedText.builder()
                        .text(vocabSchema.getCerxDisplayName())
                        .language(LanguageCode.ENG)
                        .build(),
                    LocalizedText.builder()
                        .text(vocabSchema.getCerxDisplayNameFrench())
                        .language(LanguageCode.FRA)
                        .build()
                  })
              .system(VocabConstants.SYSTEM)
              .build();
        });
  }

  /**
   * It takes in a vocab type, province, code and code type and returns a Coding object
   *
   * @param vocabType The type of vocabulary you want to search.
   * @param province The province for which the code is being looked up.
   * @param code The code to be looked up
   * @param codeType This is the type of code that is being passed in. It can be either a HW code or
   *     a CERX code.
   * @param mapper Map VocabSchema to required coding
   * @return A coding object
   */
  // TODO: Return List<Coding>
  private Optional<Coding> callRefDataForGivenCode(
      String vocabType,
      String province,
      String code,
      String codeType,
      Function<VocabSchema, Coding> mapper) {

    RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
    HttpHeaders headers = new HttpHeaders();

    HttpEntity<String> entity = new HttpEntity<>(headers);
    URI url =
        UriComponentsBuilder.fromUriString(internalApiConfig.getRds().getBaseUrl())
            .path(internalApiConfig.getRds().getVocabPath())
            .queryParam(VocabConstants.Params.PROVINCE_QUERY_PARAM, province)
            .queryParam(codeType, code)
            .build(vocabType);

    ResponseEntity<VocabResponse> response;
    try {
      response =
          restTemplateBuilder.build().exchange(url, HttpMethod.GET, entity, VocabResponse.class);
    } catch (Exception e) {
      var errorMessage =
          String.format(
              ErrorConstants.EXCEPTION_WHILE_FETCHING_VOCAB_BUILDER_VALUE,
              vocabType,
              code,
              province);
      log.error(errorMessage);
      return Optional.empty();
    }

    if (!response.getStatusCode().is2xxSuccessful()) {
      var errorMessage =
          String.format(
                  ErrorConstants.EXCEPTION_WHILE_FETCHING_VOCAB_BUILDER_VALUE,
                  vocabType,
                  code,
                  province)
              .concat(ErrorConstants.WITH_STATUS_CODE)
              .concat(String.valueOf(response.getStatusCode()));
      log.info(errorMessage);
      throw new InternalAPIClientException(errorMessage);
    }
    VocabResponse responseBody = response.getBody();

    if (responseBody == null || responseBody.getQuebecVocabularies() == null) {
      return Optional.empty();
    }

    List<VocabSchema> vocabSchemas =
        responseBody.getQuebecVocabularies().stream()
            .filter(vocabSchema -> StringUtils.isNotBlank(vocabSchema.getCerxCode()))
            .collect(Collectors.toList());

    return vocabSchemas.stream()
        .findFirst()
        .flatMap(vocabSchema -> Optional.ofNullable(mapper.apply(vocabSchema)));
  }
}
