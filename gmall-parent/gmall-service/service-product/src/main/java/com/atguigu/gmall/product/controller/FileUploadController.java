package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO的文件上传控制器
 */
@RestController
@RequestMapping("admin/product")
public class FileUploadController {



    @Autowired
    private FileUploadService fileUploadService;


    /**
     * 文件上传
     * @param file 上传的文件
     * @return
     * @throws Exception
     */
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception {
        String url = fileUploadService.upload(file);
        //  将文件上传之后的路径返回给页面！
        return Result.ok(url);
    }
}
