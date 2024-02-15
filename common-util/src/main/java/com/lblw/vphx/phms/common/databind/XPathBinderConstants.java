package com.lblw.vphx.phms.common.databind;

/** Internal common constants for XPathBinder implementations */
class XPathBinderConstants {

  static final String MESSAGE_EXCEPTION_BINDING_INSTANCE = "Exception binding instance '%s'";
  static final String MESSAGE_EXCEPTION_BINDING_METHOD_OF_INSTANCE_WITH_ARGUMENTS =
      "Exception binding method '%s' of instance '%s' with arguments '%s'";
  static final String MESSAGE_UNEXPECTED_PARAMETER_TYPE_FOR_METHOD_OF_INSTANCE =
      "Unexpected parameter type '%s' on method '%s' of instance '%s'";
  static final String MESSAGE_UNEXPECTED_PARAMETER_TYPE_FOR_XPATH_ON_METHOD_OF_INSTANCE =
      "Unexpected parameter type '%s' for xPath '%s' on method '%s' of instance '%s'";
  private XPathBinderConstants() {}
}
