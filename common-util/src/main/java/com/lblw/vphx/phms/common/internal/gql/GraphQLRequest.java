package com.lblw.vphx.phms.common.internal.gql;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;

@Data
@Builder
public class GraphQLRequest {
  private LinkedMultiValueMap<String, String> headers;
  @NonNull private Class<?> responseClass;

  @NonNull private String query;
  @NonNull private String variables;

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
   * This Method is used to add Authorization header in the request
   *
   * @param authToken {@link String}
   */
  public void setAuthToken(String authToken) {
    addHeader(HttpHeaders.AUTHORIZATION, authToken);
  }
}
