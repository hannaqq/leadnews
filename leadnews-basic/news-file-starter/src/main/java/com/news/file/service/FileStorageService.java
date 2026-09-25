package com.news.file.service;

import java.io.InputStream;

public interface FileStorageService {



    public String uploadImgFile(String prefix, String filename,InputStream inputStream);

    public String uploadHtmlFile(String prefix, String filename,InputStream inputStream);

    public void delete(String pathUrl);

    public byte[]  downLoadFile(String pathUrl);

}
