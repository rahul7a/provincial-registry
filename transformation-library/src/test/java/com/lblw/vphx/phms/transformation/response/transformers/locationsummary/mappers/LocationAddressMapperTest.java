package com.lblw.vphx.phms.transformation.response.transformers.locationsummary.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LocationAddressMapperTest {
  private static LocationAddressMapper locationAddressMapper;

  @BeforeAll
  static void buildLocationAddressMapper() {
    locationAddressMapper = new LocationAddressMapper();
  }

  @Test
  void whenBinding_thenSetLocationAddress() {
    locationAddressMapper.bindStreetAddressLine1("streetAddressLine1");
    locationAddressMapper.bindStreetAddressLine2("streetAddressLine2");
    locationAddressMapper.bindPostalCode("postalCode");
    locationAddressMapper.bindCity("city");
    locationAddressMapper.bindCountry("country");

    Assertions.assertEquals("streetAddressLine1", locationAddressMapper.getStreetAddressLine1());
    Assertions.assertEquals("streetAddressLine2", locationAddressMapper.getStreetAddressLine2());
    Assertions.assertEquals("postalCode", locationAddressMapper.getPostalCode());
    Assertions.assertEquals("city", locationAddressMapper.getCity());
    Assertions.assertEquals("country", locationAddressMapper.getCountry());
  }
}
