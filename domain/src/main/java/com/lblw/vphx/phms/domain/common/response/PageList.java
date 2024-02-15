package com.lblw.vphx.phms.domain.common.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PageList<T> extends ProvincialResponse {
  List<T> data;
  String queryId;
  Integer currentRecordCount;
  Integer totalRecordCount;
  Integer remainingRecordCount;
  Integer originalTotalRecordCount;
  Integer pageIndex;
  Integer pageSize;
}
