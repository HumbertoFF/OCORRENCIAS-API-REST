package com.humberto.api.ocorrencias.service;

import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class MinioStorageService {

  private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

  private final MinioClient minioClient;

  @Value("${minio.bucket}")
  private String bucket;

  @Value("${minio.public-endpoint}")
  private String publicEndpoint;

  public MinioStorageService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  public record UploadResult(String pathBucket, String hash) {}

  public UploadResult upload(MultipartFile arquivo, String prefixo) {
    try {
      garantirBucket();

      String extensao = obterExtensao(arquivo.getOriginalFilename());
      String objectName = prefixo + "/" + UUID.randomUUID() + extensao;
      String hash = calcularHash(arquivo.getBytes());

      minioClient.putObject(
        PutObjectArgs.builder()
          .bucket(bucket)
          .object(objectName)
          .stream(arquivo.getInputStream(), arquivo.getSize(), -1)
          .contentType(arquivo.getContentType())
          .build()
      );

      log.info("Arquivo enviado ao MinIO: {}", objectName);
      return new UploadResult(objectName, hash);

    } catch (Exception e) {
      log.error("Erro ao fazer upload para o MinIO", e);
      throw new RuntimeException("Falha ao armazenar arquivo: " + e.getMessage(), e);
    }
  }

  private void garantirBucket() throws Exception {
    boolean existe = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    if (!existe) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
      log.info("Bucket criado: {}", bucket);
    }
  }

  private String obterExtensao(String nomeArquivo) {
    if (nomeArquivo != null && nomeArquivo.contains(".")) {
      return nomeArquivo.substring(nomeArquivo.lastIndexOf("."));
    }
    return "";
  }

  private String calcularHash(byte[] bytes) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return HexFormat.of().formatHex(digest.digest(bytes));
  }

  public String gerarUrlDownload(String objectName) {
    return publicEndpoint + "/" + bucket + "/" + objectName;
  }
}
