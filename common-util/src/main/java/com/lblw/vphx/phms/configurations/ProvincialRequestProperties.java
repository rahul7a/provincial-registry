package com.lblw.vphx.phms.configurations;

import com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplateType;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

/** Custom properties class to manage related to PHMS - Provincial request / response properties. */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "provinces.quebec")
public class ProvincialRequestProperties {

  private Request request;
  private Response response;

  @Getter
  @Setter
  public static class Request {
    private Webclient webclient;
    private Sender sender;
    private Security security;
    private Map<String, TransactionProperties> transaction;
  }

  @Getter
  @Setter
  public static class Response {
    private Map<String, TransactionProperties> transaction;
  }

  @Getter
  @Setter
  public static class Webclient {
    private int responseMemoryLimitInBytes;
    private int responseTimeOut;
    private int connectionTimeOut;
    private int readTimeOut;
    private int writeTimeOut;
  }

  @Getter
  @ConstructorBinding
  public static class Sender {
    private static final String DOT = ".";
    private final String extension;
    private final String name;
    private final String processingCode;
    private final String pharmacyGroup;
    private final String pharmacyLocationId;
    private final String applicationVersionNumber;
    private final String root;
    private final String controlActRoot;

    public Sender(
        String extension,
        String name,
        String processingCode,
        String pharmacyGroup,
        String pharmacyLocationId,
        String applicationVersionNumber) {
      this.extension = extension;
      this.name = name;
      this.processingCode = processingCode;
      this.pharmacyGroup = pharmacyGroup;
      this.pharmacyLocationId = pharmacyLocationId;
      this.applicationVersionNumber = applicationVersionNumber;
      this.root =
          buildSenderRootAndControlActRoot(
              pharmacyGroup, pharmacyLocationId, applicationVersionNumber);
      this.controlActRoot =
          buildSenderRootAndControlActRoot(
              pharmacyGroup, pharmacyLocationId, applicationVersionNumber);
    }

    private static String buildSenderRootAndControlActRoot(
        String pharmacyGroup, String pharmacyLocationId, String applicationVersionNumber) {
      return pharmacyGroup
          .concat(DOT)
          .concat(pharmacyLocationId)
          .concat(DOT)
          .concat(applicationVersionNumber.replace(DOT, Strings.EMPTY));
    }
  }

  @Getter
  @Setter
  public static class TransactionProperties {
    MessagePayloadTemplate messagePayloadTemplate;
    private String uri;
    private Header header;

    public static class MessagePayloadTemplate
        extends com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplate {

      @Override
      public void setUri(String uri) {
        super.setUri(uri);
        super.setMessagePayloadTemplateType(MessagePayloadTemplateType.HL7_V3);
      }
    }
  }

  @Getter
  @Setter
  public static class Header {
    private String soapAction;
    private String value;
  }

  // TODO: To be replaced in API_CONFIG for IAM
  @Getter
  @Setter
  public static class Security {
    private Certificate certificate;
  }

  // TODO: To be replaced in API_CONFIG for IAM
  @Getter
  @Setter
  public static class Certificate {
    private String alias;
    private String password;
    private String defaultLocation;
    private String location;
  }
}
