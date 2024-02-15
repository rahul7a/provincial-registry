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
public class AuditUserDetails {
    private String firstName;
    private String lastName;
    private String licenceProvince;
    private String licenceNumber;
    private String idpUserId;
    private String state;

}
