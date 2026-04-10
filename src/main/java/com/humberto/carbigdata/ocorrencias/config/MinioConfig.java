package com.humberto.carbigdata.ocorrencias.config;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

  private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

  @Value("${minio.endpoint}")
  private String endpoint;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Value("${minio.public-endpoint}")
  private String publicEndpoint;

  @Bean
  public MinioClient minioClient() {
    log.info("Conectando ao MinIO: {}", endpoint);
    return MinioClient.builder()
      .endpoint(endpoint)
      .credentials(accessKey, secretKey)
      .build();
  }

  @Bean("minioPublicClient")
  public MinioClient minioPublicClient() {
    log.info("MinIO client público (presigned URLs): {}", publicEndpoint);
    return MinioClient.builder()
      .endpoint(publicEndpoint)
      .credentials(accessKey, secretKey)
      .build();
  }
}
