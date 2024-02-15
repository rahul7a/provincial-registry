package com.lblw.vphx.phms.common.internal;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;

/**
 * This class is used to build request, which is being used to call internal api
 *
 * @mandatory - baseUrl, uri, and responseClass are mandatory params to make a call to internal api.
 */
@Builder
@Data
public class InternalApiRequest {
  @NonNull private String baseUrl;
  @NonNull private String uri;
  private List<String> params;
  private LinkedMultiValueMap<String, String> headers;
  @NonNull private Class<?> responseClass;

  /**
   * This Method is used to add header in request
   *
   * @param headerName {@link String}
   * @param headerValue {@link String}
   */
  public void addHeader(String headerName, String headerValue) {
    if (headers == null) {
      headers = new LinkedMultiValueMap<>();
    }
    headers.set(headerName, headerValue);
  }

  /**
   * This Method is used to add path param in request
   *
   * @param paramValue {@link String}
   */
  public void addParam(String paramValue) {
    if (params == null) {
      params = new ArrayList<>();
    }
    params.add(paramValue);
  }

  /**
   * This Method is used to add Authorization header in the request
   *
   * @param authToken {@link String}
   */
  public void authorize(String authToken) {
    addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
  }
}
