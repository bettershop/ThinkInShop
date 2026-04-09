package com.laiketui.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.List;

/**
 * okhttp:
 *   upload:
 *     max-file-size: 100MB          # 单文件最大
 *     max-total-size: 500MB         # 总文件大小
 *     max-file-count: 50            # 最大文件数量
 *     max-request-size: 10MB        # 最大文件数量MaxRequestSize
 *     allowed-types:                # 白名单
 *       - image/*
 *       - application/pdf
 *       - application/msword
 *       - application/vnd.openxmlformats-officedocument
 *       - text/*
 *       - application/zip
 *
 *   download:
 *     buffer-size: 65536             # 下载缓冲区 64KB
 *     timeout: 60s                   # 下载超时
 */
@Data
@Component
@ConfigurationProperties(prefix = "okhttp")
public class FileUploadAndDownProperties
{
    private Upload upload;
    private Download download;

    @Data
    public static class Upload {
        private DataSize maxFileSize;
        private DataSize maxTotalSize;
        private DataSize maxRequestSize;
        private int    maxFileCount;
        private List<String> allowedTypes;
    }

    @Data
    public static class Download {
        private int bufferSize = 64 * 1024; // 默认64KB
        private DataSize timeout;
    }
}
