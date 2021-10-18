package com.example.demo.controller;

import com.example.demo.model.MultipartFileParam;
import com.example.demo.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Zheng kangqiang
 * @createTime 2020/6/12
 * @description 文件接口类
 */
@RequestMapping("/file")
@RestController
@CrossOrigin
@Api(tags = {"文件接口"},description = "文件接口类")
public class FileController {

    private static String fileUploadTempDir = "D:/file/fileuploaddir";
    private static String fileUploadDir = "D:/file/file";

    /**
     * @author Zheng kang qiang
     * @date 2020/6/19
     * @description  续传接口
     */
    @PostMapping("/resume")
    @ApiOperation(value = "续传接口",notes = "file对象从request中获取")
    public Map resume(@ApiParam(name = "req",value = "request对象",required = true) HttpServletRequest req) throws Exception {
        // 获取需要用到的信息
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) req; // 获取请求对象
        MultipartFile file = multipartHttpServletRequest.getFile("data"); // 获取分片的数据文件
        System.out.println(file.getContentType());
        int index = Integer.parseInt(multipartHttpServletRequest.getParameter("index")); // 获取当前第几位
        int total = Integer.parseInt(multipartHttpServletRequest.getParameter("total")); // 获取总片数
        int shardSize = Integer.parseInt(multipartHttpServletRequest.getParameter("shardSize"));
        String fileName = multipartHttpServletRequest.getParameter("name"); // 获取文件名
        String fileEnd = fileName.substring(fileName.lastIndexOf(".")); // 获取文件后缀名
        String uuid = multipartHttpServletRequest.getParameter("uuid"); // 前端uuid，作为标识
        Map<String, Object> map = new HashMap<>(); // 返回的数据

        // 创建一个文件夹对象 如果不存在则创建
        File folderFile = new File(fileUploadDir);
        if(!folderFile.exists()) folderFile.mkdirs();
        // 创建要写入的文件
        File uploadFile = new File(fileUploadDir + "/" + uuid + "." + fileEnd);
        if(!uploadFile.exists()) uploadFile.createNewFile();
        map.put("status", index < total? 201 : 200);

        RandomAccessFile readFile = new RandomAccessFile(FileUtil.multipartFileToFile(file),"rw");
        RandomAccessFile writeFile = new RandomAccessFile(uploadFile, "rw");

        int position = (index - 1) * shardSize; // 从哪个位置开始写入
        writeFile.seek(position);

        byte[] buf = new byte[(int)readFile.length()];
        int byteCount = 0;
        while ((byteCount = readFile.read(buf)) != -1) {
            // 如果不等于则证明 当前读取出来的数比要读的小 证明到了最后一个 则将最后一个赋值到buf
            if (byteCount != shardSize) {
                byte[] tempBytes = new byte[byteCount];
                System.arraycopy(buf, 0, tempBytes, 0, byteCount);
                buf = tempBytes;
            }
            writeFile.write(buf);
            // 从哪里开始写入
            position = position + byteCount;
        }
        readFile.close();
        writeFile.close();
        return map;
    }

    /**
     * @author Zheng kang qiang
     * @date 2020/6/19
     * @description 用来上传的方法
     */
    @RequestMapping("/doPost")
    @ResponseBody
    public Map fragmentation(HttpServletRequest req) {
//        resp.addHeader("Access-Control-Allow-Origin", "*");
        Map<String, Object> map = new HashMap<>();

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) req;

        // 获得文件分片数据
        MultipartFile file = multipartRequest.getFile("data");
//        分片第几片
        int index = Integer.parseInt(multipartRequest.getParameter("index"));
//        总片数
        int total = Integer.parseInt(multipartRequest.getParameter("total"));
//        获取文件名
        String fileName = multipartRequest.getParameter("name");
        String name = fileName.substring(0, fileName.lastIndexOf("."));
        String fileEnd = fileName.substring(fileName.lastIndexOf("."));
//        前端uuid，作为标识
        String uuid = multipartRequest.getParameter("uuid");

        File uploadFile = new File(fileUploadTempDir + "/" + uuid, uuid + name + index + ".tem");

        if (!uploadFile.getParentFile().exists()) {
            uploadFile.getParentFile().mkdirs();
        }

        if (index < total) {
            try {
                file.transferTo(uploadFile);
                // 上传的文件分片名称
                map.put("status", 201);
                return map;
            } catch (IOException e) {
                e.printStackTrace();
                map.put("status", 502);
                return map;
            }
        } else {
            try {
                file.transferTo(uploadFile);
                // 上传的文件分片名称
                map.put("status", 200);
                return map;
            } catch (IOException e) {
                e.printStackTrace();
                map.put("status", 502);
                return map;
            }
        }
    }

    /**
     * @author Zheng kang qiang
     * @date 2020/6/19
     * @description  用来放进去
     */
    @RequestMapping(value = "/merge", method = RequestMethod.GET)
    @ResponseBody
    public Map merge(@ApiParam(name = "uuid",value = "UUID",required = true) String uuid, String newFileName) {
        Map retMap = new HashMap();
        try {
            File dirFile = new File(fileUploadTempDir + "/" + uuid);
            if (!dirFile.exists()) {
                throw new RuntimeException("文件不存在！");
            }
            //分片上传的文件已经位于同一个文件夹下，方便寻找和遍历（当文件数大于十的时候记得排序用冒泡排序确保顺序是正确的）
            String[] fileNames = dirFile.list();

            // 先创建文件夹
            File file = new File(fileUploadDir);
            if(file.exists()) file.createNewFile();

//       创建空的合并文件
            File targetFile = new File(fileUploadDir, newFileName);
            // 如果不存在则创建
            if(!targetFile.exists()) targetFile.createNewFile();

            RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw");

            // 遍历所有的文件进行写入
            int position = 0; // 记录每一次写入的位置
            for (String fileName : fileNames) {
                System.out.println(fileName);
                File sourceFile = new File(fileUploadTempDir + "/" + uuid, fileName);
                RandomAccessFile readFile = new RandomAccessFile(sourceFile, "rw");
                int chunksize = 1024 * 3; // 一次读取3m
                byte[] buf = new byte[chunksize];
                writeFile.seek(position);
                int byteCount = 0;
                while ((byteCount = readFile.read(buf)) != -1) {
                    // 如果不等于则证明 当前读取出来的数比要读的小 证明到了最后一个 则将最后一个赋值到buf
                    if (byteCount != chunksize) {
                        byte[] tempBytes = new byte[byteCount];
                        System.arraycopy(buf, 0, tempBytes, 0, byteCount);
                        buf = tempBytes;
                    }
                    writeFile.write(buf);
                    // 从哪里开始写入
                    position = position + byteCount;
                }
                readFile.close();
                FileUtils.deleteQuietly(sourceFile);//删除缓存的临时文件
            }
            writeFile.close();
            retMap.put("code", "200");
        }catch (IOException e){
            e.printStackTrace();
            retMap.put("code", "500");
        }
        return retMap;
    }



    @PostMapping("chunkUpload")
    public void fileChunkUpload(MultipartFileParam param, HttpServletResponse response, HttpServletRequest request){
        /**
        * 判断前端Form表单格式是否支持文件上传
        */
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if(!isMultipart){
             return;
        }
//        String taskId = fileManage.chunkUploadByMappedByteBuffer(param);
    }

    @GetMapping("/img")
    public String getImg(String url) throws IOException {
        BufferedImage img = FileUtil.getImg(url);
        // 随机生成文件名 文件路径 + UUID + 文件后缀名
        FileUtil.copyImg("D:/file",img);
        System.out.println("img = " + img);
        return "OK";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file")MultipartFile file) throws Exception {
        FileUtil.fileCopy("D:/file", FileUtil.multipartFileToFile(file));
        return "OK";
    }

    @RequestMapping("/uniupload")
    public String uniUpload(HttpServletRequest request){
//        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(request.getSession()
//        .getServletContext());


//        commonsMultipartResolver.setDefaultEncoding("utf-8");

//        if (commonsMultipartResolver.isMultipart(request)){
            MultipartHttpServletRequest mulReq = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> map = mulReq.getFileMap();

            // key为前端的name属性，value为上传的对象（MultipartFile）
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                System.out.println("entry = " + entry);
                // 自己的保存文件逻辑
//                saveOrUpdateImageFile(feedback.getId(), entry.getKey(), entry.getValue());
            }
//        }


        return "OK";
    }
}
