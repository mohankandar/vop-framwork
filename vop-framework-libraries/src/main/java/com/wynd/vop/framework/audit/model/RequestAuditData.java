package com.wynd.vop.framework.audit.model;

import com.wynd.vop.framework.audit.AuditableData;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class RequestAuditData implements Serializable, AuditableData {

  private static final long serialVersionUID = -6463691536690649662L;


  /* The request. */
  private transient List<Object> request = Collections.emptyList();

  /**
   * Gets the request that is being logged in the audit logs.
   *
   * @return the request
   */
  public List<Object> getRequest() {
    return request;
  }

  /**
   * Set the request object to be logged in the audit logs.
   *
   * @param request
   */
  public void setRequest(final List<Object> request) {
    this.request = request;
  }

  /**
   * Manually formatted JSON-like string of key/value pairs.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "RequestAuditData{request=" + (request == null ? "" : ReflectionToStringBuilder.toString(request)) + '}';
  }
}
