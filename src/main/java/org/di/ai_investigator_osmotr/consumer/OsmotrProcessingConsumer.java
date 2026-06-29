package org.di.ai_investigator_osmotr.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.di.ai_investigator_osmotr.dto.notification.OsmotrProcessingMessage;
import org.di.ai_investigator_osmotr.service.MinioService;
import org.di.ai_investigator_osmotr.service.OsmotrService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsmotrProcessingConsumer {

    private final MinioService minioService;
    private final OsmotrService osmotrService;

    @RabbitListener(queues = "${spring.rabbitmq.osmotr.queue}")
    public void processFile(OsmotrProcessingMessage message) {
        log.info("Received osmotr task: {} in case {} from {}",
                message.getOriginalFileName(), message.getCaseNumber(), message.getUserEmail());

        try {
            InputStream fileStream = minioService.downloadFile(message.getFileUrl());

            osmotrService.processFile(
                    fileStream,
                    message.getOriginalFileName(),
                    message.getCaseNumber(),
                    message
            );

        } catch (Exception e) {
            log.error("Failed to process osmotr file {} in case {}: {}",
                    message.getOriginalFileName(), message.getCaseNumber(), e.getMessage());
            osmotrService.notifyFailure(message, e.getMessage(), 0);
        }
    }
}
