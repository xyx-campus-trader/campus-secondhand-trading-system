package com.xyx.trade.common.controller;

import com.xyx.trade.user.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传Controller
 */
@Api(tags = "公共模块")
@RestController
@RequestMapping("/api/common")
public class FileController {

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public AjaxResult<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 获取文件后缀名
        String extensionName = "";
        if (fileName != null && fileName.contains(".")) {
            extensionName = fileName.substring(fileName.lastIndexOf("."));
        }

        // 生成唯一文件名
        String newFileName = UUID.randomUUID().toString() + extensionName;

        try {
            // 获取项目根目录下的 uploads 文件夹
            String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            File destDir = new File(uploadPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File destFile = new File(destDir, newFileName);
            file.transferTo(destFile);

            // 返回文件访问URL
            String fileUrl = "/uploads/" + newFileName;
            return AjaxResult.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return AjaxResult.error("文件上传失败：" + e.getMessage());
        }
    }
}
