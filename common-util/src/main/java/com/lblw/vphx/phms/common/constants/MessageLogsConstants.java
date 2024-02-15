package com.lblw.vphx.phms.common.constants;

public class MessageLogsConstants {

  public static final String LOG_SOURCE_CONTEXT = "com.lblw.vphx.phms.domain.common.Service_SOURCE";
  public static final String LOG_TARGET_CONTEXT = "com.lblw.vphx.phms.domain.common.Service_TARGET";
  public static final String MESSAGE_PAYLOAD_TEMPLATE_REQUEST_CONTEXT =
      "com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplate_REQUEST";
  public static final String MESSAGE_PAYLOAD_TEMPLATE_RESPONSE_CONTEXT =
      "com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplate_RESPONSE";
  public static final String ORIGINAL_REQUEST_TIMESTAMP = "originalRequestTimestamp";
  public static final String LOG_ENCRYPTION_ENABLED_CONTEXT =
      "com.lblw.vphx.phms.domain.common.context.LOG_ENCRYPTION_ENABLED";
  public static final String LOG_COMPRESSION_ENABLED_CONTEXT =
      "com.lblw.vphx.phms.domain.common.context.LOG_COMPRESSION_ENABLED";
  private MessageLogsConstants() {}
}
