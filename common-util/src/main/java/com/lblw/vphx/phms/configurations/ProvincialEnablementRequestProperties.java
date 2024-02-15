package com.lblw.vphx.phms.configurations;

import com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplateType;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "provincial-enablement")
public class ProvincialEnablementRequestProperties {

  private Request request;
  private Response response;

  @Getter
  @Setter
  public static class Request {
    private Map<String, TransactionProperties> transaction;
  }

  @Getter
  @Setter
  public static class Response {
    private Map<String, TransactionProperties> transaction;
  }

  @Getter
  @Setter
  public static class TransactionProperties {
    private MessagePayloadTemplate messagePayloadTemplate;
    private Messaging messaging;
    private String entityType;
    private String hwEventName;

    public static class MessagePayloadTemplate
        extends com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplate {

      @Override
      public void setUri(String uri) {
        super.setUri(uri);
        super.setMessagePayloadTemplateType(MessagePayloadTemplateType.PE);
      }
    }

    @Getter
    @Setter
    public static class Messaging {
      private String exchange;
      private String routingKey;
      private String queue;
    }
  }
}
