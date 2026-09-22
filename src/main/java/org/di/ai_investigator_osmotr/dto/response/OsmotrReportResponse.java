package org.di.ai_investigator_osmotr.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsmotrReportResponse {
    private String status;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("report_filename")
    private String reportFilename;

    @JsonProperty("download_url")
    private String downloadUrl;

    @JsonProperty("report_file_base64")
    private String reportFileBase64;

    @JsonProperty("report_txt")
    private String reportTxt;

    @JsonProperty("results")
    private List<OsmotrDataItemDto> data;
}