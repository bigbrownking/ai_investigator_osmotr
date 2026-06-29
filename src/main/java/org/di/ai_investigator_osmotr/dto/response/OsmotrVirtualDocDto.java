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
public class OsmotrVirtualDocDto {
    private String title;
    private String text;

    @JsonProperty("start_page")
    private int startPage;

    @JsonProperty("end_page")
    private int endPage;
}
