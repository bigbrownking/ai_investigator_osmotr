package org.di.ai_investigator_osmotr.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsmotrProcessingMessage {
    private Long fileId;
    private String fileUrl;
    private String originalFileName;
    private String caseNumber;
    private String userEmail;
    private Long userId;
}
