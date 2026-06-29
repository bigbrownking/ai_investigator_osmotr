package org.di.ai_investigator_osmotr.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsmotrReportResponse {
    private String status;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("report_file")
    private String reportFile;

    @JsonProperty("report_txt")
    private String reportTxt;

    private List<OsmotrDataItemDto> results;
    private List<OsmotrDataItemDto> data;
}
