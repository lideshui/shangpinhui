package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.FileUploadService;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

/**
 * MinIO的文件上传ServiceImpl的实现类
 */
//添加日志注解记录失败日志
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;


    @Value("${minio.endpointUrl}")
    private String endpointUrl;



    /**
     * 将文件上传到MinIO存储空间“gmall”返回图片在线地址
     * @param file 图片文件
     * @return
     */
    @Override
    public String upload(MultipartFile file) {

        try {
            //1.判断存储空间是否存在 ,如果不存在则进行新增
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
            }

            //2.将文件上传到指定存储空间
            //2.1 生成新文件名称,避免出现同步文件覆盖
            //获取图片全名称，file.getOriginalFilename() 截取文件名称⚠️
            String allFileName = file.getOriginalFilename();
            //截取图片后缀
            String prefix = allFileName.substring(allFileName.lastIndexOf("."));
            //拼接名称
            String fileName = System.currentTimeMillis()+UUID.randomUUID().toString().replaceAll("-", "") + prefix;

            //2.2 文件上传
            String contentType = file.getContentType();
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            //3.返回上传文件的在线地址
            return endpointUrl + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            //记录失败日志
            log.error("文件上传失败:{}", e);
            throw new RuntimeException("文件上传失败!");
        }
    }
}