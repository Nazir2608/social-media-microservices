package com.socialmedia.search.controller;

import com.socialmedia.search.document.PostDocument;
import com.socialmedia.search.dto.ApiResponse;
import com.socialmedia.search.dto.PagedResponse;
import com.socialmedia.search.service.PostSearchService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final PostSearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostDocument>>> searchPosts(@RequestParam String query, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Page<PostDocument> result = searchService.searchByText(query, page, size);
        PagedResponse<PostDocument> payload = PagedResponse.<PostDocument>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
        ApiResponse<PagedResponse<PostDocument>> body = ApiResponse.<PagedResponse<PostDocument>>builder()
                .success(true)
                .data(payload)
                .message("Search results")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/hashtag/{tag}")
    public ResponseEntity<ApiResponse<PagedResponse<PostDocument>>> searchByHashtag(@PathVariable String tag, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Page<PostDocument> result = searchService.searchByHashtag(tag, page, size);
        PagedResponse<PostDocument> payload = PagedResponse.<PostDocument>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
        ApiResponse<PagedResponse<PostDocument>> body = ApiResponse.<PagedResponse<PostDocument>>builder()
                .success(true)
                .data(payload)
                .message("Search results")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(body);
    }
}
