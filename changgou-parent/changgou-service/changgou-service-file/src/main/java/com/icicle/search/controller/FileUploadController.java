package com.icicle.search.controller;

import com.icicle.file.FastDFSFile;
import com.icicle.util.FastDFSClient;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Max
 * @Date 16:31 2019/8/27
 * @Description：文件上传控制层
 **/
@RestController
public class FileUploadController {


    @PostMapping(value = "/upload")
    public Result upload(MultipartFile file) throws Exception {
        //封装一个FastDFSFile
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),  //文件名称
                file.getBytes(),  //文件内容
                StringUtils.getFilenameExtension(file.getOriginalFilename())); //文件扩展名

        //文件上传 调用FastDFSClient工具类实现文件上传
        String[] uploads = FastDFSClient.upload(fastDFSFile);

        //返回图片的访问路径
        String url = "http://192.168.211.132:8080/"+uploads[0]+"/"+uploads[1];
        return new Result(true, StatusCode.OK,"文件上传成功",url);
    }
}
