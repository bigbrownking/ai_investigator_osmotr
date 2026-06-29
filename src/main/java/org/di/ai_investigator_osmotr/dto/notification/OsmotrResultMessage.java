package org.di.ai_investigator_osmotr.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.di.ai_investigator_osmotr.dto.response.OsmotrReportResponse;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsmotrResultMessage {
    private Long fileId;
    private String sessionId;
    private String caseNumber;
    private String fileName;
    private String userEmail;
    private OsmotrProcessingStatus status;
    private OsmotrReportResponse result;
    private String errorMessage;
    private LocalDateTime timestamp;
    private long processingDurationSeconds;
}
