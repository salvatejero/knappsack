package com.sparc.knappsack.components.services;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.sparc.knappsack.components.events.sqs.SQSEventDelivery;
import com.sparc.knappsack.components.events.sqs.SQSEventDeliveryFactory;
import com.sparc.knappsack.models.EmailModel;
import com.sparc.knappsack.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SQSEmailServiceWorker {

    private static final Logger log = LoggerFactory.getLogger(SQSEmailServiceWorker.class);
    private static final String APPROXIMATE_RECEIVE_COUNT_ATTRIBUTE_NAME = "ApproximateReceiveCount";

    @Qualifier("sqsEventDeliveryFactory")
    @Autowired(required = true)
    private SQSEventDeliveryFactory sqsEventDeliveryFactory;

    private AmazonSQSClient sqsClient;

    private String awsAccessKey;
    private String awsSecretKey;
    private String sqsQueueName;
    private String queueUrl;

    @Async
    @Scheduled(fixedRate = 15000)
    public void execute() {
        String threadName = Thread.currentThread().getName();
        log.info(String.format("SQS thread working: %s", threadName));

        for (Message message : receiveMessagesFromQueue(10)) {
            boolean success = false;
            EmailModel emailModel = JsonUtil.unmarshall(message.getBody(), EmailModel.class);
            convertIntegersToLongs(emailModel);
            if (emailModel != null) {
                SQSEventDelivery deliveryMechanism = sqsEventDeliveryFactory.getEventDelivery(emailModel.getEventType());
                if (deliveryMechanism != null) {
                    success = deliveryMechanism.sendNotifications(emailModel);
                }

                if (success) {
                    deleteMessageFromQueue(message);
                } else {
                    checkMessageReceiveCount(message, 5);
                }
            } else {
                deleteMessageFromQueue(message);
            }
        }

        log.info(String.format("SQS thread ending: %s", threadName));

    }

    @PostConstruct
    private void init() {
        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        this.sqsClient = new AmazonSQSClient(credentials);

        GetQueueUrlRequest request = new GetQueueUrlRequest(sqsQueueName);
        this.queueUrl = sqsClient.getQueueUrl(request).getQueueUrl();
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public void setSqsQueueName(String sqsQueueName) {
        this.sqsQueueName = sqsQueueName;
    }

    private List<Message> receiveMessagesFromQueue(int maxNumberOfMessages) {
        List<Message> messages = new ArrayList<Message>();

        ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);
        request.setMaxNumberOfMessages(maxNumberOfMessages);
        request.withAttributeNames("All");
        ReceiveMessageResult result = sqsClient.receiveMessage(request);

        if (result != null && result.getMessages() != null) {
            messages.addAll(result.getMessages());
        }

        return messages;
    }

    private void deleteMessageFromQueue(Message message) {
        if (message != null) {
            DeleteMessageRequest request = new DeleteMessageRequest(queueUrl, message.getReceiptHandle());
            try {
                log.info(String.format("Deleting message from queue %s: %s", sqsQueueName, message.getMessageId()));
                sqsClient.deleteMessage(request);
            } catch (AmazonClientException e) {
                log.error(String.format("Error while deleting message from queue %s: %s", sqsQueueName, message.getMessageId()));
            }
        }
    }

    /**
     * Checks to see if a given message has been read too many times and delete if necessary.
     * @param message Amazon SQS Message
     * @param maxReceiveCount Maximum number of times a message should be read.
     */
    private void checkMessageReceiveCount(Message message, int maxReceiveCount) {
        if (message != null && maxReceiveCount > 0) {
            try {
                int receiveCount = Integer.valueOf(message.getAttributes().get(APPROXIMATE_RECEIVE_COUNT_ATTRIBUTE_NAME));
                //TODO: Instead of deleting messages off of queue put into separate error queue and setup CloudWatch to email us that messages are in error state.
                if (maxReceiveCount <= receiveCount) {
                    log.info(String.format("Message exceeded Max Receive Count of %s: %s", maxReceiveCount, message.getMessageId()));
                    deleteMessageFromQueue(message);
                }
            } catch (NumberFormatException e) {

            }
        }
    }

    private void convertIntegersToLongs(EmailModel emailModel) {
        Map<String, Object> params = emailModel.getParams();
        for (String s : params.keySet()) {
            Object value = params.get(s);
            if(value instanceof Integer) {
                params.put(s, Long.valueOf((Integer)value));
            } else if (value instanceof Collection) {
                List<Long> longList = new ArrayList<Long>();
                for (Object elem : (Collection) value) {
                    if (elem instanceof Integer) {
                        longList.add(Long.valueOf((Integer) elem));
                    }
                }
                if (!CollectionUtils.isEmpty(longList)) {
                    params.put(s, longList);
                }
            }
        }
    }
}
