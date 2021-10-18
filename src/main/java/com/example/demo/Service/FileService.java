package com.example.demo.Service;

import com.example.demo.model.MultipartFileParam;

import java.io.IOException;

public interface FileService {
    String chunkUploadByMappedByteBuffer(MultipartFileParam param) throws IOException;

}
