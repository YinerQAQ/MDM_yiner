package com.maike.mdm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogQueryDTO {

    private String userId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page;
    private Integer size;

    public Integer getPage() {
        return page != null && page > 0 ? page : 1;
    }

    public Integer getSize() {
        return size != null && size > 0 ? size : 10;
    }
}
