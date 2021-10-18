package com.example.demo.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.UUID;

/**
 * @author Zheng kangqiang
 * @createTime 2020/6/12
 * @description 文件工具类
 */
public class FileUtil {

    /**
     * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
     * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检 查是否还有线程在读或写
     * @param mappedByteBuffer
     */
    public static void freedMappedByteBuffer(final MappedByteBuffer mappedByteBuffer) {
        try {
            if (mappedByteBuffer == null) {
                return;
            }
            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
                        //可以访问private的权限
                        getCleanerMethod.setAccessible(true);
                        //在具有指定参数的 方法对象上调用此 方法对象表示的底层方法
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(mappedByteBuffer,
                                new Object[0]);
                        cleaner.clean();
                    } catch (Exception e) {
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author
     * @date 2020/6/15
     * @param image, image]
     * @return
     * @description 将图片对象复制到路径中
     */
    public static boolean copyImg(String realPath,BufferedImage image) throws IOException {
        String newFilePath = realPath + "/" + UUID.randomUUID().toString().replace("-","") + "." + "png";
        File file = new File(newFilePath);
        file.createNewFile();
        ImageIO.write(image,"png",file);
        return true;
    }

    /**
     * 获取服务器上的图片信息
     *
     * @param iconUrl
     * @return
     * @date
     */
    public static BufferedImage getIconInfo(String iconUrl) {
        BufferedImage sourceImg = null;
        try {
            InputStream murl = new URL(iconUrl).openStream();
            sourceImg = ImageIO.read(murl);
            System.out.println(sourceImg.getWidth()); // 源图宽度
            System.out.println(sourceImg.getHeight()); // 源图高度
            System.out.println(sourceImg.getWidth() * sourceImg.getHeight());
            System.out.println(sourceImg.getData());
        } catch (IOException e) {
            System.out.println("e = " + e);
        }
        return sourceImg;
    }



    /**
     * 获取服务器上的图片信息
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static BufferedImage getImg(String iconUrl) {
        BufferedImage image = null;

        try {
            URL url = new URL(iconUrl);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            image = ImageIO.read(connection.getInputStream());
            int srcWidth = image.getWidth(); // 源图宽度
            int srcHeight = image.getHeight(); // 源图高度
            System.out.println("srcWidth = " + srcWidth);
            System.out.println("srcHeight = " + srcHeight);
        } catch (Exception e) {
            System.out.println("e = " + e);
        }


        return image;
    }


    /**
     * @author
     * @date 2020/6/15
     * realPath 放进去的路径 如 D:/file copyFile 放进去的文件
     * @return boolean
     * @description
     */
    public static boolean fileCopy(String realPath, File copyFile) throws Exception {
        if(realPath == null || copyFile == null) return false;

        // 随机生成文件名 文件路径 + UUID + 文件后缀名
        String newFilePath = realPath + "/" + UUID.randomUUID().toString().replace("-","") + "." + getFileSuffixName(copyFile);

        // 路径转文件
        File realFolder = new File(realPath);

        // 如果没有该文件夹则创建
        if(!realFolder.exists()) realFolder.mkdirs();

        // 新文件路径
        File file = new File(newFilePath);
        // 创建新文件
        file.createNewFile();

        // 复制进新的文件中
        FileUtils.CopyFile(copyFile,file);

        return true;
    }

    /**
     * @author
     * @date 2020/6/12
     * @param
     * @return java.lang.String
     * @description  获取文件路径名
     */
    public static String getFileSuffixName(File file){
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix;
    }


    /**
     * MultipartFile 转 File
     *
     * @param file
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile file) throws Exception {

        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            file = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }

    public static File transferMultipartFileToFile(MultipartFile multfile) throws IOException {
        // 获取文件名
        String fileName = multfile.getOriginalFilename();
        // 获取文件后缀
        String prefix=fileName.substring(fileName.lastIndexOf("."));
        // 用uuid作为文件名，防止生成的临时文件重复
        final File excelFile = File.createTempFile(UUID.randomUUID().toString().replace("-",""), prefix);
        // MultipartFile to File
        multfile.transferTo(excelFile);

        return excelFile;
    }

    /**
     * @author Zheng kang qiang
     * @date 2020/6/19
     * @description 获取文件后缀名
     */
    public static String getFilePrefix(File file){
        return getFilePrefix(file.getName());
    }

    public static String getMultifFilePrefix(MultipartFile file){
        return getFilePrefix(file.getOriginalFilename());
    }

    public static String getFilePrefix(String fileName){
        return fileName.substring(fileName.lastIndexOf("."));
    }

    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除本地临时文件
     * @param file
     */
    public static void delteTempFile(File file) {
        if (file != null) {
            File del = new File(file.toURI());
            del.delete();
        }
    }
}
