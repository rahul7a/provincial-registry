package com.lblw.vphx.phms.transformation.request;

import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import org.thymeleaf.context.Context;

/**
 * Sets variable in {@link Context} to use in Find Patient Query transaction for the Quebec
 * province.
 */
public class ProvincialPatientSearchContextBuilder implements RequestContextBuilder {

  /**
   * sets contexts for following criteria: <br>
   * 1. dateOfBirth formatted to QC date pattern<br>
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param request {@link Request}
   */
  @Override
  public void setContext(Context context, Request<?> request) {
    var patientSearchCriteria =
        ((ProvincialPatientSearchRequest) request).getProvincialRequestPayload();
    var formattedDateOfBirth =
        DateUtils.formatToProvincialDateFormat(
            request.getProvincialRequestPayload().getProvincialRequestControl().getProvince(),
            patientSearchCriteria.getDateOfBirth());
    context.setVariable("formattedDateOfBirth", formattedDateOfBirth);
  }
}
