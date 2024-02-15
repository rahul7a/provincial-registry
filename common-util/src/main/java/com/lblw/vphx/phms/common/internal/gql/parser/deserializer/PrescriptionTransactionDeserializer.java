package com.lblw.vphx.phms.common.internal.gql.parser.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.common.DateFormat;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.SigDescription;
import com.lblw.vphx.phms.domain.medication.CompoundType;
import com.lblw.vphx.phms.domain.medication.Medication;
import com.lblw.vphx.phms.domain.medication.MedicationIngredient;
import com.lblw.vphx.phms.domain.medication.MedicationType;
import com.lblw.vphx.phms.domain.medication.PackSize;
import com.lblw.vphx.phms.domain.medication.UOM;
import com.lblw.vphx.phms.domain.patient.Patient;
import com.lblw.vphx.phms.domain.patient.PatientType;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.prescriber.Prescriber;
import com.lblw.vphx.phms.domain.prescription.AuditDetails;
import com.lblw.vphx.phms.domain.prescription.AuditUserDetails;
import com.lblw.vphx.phms.domain.prescription.Description;
import com.lblw.vphx.phms.domain.prescription.DiagCodeData;
import com.lblw.vphx.phms.domain.prescription.Indication;
import com.lblw.vphx.phms.domain.prescription.Prescription;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import com.lblw.vphx.phms.domain.prescription.Product;
import com.lblw.vphx.phms.domain.prescription.VirtualBasket;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

/**
 * PrescriptionTransactionDeserializer {@link PrescriptionTransactionDeserializer } deserialize and
 * build prescriptionTransaction {@link PrescriptionTransaction} DTOs.
 */
public final class PrescriptionTransactionDeserializer
    extends StdDeserializer<PrescriptionTransaction> {

  private final ObjectMapper defaultMapper;

  /**
   * Constructor
   *
   * @param defaultMapper {@link ObjectMapper}
   */
  public PrescriptionTransactionDeserializer(ObjectMapper defaultMapper) {
    super(PrescriptionTransaction.class);
    this.defaultMapper = defaultMapper;
  }

  /**
   * This method is used to build compound Name and Type
   *
   * @param medication {@link Medication}
   * @param compoundDrug {@link JsonNode}
   */
  private void buildCompoundNameAndType(Medication medication, JsonNode compoundDrug) {
    if (compoundDrug != null && !compoundDrug.isNull()) {
      JsonNode compoundType = compoundDrug.get(CommonConstants.COMPOUND_TYPE);
      ArrayNode compoundName = (ArrayNode) compoundDrug.get(CommonConstants.NAME);
      JsonNode compoundPricing = compoundDrug.get(CommonConstants.COMPOUND_PRICING);
      if (compoundType != null && !compoundType.isNull()) {
        medication.setCompoundType(
            CompoundType.builder()
                .code(
                    compoundType
                        .get(CommonConstants.PRIVATE_INSURANCE)
                        .get(CommonConstants.CODE)
                        .asText())
                .build());
      }
      StreamSupport.stream(compoundName.spliterator(), false)
          .forEach(
              node -> {
                if (node.get(CommonConstants.LANGUAGE).asText().equals(CommonConstants.ENGLISH)) {
                  medication.setTradeNameEnglish(node.get(CommonConstants.VALUE).asText());
                } else if (node.get(CommonConstants.LANGUAGE)
                    .asText()
                    .equals(CommonConstants.FRENCH)) {
                  medication.setTradeNameFrench(node.get(CommonConstants.VALUE).asText());
                }
              });

      if (compoundPricing != null && !compoundPricing.isNull()) {
        JsonNode totalDispensedQuantity =
            compoundPricing.get(CommonConstants.TOTAL_DISPENSED_QUANTITY);
        medication.setUom(
            UOM.builder().code(totalDispensedQuantity.get(CommonConstants.CODE).asText()).build());
      }
    }
  }

  /**
   * This method is used to build Drug Attributes
   *
   * @param medication {@link Medication}
   * @param drugNode {@link JsonNode}
   */
  private void buildDrugAttributes(Medication medication, JsonNode drugNode) {
    if (drugNode != null && !drugNode.isNull()) {
      ArrayNode drugName = (ArrayNode) drugNode.get(CommonConstants.TRADE_NAME);
      JsonNode amount = drugNode.get(CommonConstants.AMOUNT);
      if (drugName != null && !drugName.isNull()) {
        StreamSupport.stream(drugName.spliterator(), false)
            .forEach(
                node -> {
                  if (node.get(CommonConstants.LANGUAGE).asText().equals(CommonConstants.ENGLISH)) {
                    medication.setTradeNameEnglish(node.get(CommonConstants.VALUE).asText());
                  } else if (node.get(CommonConstants.LANGUAGE)
                      .asText()
                      .equals(CommonConstants.FRENCH)) {
                    medication.setTradeNameFrench(node.get(CommonConstants.VALUE).asText());
                  }
                });
      }
      if (amount != null && !amount.isNull()) {
        JsonNode denominator = amount.get(CommonConstants.DENOMINATOR);
        medication.setPackSize(
            PackSize.builder()
                .value(denominator.get(CommonConstants.VALUE).asDouble())
                .uom(UOM.builder().code(denominator.get(CommonConstants.CODE).asText()).build())
                .build());
      }
    }
  }

  /**
   * This method is used to build Medication
   *
   * @param node {@link JsonNode}
   * @return Medication
   */
  private Medication buildMedication(JsonNode node) {

    JsonNode medicationNode = node.get(CommonConstants.MEDICATION);
    Medication medication = defaultMapper.convertValue(medicationNode, Medication.class);

    if (medication == null || medicationNode.isNull()) {
      return medication;
    }
    JsonNode drug = node.get(CommonConstants.DRUG);
    JsonNode compoundDrug = node.get(CommonConstants.COMPOUND_DRUG);
    String medicationType = medicationNode.get(CommonConstants.PRODUCT_TYPE).asText();
    JsonNode drugIngredientsNode =
        compoundDrug != null ? compoundDrug.get(CommonConstants.DRUG_INGREDIENTS) : null;
    switch (medicationType) {
      case CommonConstants.DRG:
        populateDrugMedicationDetails(medication, drug);
        buildDrugAttributes(medication, drug);
        break;
      case CommonConstants.COMPOUND:
        medication.setType(MedicationType.COMPOUND);
        medication.setRouteOfAdministration(
            (compoundDrug != null
                    && !compoundDrug.isNull()
                    && compoundDrug.hasNonNull(CommonConstants.ROUTE_OF_ADMINISTRATION))
                ? compoundDrug
                    .get(CommonConstants.ROUTE_OF_ADMINISTRATION)
                    .get(CommonConstants.CODE)
                    .asText()
                : null);
        medication.setUpc(
            (compoundDrug != null
                    && !compoundDrug.isNull()
                    && compoundDrug.hasNonNull(CommonConstants.UPC))
                ? compoundDrug.get(CommonConstants.UPC).asText()
                : null);
        buildCompoundNameAndType(medication, compoundDrug);
        break;
      case CommonConstants.DEV:
        medication.setType(MedicationType.DEVICE);
        break;
      default:
    }

    populateMedicationIngredients(medication, drugIngredientsNode);
    return medication;
  }

  /**
   * This method is used to populate medication ingredients
   *
   * @param medication {@link Medication}
   * @param drugIngredientsNode {@link JsonNode}
   */
  private void populateMedicationIngredients(Medication medication, JsonNode drugIngredientsNode) {
    // if the medicationIngredients array contains at least one element
    if (drugIngredientsNode != null
        && drugIngredientsNode.isArray()
        && !drugIngredientsNode.isEmpty()) {
      List<MedicationIngredient> medicationIngredientsList = medication.getIngredients();
      if (medicationIngredientsList == null) {
        medicationIngredientsList = new ArrayList<>();
      }
      // loop through the medicationIngredients and add to medication's ingredients list
      for (JsonNode ingredientNode : drugIngredientsNode) {
        medicationIngredientsList.add(
            MedicationIngredient.builder()
                .medication(buildDrugIngredient(ingredientNode))
                .quantity(ingredientNode.get(CommonConstants.QUANTITY).asDouble())
                .build());
      }
      medication.setIngredients(medicationIngredientsList);
    }
  }

  /**
   * This method is used to populate drug route of administration and upc
   *
   * @param medication {@link Medication}
   * @param drug {@link JsonNode}
   */
  private void populateDrugMedicationDetails(Medication medication, JsonNode drug) {
    medication.setType(MedicationType.DRUG);
    if (drug != null && !drug.isNull()) {
      medication.setRouteOfAdministration(
          (drug.hasNonNull(CommonConstants.ROUTE_OF_ADMINISTRATION))
              ? drug.get(CommonConstants.ROUTE_OF_ADMINISTRATION).get(CommonConstants.CODE).asText()
              : null);
      medication.setUpc(
          drug.hasNonNull(CommonConstants.UPC) ? drug.get(CommonConstants.UPC).asText() : null);
    }
  }

  /**
   * This method is used to build drug ingredient that forms the medicationIngredients array
   *
   * @param node {@link JsonNode}
   * @return Medication, null if node is null
   */
  private Medication buildDrugIngredient(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    Medication medication =
        Medication.builder()
            .din(node.get(CommonConstants.DIN).asText())
            .upc(node.get(CommonConstants.UPC).asText())
            .ingredientId(node.get(CommonConstants.INGREDIENT_ID).asText())
            .type(MedicationType.DRUG)
            .build();

    JsonNode tradeNameNode = node.get(CommonConstants.TRADE_NAME);
    if (tradeNameNode != null && tradeNameNode.isArray() && !tradeNameNode.isEmpty()) {
      for (JsonNode nameNode : tradeNameNode) {
        if (nameNode.get(CommonConstants.LANGUAGE).asText().equals(CommonConstants.ENGLISH)) {
          medication.setTradeNameEnglish(nameNode.get(CommonConstants.VALUE).asText());
        }
        if (nameNode.get(CommonConstants.LANGUAGE).asText().equals(CommonConstants.FRENCH)) {
          medication.setTradeNameFrench(nameNode.get(CommonConstants.VALUE).asText());
        }
      }
    }
    if (node.hasNonNull(CommonConstants.PACK_SIZE)) {
      JsonNode packSizeNode = node.get(CommonConstants.PACK_SIZE);
      medication.setPackSize(
          PackSize.builder()
              .value(
                  packSizeNode
                      .get(CommonConstants.DENOMINATOR)
                      .get(CommonConstants.VALUE)
                      .asDouble())
              .uom(
                  UOM.builder()
                      .code(
                          packSizeNode
                              .get(CommonConstants.DENOMINATOR)
                              .get(CommonConstants.CODE)
                              .asText())
                      .build())
              .build());
    }
    return medication;
  }

  /**
   * This method is used to build Prescription
   *
   * @param prescriptionTransactionNode {@link JsonNode} prescriptionTransaction Node
   * @param prescriptionNode {@link JsonNode}
   * @return Prescription
   */
  private Prescription buildPrescription(
      JsonNode prescriptionTransactionNode, JsonNode prescriptionNode) throws IOException {
    Prescription prescription =
        defaultMapper
            .reader()
            .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .forType(Prescription.class)
            .readValue(prescriptionNode);

    if (prescriptionTransactionNode.hasNonNull(CommonConstants.PATIENT)) {
      JsonNode patientNode = prescriptionTransactionNode.get(CommonConstants.PATIENT);
      Patient patient = buildPatient(patientNode);
      if (Objects.nonNull(patient) && !hasPatientEmpty(patient)) {
        prescription.setPatient(patient);
      }
      if (prescriptionNode.hasNonNull(CommonConstants.SIG)
          && prescriptionNode.hasNonNull(CommonConstants.SIG_DATA)) {
        JsonNode sigNode = prescriptionNode.get(CommonConstants.SIG);

        List<SigDescription> sigDescriptions = buildPrescriptionSigCode(prescriptionNode);

        ArrayNode sigCodeNode = (ArrayNode) sigNode.get(CommonConstants.CODE);
        List<String> codes = new ArrayList<>(sigCodeNode.size());
        sigCodeNode.forEach(jsonNode -> codes.add(jsonNode.asText()));

        if (Objects.nonNull(prescription.getSig()) && !sigDescriptions.isEmpty()) {
          prescription.getSig().setDescriptions(sigDescriptions);
          prescription.getSig().setCode(codes);
          prescription
              .getSig()
              .setLanguage(
                  (sigNode
                          .get(CommonConstants.SIG_LANGUAGE)
                          .asText()
                          .equalsIgnoreCase(CommonConstants.ENGLISH)
                      ? LanguageCode.ENG
                      : LanguageCode.FRA));
          prescription.getSig().setDescriptionEn(sigNode.get(CommonConstants.DESCRIPTION_ENG).asText());
          prescription.getSig().setDescriptionFr(sigNode.get(CommonConstants.DESCRIPTION_FR).asText());
        }
      }
    }
    buildPrescriber(prescription, prescriptionNode);
    return prescription;
  }

  /**
   * This method is used to build Prescriber
   *
   * @param prescriptionNode {@link JsonNode}
   * @return Prescriber
   */
  private Prescriber buildPrescriber(Prescription prescription, JsonNode prescriptionNode) {
    if (!prescriptionNode.hasNonNull(CommonConstants.PRESCRIBER_META_DATA)
            && Objects.nonNull(prescription.getPrescriber())) {
      return  null;
    }
    JsonNode prescriberNode = prescriptionNode.get(CommonConstants.PRESCRIBER_META_DATA);
    Prescriber prescriber = prescription.getPrescriber();
    prescriber.setPrescriberProvincialLocationIdentifier(prescriberNode.get(CommonConstants.PRESCRIBER_PROVINCIAL_LOCATION_IDENTIFIER) == null
            ? Strings.EMPTY : prescriberNode.get(CommonConstants.PRESCRIBER_PROVINCIAL_LOCATION_IDENTIFIER).asText());
    prescriber.setOutOfProvince(prescriberNode.get(CommonConstants.OUT_OF_PROVINCE) == null
            ? null : prescriberNode.get(CommonConstants.OUT_OF_PROVINCE).asBoolean());
    prescriber.setPrescriberProvincialName(prescriberNode.get(CommonConstants.PRESCRIBER_PROVINCIAL_NAME) == null
            ? Strings.EMPTY :  prescriberNode.get(CommonConstants.PRESCRIBER_PROVINCIAL_NAME).asText());
    return prescriber;
  }
  /**
   * This method is used to build prescription sig code {@link SigDescription}
   *
   * @param prescriptionNode {@link JsonNode}
   * @return {@link List<SigDescription> }
   */
  private List<SigDescription> buildPrescriptionSigCode(JsonNode prescriptionNode) {
    JsonNode sigNode = prescriptionNode.get(CommonConstants.SIG);
    ArrayNode sigCodeNode = (ArrayNode) sigNode.get(CommonConstants.CODE);
    JsonNode sigLanguageNode = sigNode.get(CommonConstants.SIG_LANGUAGE);
    ArrayNode sigDataNode = (ArrayNode) prescriptionNode.get(CommonConstants.SIG_DATA);
    if (sigNode.isEmpty()) {
      return Collections.emptyList();
    }
    String sigLanguage = defaultMapper.convertValue(sigLanguageNode, String.class);
    return StreamSupport.stream(sigCodeNode.spliterator(), false)
        .map(
            sigCodeNodeElement -> {
              String code = defaultMapper.convertValue(sigCodeNodeElement, String.class);
              return SigDescription.builder()
                  .name(
                      StreamSupport.stream(sigDataNode.spliterator(), false)
                          .filter(
                              sigDataNodeElement ->
                                  sigDataNodeElement
                                      .get(CommonConstants.CODE)
                                      .get(sigLanguage)
                                      .asText()
                                      .equalsIgnoreCase(code))
                          .map(
                              sigDataNodeElement -> {
                                var englishName =
                                    sigDataNodeElement
                                        .get(CommonConstants.NAME)
                                        .get(CommonConstants.ENGLISH)
                                        .asText();
                                var frenchName =
                                    sigDataNodeElement
                                        .get(CommonConstants.NAME)
                                        .get(CommonConstants.FRENCH)
                                        .asText();
                                return Map.of(
                                    LanguageCode.ENG, englishName,
                                    LanguageCode.FRA, frenchName);
                              })
                          .findFirst()
                          .orElse(Map.of(LanguageCode.ENG, code, LanguageCode.FRA, code)))
                  .build();
            })
        .collect(Collectors.toList());
  }
  /**
   * This method is used to build Patient
   *
   * @param patientNode {@link JsonNode}
   * @return Patient
   */
  private Patient buildPatient(JsonNode patientNode) {
    Patient patient =
        (patientNode != null && !patientNode.isNull())
            ? defaultMapper.convertValue(patientNode, Patient.class)
            : null;
    if (Objects.nonNull(patient)) {
      var genderNode = patientNode.get(CommonConstants.GENDER_ORDINAL_VALUE);
      patient.setGender(genderNode != null ? Gender.toGender(genderNode.asInt()) : null);
      var patientTypeNode = patientNode.get(CommonConstants.PATIENT_TYPE);
      patient.setType(
          patientTypeNode != null ? PatientType.toPatientType(patientTypeNode.asInt()) : null);
    }
    return patient;
  }

  /**
   * This method check for empty
   *
   * @param patient {@link Patient}
   * @return boolean
   */
  private boolean hasPatientEmpty(Patient patient) {
    return Patient.builder().build().equals(patient);
  }

  /**
   * This method is used to build Pharmacy
   * @param prescriptionNode
   * @return Pharmacy
   */
  private Pharmacy buildPharmacy(JsonNode prescriptionNode) {
    String pharmacyId = prescriptionNode.get(CommonConstants.PHARMACY_ID).asText();
    /* TODO- Need to build other Pharmacy related fields in future*/
    return Pharmacy.builder().id(pharmacyId).build();
  }

  @Override
  public PrescriptionTransaction deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    JsonNode prescriptionNode = node.get(CommonConstants.PRESCRIPTION);
    String systemIdentifier = node.get(CommonConstants.SYSTEM_IDENTIFIER).asText();
    String fillStatus =
        node.get(CommonConstants.STATUS) == null
            ? Strings.EMPTY
            : node.get(CommonConstants.STATUS).asText();
    String dispenseType =
        prescriptionNode.get(CommonConstants.WORKFLOW_TYPE) == null
            ? Strings.EMPTY
            : prescriptionNode.get(CommonConstants.WORKFLOW_TYPE).asText();
    String workflowType =
        node.get(CommonConstants.WORKFLOW_TYPE) == null
            ? null
            : node.get(CommonConstants.WORKFLOW_TYPE).asText();
    String substitutionReason =
            node.get(CommonConstants.SUBSTITUTION_REASON) == null
                    ? Strings.EMPTY
                    : node.get(CommonConstants.SUBSTITUTION_REASON).asText();
    String cancelReason =
        node.get(CommonConstants.CANCEL_REASON) == null
            ? null
            : node.get(CommonConstants.CANCEL_REASON).asText();
    int supplyDays = node.get(CommonConstants.SUPPLY_DAYS).asInt();
    LocalDate serviceDate =
        DateUtils.parseSystemDateFormatToLocalDate(node.get(CommonConstants.SERVICE_DATE).asText());
    String createdDate =
        DateUtils.toDateFormatYrsToSecInUTCWithDate(
            node.get(CommonConstants.CREATED_DATE).asText(),
            DateTimeFormatter.ofPattern(
                DateFormat.PROVINCIAL_ENABLEMENT_DATE_TIME_FORMAT_WITH_OFFSET));
    return PrescriptionTransaction.builder()
        .prescription(buildPrescription(node, prescriptionNode))
        .pharmacy(buildPharmacy(prescriptionNode))
        .medication(buildMedication(node))
        .systemIdentifier(systemIdentifier)
        .fillStatus(fillStatus)
        .cancelReason(cancelReason)
        .dispenseType(dispenseType)
        .workflowType(workflowType)
        .substitutionReason(substitutionReason)
        .dispenseSource(prescriptionNode.get(CommonConstants.DISPENSE_SOURCE).asText())
        .supplyDays(supplyDays)
        .intervalDays(node.get(CommonConstants.INTERVAL_DAYS).asInt())
        .serviceDate(serviceDate)
        .createdDate(createdDate)
        .dispensedQuantity(node.get(CommonConstants.DISPENSED_QUANTITY).asDouble())
        .txNumber(node.get(CommonConstants.TX_NUMBER).asText())
        .virtualBasket(buildVirtualBasket(node))
        .auditDetails(buildAuditDetails(node))
        .auditUserDetails(buildAuditUserList(node))
        .product(buildProduct(node))
        .build();
  }

  private List<AuditUserDetails> buildAuditUserList(JsonNode prescriptionTransactionNode) {
    JsonNode workflowAuditHistoryNode = prescriptionTransactionNode.get(CommonConstants.WORK_FLOW_AUDIT_HISTORY);
    ArrayNode auditUserListNode = (ArrayNode) workflowAuditHistoryNode.get(CommonConstants.AUDIT_USER_LIST);
    if (!auditUserListNode.isEmpty()) {
      return StreamSupport.stream(auditUserListNode.spliterator(), false)
              .map(auditUserList -> auditUserList.get(CommonConstants.AUDIT_USER_DATA))
              .filter(auditUserDataNode -> !auditUserDataNode.isEmpty())
              .map(auditUserDataNode -> {
                AuditUserDetails.AuditUserDetailsBuilder auditUserDetailsBuilder = AuditUserDetails.builder();
                JsonNode userNode = auditUserDataNode.get(CommonConstants.USER);
                if (!userNode.isNull()) {
                  auditUserDetailsBuilder
                          .firstName(userNode.get(CommonConstants.FIRST_NAME).asText())
                          .lastName(userNode.get(CommonConstants.LAST_NAME).asText())
                          .idpUserId(userNode.get(CommonConstants.IDP_USER_ID).asText());
                }
                return auditUserDetailsBuilder
                        .licenceProvince(auditUserDataNode.get(CommonConstants.LICENCE_PROVINCE).asText())
                        .licenceNumber(auditUserDataNode.get(CommonConstants.LICENCE_NUMBER).asText())
                        .state(auditUserDataNode.get(CommonConstants.STATE).asText())
                        .build();
              }).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  /**
   * Builds Audit details from a json node.
   *
   * @param prescriptionTransactionNode {@link JsonNode}
   * @return {@link List<AuditDetails>}
   */
  private List<AuditDetails> buildAuditDetails(JsonNode prescriptionTransactionNode) {
    JsonNode workflowAuditHistoryNode =
        prescriptionTransactionNode.get(CommonConstants.WORK_FLOW_AUDIT_HISTORY);
    ArrayNode auditDetailsNode =
        (ArrayNode) workflowAuditHistoryNode.get(CommonConstants.AUDIT_DETAILS);
    if (auditDetailsNode == null || auditDetailsNode.isEmpty()) {
      return Collections.emptyList();
    }
    return StreamSupport.stream(auditDetailsNode.spliterator(), false)
        .map(
            auditDetail -> {
              JsonNode workflowActionNode = auditDetail.get(CommonConstants.WORK_FLOW_ACTION);
              String workflowStatus;
              workflowStatus =
                  workflowActionNode.isEmpty()
                      ? null
                      : workflowActionNode.get(CommonConstants.WORK_FLOW_STATUS).asText();
              String action = workflowActionNode.isEmpty() ? null :
                      workflowActionNode.get(CommonConstants.ACTION).asText();
              ArrayNode userDetailsNode = (ArrayNode) auditDetail.get(CommonConstants.USER_DETAILS);
              List<String> userIds = new ArrayList<>();
              List<String> auditDateTimes = new ArrayList<>();
              if (!userDetailsNode.isNull()) {

                StreamSupport.stream(userDetailsNode.spliterator(), false)
                    .forEach(
                        userDetail -> {
                          String auditDateTime =
                              userDetail.get(CommonConstants.ACT_DATE_TIME).asText();
                          auditDateTimes.add(auditDateTime);
                          JsonNode userNode = userDetail.get(CommonConstants.USER);
                          String userId = userNode.get(CommonConstants.USER_ID).asText();
                          userIds.add(userId);
                        });
              }
              //  picking  the latest user
              return AuditDetails.builder()
                  .workFlowStatus(workflowStatus)
                  .action(action)
                  .userId(userIds.isEmpty() ? null : userIds.get(userIds.size() - 1))
                  .auditDateTime(
                      auditDateTimes.isEmpty()
                          ? null
                          : auditDateTimes.get(auditDateTimes.size() - 1))
                  .build();
            })
        .collect(Collectors.toList());
  }

  /**
   * This method is used to build VirtualBasket
   *
   * @param prescriptionTransactionNode {@link JsonNode}
   * @return VirtualBasket
   */
  private VirtualBasket buildVirtualBasket(JsonNode prescriptionTransactionNode) {

    JsonNode virtualBasketNode = prescriptionTransactionNode.get(CommonConstants.VIRTUAL_BASKET);
    return (virtualBasketNode != null && !virtualBasketNode.isNull())
        ? defaultMapper.convertValue(virtualBasketNode, VirtualBasket.class)
        : null;
  }

  private Product buildProduct(JsonNode prescriptionTransactionNode) {
    Product product = new Product();
    JsonNode productNode = prescriptionTransactionNode.get(CommonConstants.PRODUCT);
    JsonNode indicationsNode = productNode.get(CommonConstants.INDICATIONS);

    List<Indication> indications =
        StreamSupport.stream(indicationsNode.spliterator(), false)
            .map(
                indicationDetail -> {
                  JsonNode diagCodeDataNode = indicationDetail.get(CommonConstants.DIAG_CODE_DATA);
                  JsonNode descriptionsNode = diagCodeDataNode.get(CommonConstants.DESCRIPTIONS);
                  List<Description> descriptions = new ArrayList<>();

                  StreamSupport.stream(descriptionsNode.spliterator(), false)
                      .forEach(
                          descriptionDetail -> {
                            if (StringUtils.equalsIgnoreCase(
                                descriptionDetail.get(CommonConstants.LANGUAGE).asText(),
                                CommonConstants.ENGLISH)) { // only interested in english lang value
                              descriptions.add(
                                  Description.builder()
                                      .lang(
                                          descriptionDetail.get(CommonConstants.LANGUAGE).asText())
                                      .value(descriptionDetail.get(CommonConstants.VALUE).asText())
                                      .build());
                            }
                          });
                  return Indication.builder()
                      .therapeuticObjective(
                          indicationDetail.get(CommonConstants.THERAPEUTIC_OBJECTIVE).asText())
                      .source(indicationDetail.get(CommonConstants.SOURCE).asText())
                      .diagCodeData(
                          DiagCodeData.builder()
                              .diagCode(diagCodeDataNode.get(CommonConstants.DIAG_CODE).asText())
                              .descriptions(descriptions)
                              .isActive(diagCodeDataNode.get(CommonConstants.IS_ACTIVE).asBoolean())
                              .build())
                      .build();
                })
            .collect(Collectors.toList());
    String category = productNode.get(CommonConstants.CATEGORY) == null
            ? null : productNode.get(CommonConstants.CATEGORY).asText();
    product.setIndications(indications);
    product.setCategory(category);
    return product;
  }
}
