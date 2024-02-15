package com.lblw.vphx.phms.common.constants;

/** Error Constants */
public class ErrorConstants {
  public static final String EXCEPTION_OCCURRED_WHILE_CALLING_PROVINCE =
      "Exception occurred while calling Province";
  public static final String EXCEPTION_WHILE_FETCHING_CERTIFICATE_FROM_OBJECT_STORE =
      "Exception while fetching certificate from object store for certificate id: %s";
  public static final String EXCEPTION_WHILE_FETCHING_CERTIFICATE =
      "Exception while fetching certificate";
  public static final String EXCEPTION_IN_PARSING_JWT_TOKEN =
      "Exception occurred while parsing JWT.";
  public static final String EXCEPTION_IN_PARSING_NULL_OR_BLANK_JWT_CLAIM =
      "Cannot parse null/empty claims.";
  public static final String EXCEPTION_WHILE_FETCHING_PHARMACY_DETAILS =
      "Exception while fetching pharmacy details from pharmacy store for pharmacy id: %s";
  public static final String MESSAGE_LOGGING_SERVICE_EXCEPTION_FOR_BUILT_MESSAGE =
      "Error Logging message: {0}";
  public static final String MESSAGE_LOGGING_SERVICE_EXCEPTION = "Error Logging message";
  public static final String INTERNAL_API_ERROR_MESSAGE =
      "Call to internal service failed with http status code %s %s";
  public static final String EXCEPTION_WHILE_FETCHING_VOCAB_BUILDER_VALUE =
      "Exception while fetching vocab value from %s vocabulary and province: %s";
  public static final String WITH_STATUS_CODE = "with status code: ";
  public static final String COULD_NOT_PERFORM_OUTBOX_SUBSCRIPTION_DUE_TO_REASON =
      "Could not perform outbox subscription due to reason: {}";
  public static final String CAN_NOT_CONVERT_EVENT_OUTBOX_MESSAGE_TO_BYTE =
      "Cannot convert event outbox message to byte[]";
  public static final String EXCEPTION_MESSAGE_TRANSFORM_PHMS_REQUEST =
      "Exception Occurred while converting PHMS Request Object to signed HL7 V3 Xml String: %s";
  public static final String EXCEPTION_MESSAGE_TRANSFORM_XML_RESPONSE =
      "Exception Occurred exception while doing transformation of XML response: %s";
  public static final String HL7V3_XML_REQUEST_IS_BLANK = "HL7V3 XML request is blank for [%s]";
  public static final String EXCEPTION_OCCURRED_WHILE_SIGNING_REQUEST =
      "Exception occurred while signing request";
  public static final String EXCEPTION_OCCURRED_WHILE_FETCHING_CERTIFICATE =
      "Exception occurred while fetching certificate using ObjectStorageService";

  ErrorConstants() {}
}
