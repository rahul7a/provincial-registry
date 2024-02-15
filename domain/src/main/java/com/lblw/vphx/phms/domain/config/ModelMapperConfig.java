package com.lblw.vphx.phms.domain.config;

import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.patient.prescription.PrescriptionDispensableIndicator;
import com.lblw.vphx.phms.domain.patient.prescription.PrescriptionStatus;
import com.lblw.vphx.phms.domain.patient.prescription.PrescriptionType;
import java.util.List;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
public class ModelMapperConfig {

  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<CharSequence, PrescriptionType> getConverterForPrescriptionType() {
    return new Converter<CharSequence, PrescriptionType>() {
      @Override
      public PrescriptionType convert(
          MappingContext<CharSequence, PrescriptionType> mappingContext) {
        CharSequence source = mappingContext.getSource();
        return source == null ? null : PrescriptionType.valueOf(String.valueOf(source));
      }
    };
  }

  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<CharSequence, PrescriptionStatus> getConverterForPrescriptionStatus() {
    return new Converter<CharSequence, PrescriptionStatus>() {
      @Override
      public PrescriptionStatus convert(
          MappingContext<CharSequence, PrescriptionStatus> mappingContext) {
        CharSequence source = mappingContext.getSource();
        return source == null ? null : PrescriptionStatus.valueOf(String.valueOf(source));
      }
    };
  }

  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<CharSequence, PrescriptionDispensableIndicator>
      getConverterForPrescriptionDispensableIndicator() {
    return new Converter<CharSequence, PrescriptionDispensableIndicator>() {
      @Override
      public PrescriptionDispensableIndicator convert(
          MappingContext<CharSequence, PrescriptionDispensableIndicator> mappingContext) {
        CharSequence source = mappingContext.getSource();
        return source == null
            ? null
            : PrescriptionDispensableIndicator.valueOf(String.valueOf(source));
      }
    };
  }

  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<CharSequence, SystemIdentifier.IDENTIFIER_TYPE>
      getConverterForSystemIdentifierType() {
    return new Converter<CharSequence, SystemIdentifier.IDENTIFIER_TYPE>() {
      @Override
      public SystemIdentifier.IDENTIFIER_TYPE convert(
          MappingContext<CharSequence, SystemIdentifier.IDENTIFIER_TYPE> mappingContext) {
        CharSequence source = mappingContext.getSource();
        return source == null
            ? null
            : SystemIdentifier.IDENTIFIER_TYPE.valueOf(String.valueOf(source));
      }
    };
  }

  /**
   * maps from {@link CharSequence} object to {@link Gender}
   *
   * @return {@link Converter}
   */
  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<CharSequence, Gender> getConverterForGender() {
    return new Converter<CharSequence, Gender>() {
      @Override
      public Gender convert(MappingContext<CharSequence, Gender> mappingContext) {
        CharSequence source = mappingContext.getSource();
        return source == null ? null : Gender.valueOf(source.toString());
      }
    };
  }

  /**
   * This creates an instance of a {@link Converter} that would convert {@link
   * SystemIdentifier.IDENTIFIER_TYPE} to {@link * CharSequence}
   *
   * @return the converter described above.
   */
  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<SystemIdentifier.IDENTIFIER_TYPE, CharSequence>
      getIdentifierTypeCharSequenceConverter() {
    return new Converter<
        SystemIdentifier.IDENTIFIER_TYPE,
        CharSequence>() { // Converting to Lambda expression was causing the conversion to
      // fail.
      @Override
      public CharSequence convert(
          MappingContext<SystemIdentifier.IDENTIFIER_TYPE, CharSequence> mappingContext) {
        return mappingContext.getSource().name();
      }
    };
  }

  /**
   * This creates an instance of a {@link Converter} that would convert {@link Gender} to {@link
   * CharSequence}
   *
   * @return the converter described above.
   */
  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<Gender, CharSequence> getGenderCharSequenceConverter() {
    return new Converter<
        Gender,
        CharSequence>() { // Converting to Lambda expression was causing the conversion to fail.
      @Override
      public CharSequence convert(MappingContext<Gender, CharSequence> mappingContext) {
        final Gender source = mappingContext.getSource();
        return source == null ? null : source.name();
      }
    };
  }

  /**
   * This creates an instance of a {@link Converter} that would convert {@link PrescriptionType} to
   * {@link CharSequence}
   *
   * <p>Converting to Lambda expression was causing the conversion to fail.
   *
   * @return the converter described above.
   */
  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<PrescriptionType, CharSequence>
      getConverterForPrescriptionTypeFromCharSequence() {
    return new Converter<PrescriptionType, CharSequence>() {
      @Override
      public CharSequence convert(MappingContext<PrescriptionType, CharSequence> mappingContext) {
        PrescriptionType source = mappingContext.getSource();
        return source == null ? null : source.name();
      }
    };
  }

  /**
   * This creates an instance of a {@link Converter} that would convert {@link PrescriptionStatus}
   * to {@link CharSequence}
   *
   * <p>Converting to Lambda expression was causing the conversion to fail.
   *
   * @return the converter described above.
   */
  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<PrescriptionStatus, CharSequence>
      getConverterForPrescriptionStatusFromCharSequence() {
    return new Converter<PrescriptionStatus, CharSequence>() {
      @Override
      public CharSequence convert(MappingContext<PrescriptionStatus, CharSequence> mappingContext) {
        PrescriptionStatus source = mappingContext.getSource();
        return source == null ? null : source.name();
      }
    };
  }

  /**
   * This creates an instance of a {@link Converter} that would convert {@link
   * PrescriptionDispensableIndicator} to {@link CharSequence}
   *
   * <p>Converting to Lambda expression was causing the conversion to fail.
   *
   * @return the converter described above.
   */
  @Bean
  @SuppressWarnings("java:S1604")
  public Converter<PrescriptionDispensableIndicator, CharSequence>
      getConverterForPrescriptionDispensableIndicatorFromCharSequence() {
    return new Converter<PrescriptionDispensableIndicator, CharSequence>() {
      @Override
      public CharSequence convert(
          MappingContext<PrescriptionDispensableIndicator, CharSequence> mappingContext) {
        PrescriptionDispensableIndicator source = mappingContext.getSource();
        return source == null ? null : source.name();
      }
    };
  }

  /**
   * Create a custom Model Mapper with converter for PrescriptionType, PrescriptionStatus,
   * PrescriptionDispensableIndicator, Identifier, Gender and Address. The other fields are mapped
   * by model mapper (ref: http://modelmapper.org/getting-started/)
   *
   * @return {@link ModelMapper}
   */
  @Bean
  public ModelMapper modelMapper(List<Converter<?, ?>> converters) {
    var modelMapper = new ModelMapper();
    converters.forEach(modelMapper::addConverter);
    return modelMapper;
  }

  /**
   * This utility contains utility functions centered around CharSequence.The functions must be very
   * simple to be declared as static, otherwise must be instance methods to enable mocking.
   */
  public static class CharSequenceUtility {
    /** Private default constructor */
    private CharSequenceUtility() {}

    /**
     * Coverts a {@link CharSequence} to its equivalent {@link String} after doing null checks.
     *
     * @param charSequence the input to be converted to {@link String}
     * @return The equivalent {@link String} of the input {@link CharSequence}
     */
    public static @Nullable String getStringFromCharSequence(@Nullable CharSequence charSequence) {
      return charSequence == null ? null : charSequence.toString();
    }
  }
}
