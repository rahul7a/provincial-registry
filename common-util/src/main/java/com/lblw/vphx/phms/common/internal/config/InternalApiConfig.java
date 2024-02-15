package com.lblw.vphx.phms.common.internal.config;

import com.lblw.vphx.phms.domain.common.MessageProcess;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services.internal")
@Data
public class InternalApiConfig {

  private int responseTimeout = 1000;

  private int connectTimeout = 1000;

  private int readTimeout = 1000;

  private int writeTimeout = 1000;

  private GraphQL graphql;

  private IAM iam;

  private RDS rds;

  private DPS dps;

  private ObjectStorage objectStorage;

  @Getter
  @Setter
  public static class DPS {
    private String baseUrl;
    private Paths paths;

    private Set<MessageProcess> enableCompression;
    private Set<MessageProcess> enableEncryption;

    @Getter
    @Setter
    public static class Paths {
      private String deidentifyBlobPath;
      private String reidentifyBlobPath;
    }
  }

  /** Properties corresponding to internal VPHXJwtService */
  @Getter
  @Setter
  public static class IAM {
    private String clientId;
    private String clientSecret;
    private String environment;
  }

  /** Properties corresponding to internal Reference Data Service */
  @Getter
  @Setter
  public static class RDS {
    private String baseUrl;
    private String actuator;
    private String vocabPath;
    private Schedule schedule;

    @Getter
    @Setter
    public static class Schedule {
      private String cron;
    }
  }

  @Getter
  @Setter
  public static class ObjectStorage {
    private String baseUrl;
    private String objectStorePath;
  }

  /** Properties corresponding to internal GraphQL Service */
  @Getter
  @Setter
  public static class GraphQL {
    private String url;
  }
}
