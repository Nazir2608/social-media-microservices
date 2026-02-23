package com.socialmedia.post.controller;

import com.socialmedia.post.dto.ApiResponse;
import com.socialmedia.post.dto.CreatePostResponse;
import com.socialmedia.post.dto.PagedResponse;
import com.socialmedia.post.dto.PostResponse;
import com.socialmedia.post.service.PostService;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
            @RequestHeader("X-User-Id") @NotNull Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "hashtags", required = false) String hashtags
    ) {
        log.info("Creating post for userId={}, captionLength={}, hashtags={}", userId,
                caption != null ? caption.length() : 0, hashtags);
        CreatePostResponse response = postService.createPost(userId, caption, hashtags, file);
        ApiResponse<CreatePostResponse> body = ApiResponse.<CreatePostResponse>builder()
                .success(true)
                .data(response)
                .message("Post created")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long id) {
        log.debug("Fetching post id={}", id);
        PostResponse response = postService.getPost(id);
        ApiResponse<PostResponse> body = ApiResponse.<PostResponse>builder()
                .success(true)
                .data(response)
                .message("Post fetched")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> listPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        log.debug("Listing posts page={}, size={}", page, size);
        PagedResponse<PostResponse> response = postService.listPosts(page, size);
        ApiResponse<PagedResponse<PostResponse>> body = ApiResponse.<PagedResponse<PostResponse>>builder()
                .success(true)
                .data(response)
                .message("Posts fetched")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        log.info("Deleting post id={}", id);
        postService.deletePost(id);
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .success(true)
                .data(null)
                .message("Post deleted")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(body);
    }
}
