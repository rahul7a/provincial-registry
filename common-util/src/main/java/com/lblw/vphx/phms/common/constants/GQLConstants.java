package com.lblw.vphx.phms.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GQLConstants {
  public static final String EXCEPTION_OCCURRED_WHILE_CALLING_GQL =
      "Exception occurred while calling GQL";
  public static final String GQL_DATA_FETCHING_ERROR =
      "Fetching %s data from GQL failed with cause %s";
  public static final String GRAPHQL_FILE_NOT_FOUND_MESSAGE = "File %s not found in graphql folder";
  public static final String GRAPHQL_URL_KEY = "graphql";
  public static final String GRAPHQL_FOLDER = "graphql/";
  public static final String GRAPHQL_FILE_EXTN = ".graphql";
  public static final String QUERY = "query";
  public static final String VARIABLES = "variables";
}
