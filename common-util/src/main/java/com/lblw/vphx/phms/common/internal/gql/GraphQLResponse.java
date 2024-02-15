package com.lblw.vphx.phms.common.internal.gql;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class GraphQLResponse {
  private JsonNode data;
  private JsonNode errors;
}
