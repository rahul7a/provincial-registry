package com.lblw.vphx.phms.common.utils;

import static com.lblw.vphx.phms.common.constants.CommonConstants.EXCEPTION_IN_PARSING_JWT_TOKEN;
import static com.lblw.vphx.phms.common.constants.CommonConstants.EXCEPTION_IN_PARSING_NULL_OR_BLANK_JWT_CLAIM;

import com.lblw.vphx.iams.securityauditengine.security.RequestDetails;
import com.lblw.vphx.iams.securityauditengine.security.VPHJwtDecoder;
import com.lblw.vphx.phms.common.exceptions.RequestNotAcceptableException;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.common.Language;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.User;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

/**
 * This class contains utility methods for controllers and supporting classes like ControllerAdvice,
 * Interceptors, Filters...
 */
@Component
public class ControllerUtils {
  /**
   * Creates {@link ProvincialRequestControl} from {@link HttpHeaders#AUTHORIZATION} header value if
   * present.
   *
   * @param serverHttpRequest {@link ServerHttpRequest}
   * @return provincialRequestControl {@link ProvincialRequestControl}
   */
  public ProvincialRequestControl buildProvincialRequestControl(
      ServerHttpRequest serverHttpRequest) {
    // helper method to parse JWT claims into RequestDetails
    RequestDetails jwtClaims = null;
    try {
      jwtClaims = VPHJwtDecoder.extractRequestDetails(serverHttpRequest);
    } catch (Exception ex) {
      throw new JwtException(EXCEPTION_IN_PARSING_JWT_TOKEN, ex);
    }
    if (jwtClaims == null) {
      throw new JwtException(EXCEPTION_IN_PARSING_NULL_OR_BLANK_JWT_CLAIM);
    }
    LanguageCode languageCode = null;
    if (jwtClaims.getLanguageCode() != null && jwtClaims.getLanguageCode().name().equals("ENG")) {
      languageCode = LanguageCode.ENG;
    } else {
      languageCode = LanguageCode.FRA;
    }
    return ProvincialRequestControl.builder()
        .pharmacy(
            Pharmacy.builder()
                .id(jwtClaims.getPharmacyId())
                .storeNumber(jwtClaims.getStoreId())
                .workstationHostname(jwtClaims.getMachineName())
                .build())
        .requestId(jwtClaims.getRequestId())
        .province(Province.valueOf(jwtClaims.getProvinceCode().name()))
        .user(User.builder().idpUserId(jwtClaims.getIdpUserId()).build())
        .language(Language.builder().code(languageCode).build())
        .build();
  }
  /**
   * Checks whether the province from the requested header is supported or not.
   *
   * <p>Will throw ProvinceNotAcceptableException if province is not supported.
   *
   * @param xProvinceCode @{@link Province}
   */
  public void checkSupportedProvince(Province xProvinceCode) {
    if (Province.QC != xProvinceCode) {
      throw new RequestNotAcceptableException(
          String.format("Province %s is not supported", xProvinceCode));
    }
  }
}
