package com.socialmedia.post.storage;

import java.io.InputStream;

public interface MediaStorageService {

    String store(String filename, InputStream content);
}

