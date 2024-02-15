package com.lblw.vphx.phms.domain.prescription;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Indication {
    private String therapeuticObjective;
    private String source;
    private String id;
    private DiagCodeData diagCodeData;
}
