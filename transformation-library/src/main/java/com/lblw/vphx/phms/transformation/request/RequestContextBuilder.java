package com.lblw.vphx.phms.transformation.request;

import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import org.thymeleaf.context.Context;

/** The implementation of this interface will set data used in rendering Thymeleaf template. */
public interface RequestContextBuilder {

  /**
   * Enriches data and sets data in a {@link Context} which is otherwise difficult to set using
   * Thymeleaf template scripting.
   *
   * @param context{@link Context} context from which template gets data used in transformation
   * @param request{@link Request} request to transform
   */
  void setContext(Context context, Request<?> request);
}
