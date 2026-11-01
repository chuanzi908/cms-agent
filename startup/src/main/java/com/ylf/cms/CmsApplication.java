package com.ylf.cms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CMS 启动类（startup 模块，聚合所有模块）
 * 包扫描根包 com.ylf.cms，确保 M1/M2/M3/M4 的 Service/Controller/Mapper 全部注册
 * MapperScan 只扫各模块 mapper 包，避免将 Service 接口误注册为 Mapper
 */
@SpringBootApplication(scanBasePackages = "com.ylf.cms")
@MapperScan({
        "com.ylf.cms.security.mapper",
        "com.ylf.cms.content.mapper",
        "com.ylf.cms.catalog.mapper",
        "com.ylf.cms.ops.mapper"
})
public class CmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }
}
