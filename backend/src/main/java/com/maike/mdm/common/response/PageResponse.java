package com.maike.mdm.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> records;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;

    public static <T> PageResponse<T> of(List<T> records, long total, int pageNum, int pageSize) {
        int pages = (int) Math.ceil((double) total / pageSize);
        return PageResponse.<T>builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(pages)
                .build();
    }
}