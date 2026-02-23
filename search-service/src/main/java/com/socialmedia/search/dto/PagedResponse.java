package com.socialmedia.search.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;
}

