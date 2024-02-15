package com.lblw.vphx.phms.rules.config;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/** Custom configuration for Drools rules Engine */
@Configuration
@Slf4j
public class RulesEngineConfig {
  private static final String RULES_PATH = "rules/";

  /**
   * kieFileSystem is an in-memory file system provided by the framework
   *
   * @return kieFileSystem {@link KieFileSystem}
   * @throws IOException
   */
  @Bean
  public KieFileSystem kieFileSystem() throws IOException {
    KieFileSystem kieFileSystem = kieServices().newKieFileSystem();
    for (Resource file : getRuleFiles()) {
      kieFileSystem.write(ResourceFactory.newUrlResource(String.valueOf(file.getURL())));
    }
    return kieFileSystem;
  }

  /**
   * Strategy interface for resolving a location pattern into Resource objects.
   *
   * @return {@link ResourcePatternResolver}
   * @throws IOException
   */
  private Resource[] getRuleFiles() throws IOException {
    ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    return resourcePatternResolver.getResources("classpath*:" + RULES_PATH + "**/*.*");
  }

  /**
   * Creates a container for all the KieBases of a given KieModule
   *
   * @param kieFileSystem {@link KieFileSystem}
   * @return {@link KieContainer}
   */
  @Bean
  @ConditionalOnMissingBean(KieContainer.class)
  public KieContainer kieContainer(KieFileSystem kieFileSystem) {
    final KieRepository kieRepository = kieRepository();
    KieBuilder kieBuilder = kieServices().newKieBuilder(kieFileSystem);
    kieBuilder.buildAll();
    return kieServices().newKieContainer(kieRepository.getDefaultReleaseId());
  }

  /**
   * Provides a way to notify the user when a rule event is triggered
   *
   * @return event listener
   */
  @Bean
  public AgendaEventListener agendaEventListener() {
    if (log.isDebugEnabled()) {
      return new DebugLoggingAgendaEventListener();
    }

    return new DefaultAgendaEventListener();
  }

  /**
   * KieRepository is a singleton acting as a repository for all the available KieModules
   *
   * @return kieRepository {@link KieRepository}
   */
  private KieRepository kieRepository() {
    final KieRepository kieRepository = kieServices().getRepository();
    kieRepository.addKieModule(kieRepository::getDefaultReleaseId);
    return kieRepository;
  }

  /**
   * The KieServices is a thread-safe singleton acting as a hub giving access to the other Services
   * provided by Kie
   *
   * @return {@link KieServices}
   */
  private KieServices kieServices() {
    return KieServices.Factory.get();
  }
}
