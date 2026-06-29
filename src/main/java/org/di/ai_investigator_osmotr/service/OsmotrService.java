package org.di.ai_investigator_osmotr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.di.ai_investigator_osmotr.dto.notification.OsmotrProcessingMessage;
import org.di.ai_investigator_osmotr.dto.notification.OsmotrProcessingStatus;
import org.di.ai_investigator_osmotr.dto.notification.OsmotrResultMessage;
import org.di.ai_investigator_osmotr.dto.request.OsmotrGenerateReportRequest;
import org.di.ai_investigator_osmotr.dto.response.OsmotrReportResponse;
import org.di.ai_investigator_osmotr.dto.response.OsmotrUploadResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsmotrService {

    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;

    @Value("${ai.model.url}")
    private String osmotrModelUrl;

    @Value("${osmotr.port}")
    private String osmotrModelPort;

    @Value("${spring.rabbitmq.osmotr.result.exchange}")
    private String RESULT_EXCHANGE;

    @Value("${spring.rabbitmq.osmotr.result.routing-key}")
    private String RESULT_ROUTING_KEY;

    public void processFile(InputStream fileStream, String fileName,
                            String caseNumber, OsmotrProcessingMessage originalMessage) {
        long startTime = System.currentTimeMillis();
        notifyProcessing(originalMessage);

        try {
            byte[] fileBytes = fileStream.readAllBytes();

            log.info("Osmotr step 1: uploading file {} in case {}", fileName, caseNumber);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() { return fileName; }
            };
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.APPLICATION_PDF);
            body.add("file", new HttpEntity<>(resource, fileHeaders));

            OsmotrUploadResponse uploadResponse = webClient.post()
                    .uri(osmotrModelUrl + ":" + osmotrModelPort + "/api/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(OsmotrUploadResponse.class)
                    .block();


            log.info("Osmotr step 1 done: {} documents found in case {}",
                    uploadResponse != null ? uploadResponse.getTotalDocuments() : 0, caseNumber);

            log.info("Osmotr step 2: generating report for case {}", caseNumber);

            OsmotrGenerateReportRequest reportRequest = OsmotrGenerateReportRequest.builder()
                    .sessionId(uploadResponse != null ? uploadResponse.getSessionId() : null)
                    .userId(originalMessage.getUserId())
                    .virtualDocs(uploadResponse != null ? uploadResponse.getVirtualDocs() : List.of())
                    .build();


            OsmotrReportResponse reportResponse = webClient.post()
                    .uri(osmotrModelUrl + ":" + osmotrModelPort + "/api/generate-report-direct")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(reportRequest)
                    .retrieve()
                    .bodyToMono(OsmotrReportResponse.class)
                    .block();

            long duration = (System.currentTimeMillis() - startTime) / 1000;
            log.info("Osmotr processing completed for case {} after {}s", caseNumber, duration);

            notifyCompletion(originalMessage, reportResponse, duration);

        } catch (Exception e) {
            long duration = (System.currentTimeMillis() - startTime) / 1000;
            log.error("Osmotr processing failed for file {} in case {} after {}s: {}",
                    fileName, caseNumber, duration, e.getMessage());
            notifyFailure(originalMessage, e.getMessage(), duration);
        }
    }

    public void notifyProcessing(OsmotrProcessingMessage msg) {
        sendNotification(OsmotrResultMessage.builder()
                .fileId(msg.getFileId())
                .caseNumber(msg.getCaseNumber())
                .fileName(msg.getOriginalFileName())
                .userEmail(msg.getUserEmail())
                .status(OsmotrProcessingStatus.PROCESSING)
                .timestamp(LocalDateTime.now())
                .build());
    }

    public void notifyCompletion(OsmotrProcessingMessage msg, OsmotrReportResponse result, long duration) {
        sendNotification(OsmotrResultMessage.builder()
                .fileId(msg.getFileId())
                .sessionId(result != null ? result.getSessionId() : null)
                .caseNumber(msg.getCaseNumber())
                .fileName(msg.getOriginalFileName())
                .userEmail(msg.getUserEmail())
                .status(OsmotrProcessingStatus.COMPLETED)
                .result(result)
                .timestamp(LocalDateTime.now())
                .processingDurationSeconds(duration)
                .build());
    }

    public void notifyFailure(OsmotrProcessingMessage msg, String errorMessage, long duration) {
        sendNotification(OsmotrResultMessage.builder()
                .fileId(msg.getFileId())
                .caseNumber(msg.getCaseNumber())
                .fileName(msg.getOriginalFileName())
                .userEmail(msg.getUserEmail())
                .status(OsmotrProcessingStatus.FAILED)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .processingDurationSeconds(duration)
                .build());
    }

    private void sendNotification(OsmotrResultMessage message) {
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                rabbitTemplate.convertAndSend(RESULT_EXCHANGE, RESULT_ROUTING_KEY, message);
                log.debug("Sent {} notification for file {} in case {}",
                        message.getStatus(), message.getFileId(), message.getCaseNumber());
                return;
            } catch (Exception e) {
                retryCount++;
                log.error("Failed to send notification (attempt {}/{}): {}", retryCount, maxRetries, e.getMessage());
                if (retryCount < maxRetries) {
                    try { Thread.sleep(1000L * retryCount); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
