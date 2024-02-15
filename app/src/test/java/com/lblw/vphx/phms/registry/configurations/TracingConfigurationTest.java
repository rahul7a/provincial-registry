package com.lblw.vphx.phms.registry.configurations;

import brave.propagation.Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test cases to unit test {@link TracingConfiguration} TracingConfiguration */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TracingConfiguration.class)
class TracingConfigurationTest {

  @Autowired TracingConfiguration tracingConfiguration;

  @Test
  void WhenCurrentTraceContextMethodCalled_ThenCreateThreadLocalCurrentTraceContextBean() {
    ThreadLocalCurrentTraceContext threadLocalCurrentTraceContext =
        tracingConfiguration.currentTraceContext();
    Assertions.assertNotNull(threadLocalCurrentTraceContext);
  }

  @Test
  void WhenBaggagePropagationMethodCalled_ThenCreatePropagationFactoryObject() {
    Propagation.Factory propagationFactory = tracingConfiguration.baggagePropagation();
    Assertions.assertNotNull(propagationFactory);
  }
}
