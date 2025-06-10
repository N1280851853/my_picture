package com.whc.picture.property.picture;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "picture")
@Data
public class OssProperty {

    /**
     * oss端口
     */
    private String ossEndPoint;
    /**
     * oss访问key
     */
    private String ossAccessKeyId;
    /**
     * oss访问Secret
     */
    private String ossAccessKeySecret;
    /**
     * ossBucketName
     */
    private String ossBucketName;
}
