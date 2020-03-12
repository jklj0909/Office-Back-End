package com.office.common.utils;

import com.office.common.entity.MyElement;
import org.apache.commons.io.FileUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class XmlDiffUtils {
    private static ArrayList<String> strings = new ArrayList<String>();

    public static void copyFile(String filename, String newFilename, String destination) {
        File outerFile = new File(destination);
        if (!outerFile.exists()) {
            outerFile.mkdirs();
        }
        File file = new File(filename);
        if (file.isDirectory()) {
            if (!new File(destination + "/" + newFilename).exists()) {
                new File(destination + "/" + newFilename).mkdirs();
            }
            for (File ChildFile : file.listFiles()) {
                copyFile(ChildFile.getPath(), "(1)" + ChildFile.getName(), destination + "/" + newFilename);
            }
        } else if (file.isFile()) {
            File newFile = new File(destination + "/" + newFilename);
            BufferedOutputStream bos = null;
            BufferedInputStream bis = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(newFile));
                bis = new BufferedInputStream(new FileInputStream(file));
                byte[] bytes = new byte[1024];
                int len = -1;
                while ((len = bis.read(bytes)) != -1) {
                    bos.write(bytes, 0, len);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            for (File ChildFile : file.listFiles()) {
                deleteFile(ChildFile);
            }
            file.delete();
        }
    }

    public static void zipMyFile(String filepath, String newFilename) {
        filepath = filepath.replaceAll("\\\\", "/");
        newFilename = newFilename.replaceAll("\\\\", "/");
        String direction = filepath.substring(0, filepath.lastIndexOf("/"));
        String newFile = direction + "/" + newFilename + ".zip";
        copyFile(filepath, newFilename + ".zip", direction);
        ZipFile zipFile = null;
        File file = new File(direction + "/" + newFilename);
        if (file.exists()) {
            deleteFile(file);
        }
        file.mkdirs();
        try {
            zipFile = new ZipFile(newFile);
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                String temp = zipEntry.getName();
                if (temp.contains("/")) {
                    File f1 = new File(file.getPath() + "/" + temp.substring(0, temp.lastIndexOf("/")));
                    if (!f1.exists()) {
                        f1.mkdirs();
                    }
                }
                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(file.getPath() + "/" + temp);
                byte[] bytes = new byte[1024];
                int len = -1;
                while ((len = is.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                }
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        deleteFile(new File(newFile));
    }

    /**
     * 存储差异节点
     *
     * @param type 匹配类型
     * @param me   节点
     */
    private static void storeNodes(MyElement me, String type) {
        if ((type.equals("old") && me.getNote().equals("del")) || (type.equals("new") && !me.getNote().equals("mat"))) {
            Element element = me.getElement();
            StringBuilder s = new StringBuilder();
            s.append(me.getNote() + "-" + element.getPath() + "-" + element.getTextTrim() + " -");
            for (Attribute attribute : element.attributes()) {
                s.append(attribute.getName() + ":" + attribute.getValue());
            }
            strings.add(s.toString());
        }
        if (me.getSons().size() == 0) {
            return;
        }
        for (MyElement e : me.getSons()) {
            storeNodes(e, type);
        }
        if (me.getFather() == null) {
            return;
        }
    }

    /**
     * 比较xml
     *
     * @param xmlPath_1 xml路径1
     * @param xmlPath_2 xml路径2
     */
    private static void diffTwoXml(String xmlPath_1, String xmlPath_2) {
        DIFF.elementHashMap.clear();
        DIFF.elementHashMap1.clear();
        SAXReader saxReader1 = new SAXReader();
        SAXReader saxReader2 = new SAXReader();
        try {
            Document document1 = saxReader1.read(xmlPath_1);
            Document document2 = saxReader2.read(xmlPath_2);
            DIFF.wrapNode(document1.getRootElement(), null, 1, 1);
            DIFF.wrapNode(document2.getRootElement(), null, 1, 2);
            DIFF.countStatics(DIFF.rootElement, DIFF.elementHashMap);
            DIFF.countStatics(DIFF.rootElement1, DIFF.elementHashMap1);
            DIFF.matchNodes();
            DIFF.matchRemainedNode(DIFF.rootElement);
            DIFF.noteNodes(DIFF.rootElement, 1);
            DIFF.noteNodes(DIFF.rootElement1, 2);
            DIFF.nodeMovedNodes(DIFF.rootElement);
            storeNodes(DIFF.rootElement, "old");
            storeNodes(DIFF.rootElement1, "new");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 比较xml
     */
    public static ArrayList<String> dealXmlDiff(String filepath_1, String filepath_2, String destination) {
        strings.clear();
        zipMyFile(filepath_1, "old(1)");
        zipMyFile(filepath_2, "new(1)");
        destination = destination.replaceAll("\\\\", "/");
        diffTwoXml(destination + "/old(1)/word/document.xml", destination + "/new(1)/word/document.xml");
        try {
            FileUtils.deleteDirectory(new File(destination + "/old(1)"));
            FileUtils.deleteDirectory(new File(destination + "/new(1)"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> arrayList = new ArrayList<>(strings);
        return arrayList;
    }
}