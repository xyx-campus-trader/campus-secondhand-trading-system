package com.xyx.trade.common.controller;

import com.xyx.trade.user.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Api(tags = "公共模块")
@RestController
@RequestMapping("/api/common")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
            ".doc", ".docx", ".pdf", ".xls", ".xlsx", ".txt"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public AjaxResult<?> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }

        if (file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return AjaxResult.error("文件大小不能超过10MB");
        }

        String fileName = file.getOriginalFilename();
        String extensionName = "";
        if (fileName != null && fileName.contains(".")) {
            extensionName = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extensionName)) {
            return AjaxResult.error("不支持的文件类型: " + extensionName);
        }

        String newFileName = UUID.randomUUID().toString() + extensionName;

        try {
            String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            File destDir = new File(uploadPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File destFile = new File(destDir, newFileName);
            file.transferTo(destFile);

            String fileUrl = "/uploads/" + newFileName;
            return AjaxResult.success("上传成功", fileUrl);
        } catch (IOException e) {
            return AjaxResult.error("文件上传失败");
        }
    }
}
