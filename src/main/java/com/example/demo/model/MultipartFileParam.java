package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Zheng kangqiang
 * @createTime 2020/6/18
 * @description 文件传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultipartFileParam {
    /**
     * @author Zheng kang qiang
     * @date 2020/6/18
     * @description 文件传输任务ID
     */
    private  String taskId;

    /**
     * @author Zheng kang qiang
     * @date 2020/6/18
     * @description 为第几分片
     */
    private int chunk;

    /**
     * @author Zheng kang qiang
     * @date 2020/6/18
     * @description  分片总数
     */
    private int chunkTotal;

    /**
     * @author Zheng kang qiang
     * @date 2020/6/18
     * @description 文件传输对象
     */
    private MultipartFile file;

    /**
     * @author Zheng kang qiang
     * @date 2020/6/18
     * @description 每个分块的大小
     */
    private long size;

}
