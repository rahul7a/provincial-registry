package com.lblw.vphx.phms.common.logs;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "pageLists")
public class PageListLog {
  @Indexed(unique = true)
  private String provincialQueryId;

  // TODO: expiry period TBD further in future
  @Indexed(expireAfter = "365d")
  private Instant createdAt;

  @Indexed() private String requestId;
  private Integer currentRecordCount;
  private Integer totalRecordCount;
  private Integer remainingRecordCount;
  private Integer pageIndex;
  private Integer pageSize;
}
