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
@Getter
@Setter
public class DiagCodeData {
    List<Description> descriptions;
    private String diagCode;
    private Boolean isActive;
}
