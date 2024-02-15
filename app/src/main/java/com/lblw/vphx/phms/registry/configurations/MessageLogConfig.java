package com.lblw.vphx.phms.registry.configurations;

import com.lblw.vphx.phms.domain.common.MessageProcess;
import java.util.Set;

public class MessageLogConfig {

  public static final Set<MessageProcess> ENCRYPTION_ENABLED_MESSAGE_PROCESS =
      Set.of(MessageProcess.PATIENT_SEARCH);
  public static final Set<MessageProcess> COMPRESSION_ENABLED_MESSAGE_PROCESS =
      Set.of(
          MessageProcess.PATIENT_SEARCH,
          MessageProcess.PROVIDER_SEARCH,
          MessageProcess.LOCATION_SEARCH,
          MessageProcess.LOCATION_DETAILS,
          MessageProcess.PATIENT_CONSENT);
  private MessageLogConfig() {}
}
