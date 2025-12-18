package by.bsuir.documentservice.service;

import by.bsuir.documentservice.dto.DocumentResponse;
import by.bsuir.documentservice.dto.DocumentTypeResponse;
import by.bsuir.documentservice.dto.UploadDocumentRequest;
import by.bsuir.documentservice.entity.Document;
import by.bsuir.documentservice.entity.DocumentType;
import by.bsuir.documentservice.exception.ResourceNotFoundException;
import by.bsuir.documentservice.repository.DocumentRepository;
import by.bsuir.documentservice.repository.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

	private final DocumentRepository documentRepository;
	private final DocumentTypeRepository documentTypeRepository;
	private final MinioService minioService;

	@Transactional
	public DocumentResponse uploadDocument(MultipartFile file, UploadDocumentRequest request, Long userId) {
		DocumentType documentType = documentTypeRepository.findByCode(request.documentTypeCode())
				.orElseThrow(() -> new ResourceNotFoundException("DocumentType", "code", request.documentTypeCode()));

		try {
			String folder = request.entityType() + "/" + request.entityId();
			String fileKey = minioService.uploadFile(file, folder);

			String mimeType = file.getContentType();
			if (mimeType == null || mimeType.isEmpty()) {
				mimeType = "application/octet-stream";
			}

			Document document = Document.builder()
					.documentType(documentType)
					.entityType(request.entityType())
					.entityId(request.entityId())
					.fileName(file.getOriginalFilename())
					.fileKey(fileKey)
					.fileSize(file.getSize())
					.mimeType(mimeType)
					.uploadedBy(userId)
					.build();

			document = documentRepository.save(document);
			log.info("Uploaded document {} for {} {}", document.getId(), request.entityType(), request.entityId());

			return toResponse(document);
		} catch (Exception e) {
			log.error("Failed to upload document: {}", e.getMessage());
			throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
		}
	}

	@Transactional(readOnly = true)
	public DocumentResponse getDocument(Long id) {
		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
		return toResponse(document);
	}

	@Transactional(readOnly = true)
	public List<DocumentResponse> getDocumentsByEntity(String entityType, Long entityId) {
		return documentRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public InputStream downloadDocument(Long id) {
		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

		try {
			return minioService.downloadFile(document.getFileKey());
		} catch (Exception e) {
			log.error("Failed to download document: {}", e.getMessage());
			throw new RuntimeException("Failed to download document: " + e.getMessage(), e);
		}
	}

	@Transactional(readOnly = true)
	public String getDownloadUrl(Long id) {
		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

		try {
			return minioService.getPresignedUrl(document.getFileKey(), 3600);
		} catch (Exception e) {
			log.error("Failed to get download URL: {}", e.getMessage());
			throw new RuntimeException("Failed to get download URL: " + e.getMessage(), e);
		}
	}

	@Transactional(readOnly = true)
	public List<DocumentTypeResponse> getDocumentTypes() {
		return documentTypeRepository.findAll().stream()
				.map(type -> new DocumentTypeResponse(
						type.getId(),
						type.getCode(),
						type.getName(),
						type.getDescription()
				))
				.collect(Collectors.toList());
	}

	@Transactional
	public void deleteDocument(Long id) {
		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

		try {
			minioService.deleteFile(document.getFileKey());
			documentRepository.delete(document);
			log.info("Deleted document {}", id);
		} catch (Exception e) {
			log.error("Failed to delete document: {}", e.getMessage());
			throw new RuntimeException("Failed to delete document: " + e.getMessage(), e);
		}
	}

	private DocumentResponse toResponse(Document document) {
		return new DocumentResponse(
				document.getId(),
				document.getDocumentType().getCode(),
				document.getDocumentType().getName(),
				document.getEntityType(),
				document.getEntityId(),
				document.getFileName(),
				document.getFileKey(),
				document.getFileSize(),
				document.getMimeType(),
				document.getUploadedBy(),
				document.getCreatedAt()
		);
	}
}
