package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.FileUploadService;
import io.minio.*;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${minio.endpointUrl}")
    private String endpointUrl;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secreKey}")
    private String secreKey;

    @Value("${minio.bucketName}")
    private String bucketName;


    /**
     * 将文件上传到MinIO存储空间“gmall”返回图片在线地址
     *
     * @param file 图片文件
     * @return
     */
    @Override
    public String upload(MultipartFile file) {
        try {
            //1.创建操作MinIO存储客户端对象
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(endpointUrl)
                            .credentials(accessKey, secreKey)
                            .build();

            //2.判断存储空间是否存在
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                //如果存储空间不存在则创建
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("asiatrip").build());
            }

            //3.上传文件到MinIO，通过UUID生成随机的名称防止重复被覆盖
            String fileName = System.currentTimeMillis() + UUID.randomUUID().toString();
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            //4.返回在线地址，拼接在线地址
            String url = endpointUrl + "/" + bucketName + "/" + fileName;
            return url;
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
        }
        return null;
    }
}