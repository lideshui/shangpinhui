package com.atguigu.gmall.product.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO的文件上传Service类
 */
public interface FileUploadService {
    /**
     * 文件上传到MinIO
     * @param file
     * @return
     */
    String upload(MultipartFile file);
}