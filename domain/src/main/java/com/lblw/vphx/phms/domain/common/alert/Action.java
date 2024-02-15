package com.lblw.vphx.phms.domain.common.alert;

import com.lblw.vphx.phms.domain.common.response.Issue;

/** Action type for issue request {@link Issue} */
public enum Action {
  SNOW_TICKET,
  SYSTEM_OUTAGE,
  WEBHOOK
}
