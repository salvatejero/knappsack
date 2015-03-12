package com.sparc.knappsack.components.services;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.google.common.collect.Lists;
import com.googlecode.flyway.core.util.StringUtils;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.ResignErrorType;
import com.sparc.knappsack.models.DomainUserRequestModel;
import com.sparc.knappsack.models.EmailModel;
import com.sparc.knappsack.models.UserModel;
import com.sparc.knappsack.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

public class SQSEmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SQSEmailServiceImpl.class);

    private AmazonSQSClient sqsClient;
    private String queueUrl;
    private String awsAccessKey;
    private String awsSecretKey;
    private String sqsQueueName;

    @Override
    public boolean sendDomainUserAccessRequestEmail(Long domainUserRequestId) {
        boolean success = false;
        if (domainUserRequestId != null) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.DOMAIN_USER_ACCESS_REQUEST);
            model.getParams().put("domainUserRequestId", domainUserRequestId);

            success = sendMessageToQueue(model);
        }
        return success;
    }

    @Override
    public boolean sendActivationEmail(Long userId) {
        boolean success = false;
        if (userId != null) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.USER_ACCOUNT_ACTIVATION);
            model.getParams().put("userId", userId);

            success = sendMessageToQueue(model);
        }
        return success;
    }

    @Override
    public boolean sendActivationSuccessEmail(Long userId) {
        boolean success = false;
        if (userId != null) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.USER_ACCOUNT_ACTIVATION_SUCCESS);
            model.getParams().put("userId", userId);

            success = sendMessageToQueue(model);
        }
        return success;
    }

    @Override
    public List<Long> sendInvitationsEmail(Long fromUserId, List<Long> invitationIds) {
        List<Long> invitationsSent = new ArrayList<Long>();
        if (!CollectionUtils.isEmpty(invitationIds)) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.USER_INVITE);
            model.getParams().put("userId", (fromUserId != null ? fromUserId : 0));
            model.getParams().put("invitationIds", invitationIds);

            if (sendMessageToQueue(model)) {
                invitationsSent.addAll(invitationIds);
            }
        }
        return invitationsSent;
    }

    @Override
    public boolean sendPasswordResetEmail(Long userId, String password) {
        boolean success = false;
        if (userId != null && StringUtils.hasText(password)) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.USER_PASSWORD_RESET);
            model.getParams().put("userId", userId);
            model.getParams().put("password", password);

            success = sendMessageToQueue(model);
        }
        return success;
    }

    @Override
    public boolean sendApplicationPublishRequestEmail(Long applicationVersionId, UserModel userModel) {
        boolean success = false;
        if (applicationVersionId != null && userModel != null) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.APPLICATION_VERSION_ORGANIZATION_PUBLISH_REQUEST);
            model.getParams().put("applicationVersionId", applicationVersionId);
            model.getParams().put("userModel", userModel);

            success = sendMessageToQueue(model);
        }
        return success;
    }

    @Override
    public boolean sendDomainUserAccessConfirmationEmail(DomainUserRequestModel domainUserRequestModel) {
        boolean success = false;
        if (domainUserRequestModel != null && domainUserRequestModel.getUser() != null && domainUserRequestModel.getUser().getId() > 0) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.DOMAIN_USER_ACCESS_REQUEST_CONFIRMATION);
            model.getParams().put("domainUserRequestModel", domainUserRequestModel);

            success = sendMessageToQueue(model);
        }
        return success;
    }

    @Override
    public boolean sendOrganizationRegistrationEmail(Long organizationId, UserModel userModel) {
        boolean success = false;
        if (organizationId != null && userModel != null) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.ORGANIZATION_REGISTRATION);
            model.getParams().put("organizationId", organizationId);
            model.getParams().put("userModel", userModel);

            success = sendMessageToQueue(model);
        }
        return success;
    }

    @Override
    public boolean sendApplicationVersionBecameVisibleEmail(Long applicationVersionId, List<Long> userIds) {
        boolean success = false;
        if (applicationVersionId != null && userIds != null) {
            List<EmailModel> emailModels = new ArrayList<EmailModel>();
            for (Long userId : userIds) {
                EmailModel model = new EmailModel();
                model.setEventType(EventType.APPLICATION_VERSION_STATE_CHANGED);
                model.getParams().put("applicationVersionId", applicationVersionId);
                model.getParams().put("userId", userId);

                emailModels.add(model);
            }

            int numSent = sendBatchMessagesToQueue(emailModels);
            if (numSent == emailModels.size()) {
                success = true;
            }
        }

        return success;
    }

    @Override
    public boolean sendBandwidthLimitNotification(Long organizationId, List<UserModel> users) {
        boolean success = false;
        if(organizationId != null && users != null) {
            List<EmailModel> emailModels = new ArrayList<EmailModel>();
            for (UserModel user : users) {
                EmailModel model = new EmailModel();
                model.setEventType(EventType.BANDWIDTH_LIMIT_REACHED);
                model.getParams().put("organizationId", organizationId);
                model.getParams().put("userModel", user);
                emailModels.add(model);
            }
            int numSent = sendBatchMessagesToQueue(emailModels);
            if (numSent == emailModels.size()) {
                success = true;
            }
        }

        return success;
    }

    @Override
    public boolean sendApplicationVersionErrorEmail(Long applicationVersionId, List<Long> users) {
        boolean success = false;
        if (applicationVersionId != null && users != null) {
            List<EmailModel> emailModels = new ArrayList<EmailModel>();
            for (Long user : users) {
                EmailModel model = new EmailModel();
                model.setEventType(EventType.APPLICATION_VERSION_STATE_CHANGED);
                model.getParams().put("applicationVersionId", applicationVersionId);
                model.getParams().put("userId", user);

                emailModels.add(model);
            }

            int numSent = sendBatchMessagesToQueue(emailModels);
            if (numSent == emailModels.size()) {
                success = true;
            }
        }

        return success;
    }

    @Override
    public boolean sendApplicationVersionResignCompleteEmail(Long applicationVersionId, boolean resignSuccess, ResignErrorType resignErrorType, List<Long> users) {
        boolean success = false;
        if (applicationVersionId != null && users != null) {
            List<EmailModel> emailModels = new ArrayList<EmailModel>();
            for (Long user : users) {
                EmailModel model = new EmailModel();
                model.setEventType(EventType.APPLICATION_VERSION_RESIGN_COMPLETE);
                model.getParams().put("applicationVersionId", applicationVersionId);
                model.getParams().put("resignSuccess", resignSuccess);
                model.getParams().put("resignErrorType", resignErrorType);
                model.getParams().put("userId", user);

                emailModels.add(model);
            }

            int numSent = sendBatchMessagesToQueue(emailModels);
            if (numSent == emailModels.size()) {
                success = true;
            }
        }

        return success;
    }

    @Override
    public boolean sendDomainAccessRequestEmail(Long domainRequestId) {
        boolean success = false;
        if (domainRequestId != null) {
            EmailModel model = new EmailModel();
            model.setEventType(EventType.DOMAIN_ACCESS_REQUEST);
            model.getParams().put("domainRequestId", domainRequestId);

            success = sendMessageToQueue(model);
        }
        return success;
    }

    private boolean sendMessageToQueue(EmailModel model) {
        boolean success = false;
        if (model != null) {
            String json = JsonUtil.marshall(model);
            if (StringUtils.hasText(json)) {
                SendMessageRequest request = new SendMessageRequest(queueUrl, json);
                SendMessageResult result = null;
                try {
                    result = sqsClient.sendMessage(request);
                    if (result != null) {
                        success = true;
                    }
                } catch (AmazonClientException e) {
                    log.error("Error sending email message to SQS: ", e);
                }
            }
        }
        return success;
    }

    /**
     *
     * @param models
     * @return The number of messages successfully sent.
     */
    private int sendBatchMessagesToQueue(List<EmailModel> models) {
        int messagesSent = 0;
        if (models != null) {
            List<List<EmailModel>> modelLists = Lists.partition(models, 10);
            for (List<EmailModel> list : modelLists) {

                List<SendMessageBatchRequestEntry> entries = new ArrayList<SendMessageBatchRequestEntry>();

                for (EmailModel model : list) {
                    String json = JsonUtil.marshall(model);
                    if (StringUtils.hasText(json)) {
                        SendMessageBatchRequestEntry entry = new SendMessageBatchRequestEntry(String.valueOf(models.indexOf(model)), json);
                        entries.add(entry);
                    }
                }

                if (!entries.isEmpty()) {
                    SendMessageBatchRequest request = new SendMessageBatchRequest(queueUrl, entries);

                    SendMessageBatchResult result = null;
                    try {
                        result = sqsClient.sendMessageBatch(request);
                        messagesSent += result.getSuccessful().size();
                    } catch (AmazonClientException e) {
                        log.error("Error sending batch email messages to SQS: ", e);
                    }
                }
            }
        }
        return messagesSent;
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

    @PostConstruct
    private void init() {
        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        this.sqsClient = new AmazonSQSClient(credentials);

        GetQueueUrlRequest request = new GetQueueUrlRequest(sqsQueueName);
        this.queueUrl = sqsClient.getQueueUrl(request).getQueueUrl();
    }
}
