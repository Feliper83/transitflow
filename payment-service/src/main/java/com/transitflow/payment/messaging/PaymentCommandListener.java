package com.transitflow.payment.messaging;

import com.transitflow.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentCommandListener {

    public static final String COMMANDS_TOPIC = "payment.commands";
    public static final String EVENTS_TOPIC = "payment.events";

    private static final Logger log = LoggerFactory.getLogger(PaymentCommandListener.class);

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafka;

    public PaymentCommandListener(PaymentService paymentService, KafkaTemplate<String, Object> kafka) {
        this.paymentService = paymentService;
        this.kafka = kafka;
    }

    @KafkaListener(topics = COMMANDS_TOPIC, groupId = "payment-service")
    public void onCommand(ProcessPaymentCommand command) {
        MDC.put("correlationId", command.correlationId());
        try {
            PaymentResultEvent result = paymentService.process(command);
            kafka.send(EVENTS_TOPIC, String.valueOf(result.bookingId()), result);
            log.info("Published {} for booking {}", result.status(), result.bookingId());
        } finally {
            MDC.clear();
        }
    }
}
