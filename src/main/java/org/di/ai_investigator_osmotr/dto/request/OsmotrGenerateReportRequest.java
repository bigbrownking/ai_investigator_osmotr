package org.di.ai_investigator_osmotr.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.di.ai_investigator_osmotr.dto.response.OsmotrVirtualDocDto;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsmotrGenerateReportRequest {
    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("virtual_docs")
    private List<OsmotrVirtualDocDto> virtualDocs;
}
