package org.di.ai_investigator_osmotr.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsmotrDataItemDto {
    @JsonProperty("doc_id")
    private String docId;

    @JsonProperty("start_page")
    private Integer startPage;

    @JsonProperty("end_page")
    private Integer endPage;

    @JsonProperty("inspection_text")
    private String inspectionText;

    private Boolean needed;
}