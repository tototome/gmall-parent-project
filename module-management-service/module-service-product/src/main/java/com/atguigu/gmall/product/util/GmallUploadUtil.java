package com.atguigu.gmall.product.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class GmallUploadUtil {

    private static StorageClient1 storageClient1;

    static {
        try {
            String file = GmallUploadUtil.class.getResource("/tracker.conf").getFile();
            ClientGlobal.init(file);

            TrackerClient trackerClient = new TrackerClient();
            TrackerServer connection = trackerClient.getConnection();
            storageClient1 = new StorageClient1(connection, null);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
    }

    public synchronized static String doUpload(String originalFileName, byte[] bytes) {

        try {
            String filenameExtension = StringUtils.getFilenameExtension(originalFileName);

            return storageClient1.upload_appender_file1(bytes, filenameExtension, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return null;
    }

}
