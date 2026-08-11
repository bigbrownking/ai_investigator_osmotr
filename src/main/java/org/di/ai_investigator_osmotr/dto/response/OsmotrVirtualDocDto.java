package org.di.ai_investigator_osmotr.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OsmotrVirtualDocDto {
    private String title;
    private String text;

    @JsonProperty("start_page")
    private Integer startPage;

    @JsonProperty("end_page")
    private Integer endPage;
}