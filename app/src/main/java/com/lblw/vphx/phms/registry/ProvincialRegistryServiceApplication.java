package com.lblw.vphx.phms.registry;

import com.sdm.ehealth.referencedata.client.cache.EnableRdsCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ProvincialRegistryService SpringBootApplication */
@SpringBootApplication(
    scanBasePackages = {
      "com.lblw.vphx.phms.*",
      "com.lblw.vphx.iams.*",
      "com.sdm.ehealth.referencedata.*"
    },
    exclude = {EnableRdsCaching.class})
public class ProvincialRegistryServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProvincialRegistryServiceApplication.class, args);
  }
}
