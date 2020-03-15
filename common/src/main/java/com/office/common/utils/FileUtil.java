package com.office.common.utils;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class FileUtil {
    public static void download(String filename, String parentPath, HttpServletResponse res) throws IOException {
        // 发送给客户端的数据
        OutputStream outputStream = res.getOutputStream();
        FileInputStream input = new FileInputStream(new File(parentPath + "/" + filename));
        byte[] buff = new byte[2048];
        int len;
        while ((len = input.read(buff)) != -1) {
            outputStream.write(buff, 0, len);
        }
        //避免出现文件损坏的情况
        res.setHeader("Content-Length", String.valueOf(input.getChannel().size()));
        input.close();
        outputStream.close();
    }

    /**
     * 获取文件后缀
     *
     * @param fileType 试题类型
     * @return 返回文件后缀
     * @author jie
     **/
    public static String getFileSuffix(String fileType) {
        if (StringUtils.equals(fileType, "word")) {
            return ".docx";
        } else if (StringUtils.equals(fileType, "ppt")) {
            return ".pptx";
        } else if (StringUtils.equals(fileType, "excel")) {
            return ".xlsx";
        }
        return null;
    }
}
