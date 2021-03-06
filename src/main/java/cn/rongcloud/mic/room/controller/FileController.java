package cn.rongcloud.mic.room.controller;

import cn.rongcloud.common.utils.GsonUtil;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.common.rest.RestResultCode;
import cn.rongcloud.mic.jwt.JwtUser;
import cn.rongcloud.mic.jwt.filter.JwtFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Value("${rcrtc.filepath}")
    private String filepath;

    /**
     * @api {POST} /file/upload 文件上传
     * @apiVersion 1.0.0
     * @apiGroup 文件模块
     * @apiName fileupload
     * @apiParam (请求参数) {Object} file
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例
     * file=
     * @apiSuccess (响应结果) {Number} code
     * @apiSuccess (响应结果) {String} msg
     * @apiSuccess (响应结果) {Object} result
     * @apiSuccessExample 响应结果示例
     * {"msg":"wDc","result":{"useridxxxxx/aaa.png"},"code":4883}
     */
    @RequestMapping(value = "/upload")
    public RestResult upload(@RequestParam("file") MultipartFile file, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        try {
            if (file.isEmpty()) {
                return RestResult.generic(RestResultCode.ERR_REQUEST_PARA_ERR);
            }
            // 获取文件名
            String fileName = file.getOriginalFilename();
            log.info("上传的文件名为：" + fileName);
            // 获取文件的后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            log.info("文件的后缀名为：" + suffixName);
            // 设置文件存储路径
            String path = filepath+jwtUser.getUserId()+"/" + fileName;
//            String path = filepath+"aaa/" + fileName;

            File dest = new File(path);
            // 检测是否存在目录
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();// 新建文件夹
            }
            file.transferTo(dest);// 文件写入
            return RestResult.success(jwtUser.getUserId()+"/" + fileName);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return RestResult.generic(RestResultCode.ERR_OTHER,"上传失败");
    }

    /**
     * @api {GET} /file/show 文件下载
     * @apiVersion 1.0.0
     * @apiGroup 文件模块
     * @apiName fileshow
     * @apiParam (请求参数) {String} path
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例
     * path=useridxxxxx/aaa.png
     * @apiSuccess (响应结果) {Object} response
     * @apiSuccessExample 响应结果示例
     * null
     */
    @GetMapping("/show")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam("path") String path) throws Exception {
        String fileName = filepath+path;// 文件名
        InputStream in = null;
        OutputStream outputStream = null;
        try {
            in = new FileInputStream(fileName);
            outputStream = response.getOutputStream();//输出流

            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf, 0, 1024)) != -1) {
                outputStream.write(buf, 0, len);
            }
            in.close();
            outputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
