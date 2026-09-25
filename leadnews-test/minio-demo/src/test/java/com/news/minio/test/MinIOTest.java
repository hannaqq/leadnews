package com.news.minio.test;

import com.news.file.service.FileStorageService;
import com.news.minio.MinIOApplication;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SpringBootTest(classes = MinIOApplication.class)
@ExtendWith(SpringExtension.class)
public class MinIOTest {

    @Autowired
    private FileStorageService fileStorageService;

/*    @Test
    public void test() throws FileNotFoundException {
*//*        String path = fileStorageService.uploadHtmlFile("", "list.html", new FileInputStream("D:\\Qiao\\list.html"));
        System.out.println(path);*/


    public static void main(String[] args) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        FileInputStream fileInputStream = new FileInputStream("D:\\Qiao\\plugins\\js\\axios.min.js");
        MinioClient minioClient = MinioClient.builder().credentials("minio", "minio123").endpoint("http://172.29.171.167:9000").build();
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .object("plugins/js/axios.min.js")
                .contentType("text/js")
                .bucket("leadnews")
                .stream(fileInputStream,fileInputStream.available(),-1)
                .build();
        minioClient.putObject(putObjectArgs);
        //System.out.println("http://172.29.171.167:9000/leadnews/list.html");
    }
}
