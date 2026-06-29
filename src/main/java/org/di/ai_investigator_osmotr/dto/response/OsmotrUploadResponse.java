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
public class OsmotrUploadResponse {
    private String status;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("total_documents")
    private Integer totalDocuments;

    private List<OsmotrDataItemDto> documents;

    @JsonProperty("virtual_docs")
    private List<OsmotrVirtualDocDto> virtualDocs;
}
