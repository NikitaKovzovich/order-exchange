package by.bsuir.documentservice.service;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class MinioService {

	private final MinioClient minioClient;
	private final String bucketName;

	public MinioService(
			@Value("${minio.endpoint}") String endpoint,
			@Value("${minio.access-key}") String accessKey,
			@Value("${minio.secret-key}") String secretKey,
			@Value("${minio.bucket-name}") String bucketName) {

		this.minioClient = MinioClient.builder()
				.endpoint(endpoint)
				.credentials(accessKey, secretKey)
				.build();
		this.bucketName = bucketName;

		initBucket();
	}

	private void initBucket() {
		try {
			boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
			if (!exists) {
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
				log.info("Created bucket: {}", bucketName);
			}
		} catch (Exception e) {
			log.error("Failed to initialize bucket: {}", e.getMessage());
		}
	}

	public String uploadFile(MultipartFile file, String folder) throws Exception {
		String fileName = generateFileName(file.getOriginalFilename());
		String objectName = folder + "/" + fileName;

		String contentType = file.getContentType();
		if (contentType == null || contentType.isEmpty()) {
			contentType = "application/octet-stream";
		}

		minioClient.putObject(PutObjectArgs.builder()
				.bucket(bucketName)
				.object(objectName)
				.stream(file.getInputStream(), file.getSize(), -1)
				.contentType(contentType)
				.build());

		log.info("Uploaded file: {}", objectName);
		return objectName;
	}

	public InputStream downloadFile(String fileKey) throws Exception {
		return minioClient.getObject(GetObjectArgs.builder()
				.bucket(bucketName)
				.object(fileKey)
				.build());
	}

	public void deleteFile(String fileKey) throws Exception {
		minioClient.removeObject(RemoveObjectArgs.builder()
				.bucket(bucketName)
				.object(fileKey)
				.build());
		log.info("Deleted file: {}", fileKey);
	}

	/**
	 * Загрузить PDF из byte array
	 */
	public String uploadPdfBytes(byte[] content, String folder, String fileName) throws Exception {
		String objectName = folder + "/" + fileName;

		minioClient.putObject(PutObjectArgs.builder()
				.bucket(bucketName)
				.object(objectName)
				.stream(new java.io.ByteArrayInputStream(content), content.length, -1)
				.contentType("application/pdf")
				.build());

		log.info("Uploaded PDF: {}", objectName);
		return objectName;
	}

	public String getPresignedUrl(String fileKey, int expirySeconds) throws Exception {
		return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
				.bucket(bucketName)
				.object(fileKey)
				.method(io.minio.http.Method.GET)
				.expiry(expirySeconds)
				.build());
	}

	private String generateFileName(String originalFileName) {
		String extension = "";
		if (originalFileName != null && originalFileName.contains(".")) {
			extension = originalFileName.substring(originalFileName.lastIndexOf("."));
		}
		return UUID.randomUUID() + extension;
	}
}
