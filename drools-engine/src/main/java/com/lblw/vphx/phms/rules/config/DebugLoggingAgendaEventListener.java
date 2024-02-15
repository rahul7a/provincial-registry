package com.lblw.vphx.phms.rules.config;

import lombok.extern.slf4j.Slf4j;
import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;

/** EventListener Enables debug logging for Drools' rules when enabled */
@Slf4j
public class DebugLoggingAgendaEventListener extends DefaultAgendaEventListener {
  @Override
  public void afterMatchFired(AfterMatchFiredEvent event) {
    super.afterMatchFired(event);
    var ruleMatch = event.getMatch();
    log.debug(
        "Rule Match: \nRule: [{}]\nFacts: {}",
        ruleMatch.getRule().getName(),
        ruleMatch.getObjects());
  }

  @Override
  public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
    super.agendaGroupPopped(event);
    log.debug("Agenda Group Exit: {}", event.getAgendaGroup());
  }

  @Override
  public void agendaGroupPushed(AgendaGroupPushedEvent event) {
    super.agendaGroupPushed(event);
    log.debug("Agenda Group Enter: {}", event.getAgendaGroup());
  }
}
