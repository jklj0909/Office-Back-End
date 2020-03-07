package com.office.common.utils;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CodecUtils {
    public static String md5Hex(String data, String salt) {
        if (StringUtils.isBlank(salt)) {
            salt = data.hashCode() + "";
        }
        return DigestUtils.md5Hex(salt + DigestUtils.md5Hex(data));
    }

    public static String generateSalt() {
        return StringUtils.replace(UUID.randomUUID().toString(), "-", "");
    }

    public static String generateUUID() {
        String head = StringUtils.replace(UUID.randomUUID().toString(), "-", "").substring(0, 10);
        String body = new SimpleDateFormat("YYYYMMddhhmmss").format(new Date());
        return head + body;
    }
}
