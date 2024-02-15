package com.lblw.vphx.phms.domain.patient.prescription;

import com.lblw.vphx.phms.domain.common.response.Status;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Process {
    private String domainProcessName;
    private Status responseStatus;
    private String exceptionRaisedEventId;
    private String responseMessage;
    private String responseMessageFrench;
    private List<ExceptionItem> exceptionItem;
}
