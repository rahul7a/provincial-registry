package com.lblw.vphx.phms.domain.prescription;


import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Product {
    private List<Indication> indications;
    private String therapeuticObjective;
    private String source;
    private String id;
    private String diagCode;
    private DiagCodeData diagCodeData;
    private String category;

}
