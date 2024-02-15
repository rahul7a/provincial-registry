package com.lblw.vphx.phms.domain.coding;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
public class CodeableConcept {

  private ObjectId id;
  private List<Coding> coding;
  @Indexed private CodeableConceptType type;
}
