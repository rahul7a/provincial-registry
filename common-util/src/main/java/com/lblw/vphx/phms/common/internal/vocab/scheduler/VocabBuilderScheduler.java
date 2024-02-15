package com.lblw.vphx.phms.common.internal.vocab.scheduler;

import com.lblw.vphx.phms.common.internal.vocab.runner.VocabBuilderRunner;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
@EnableScheduling
/** Scheduler for Scheduling vocabularies refresh at predefined interval. */
public class VocabBuilderScheduler {

  private final VocabBuilderRunner vocabBuilderRunner;

  /** Schedules refreshing of vocabulary values. */
  @Scheduled(cron = "${services.internal.rds.schedule.cron}")
  public void refreshVocabulary() {

    log.info("Starting Refreshing Vocabularies");

    vocabBuilderRunner.run();

    log.info("Refreshing Vocabularies Completed");
  }
}
