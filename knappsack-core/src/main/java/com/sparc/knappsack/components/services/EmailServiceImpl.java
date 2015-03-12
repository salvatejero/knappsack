package com.sparc.knappsack.components.services;

import com.sparc.knappsack.comparators.InvitationDomainNameComparator;
import com.sparc.knappsack.comparators.LanguageComparator;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import com.sparc.knappsack.models.DomainUserRequestModel;
import com.sparc.knappsack.models.UserModel;
import com.sparc.knappsack.util.WebRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static com.sparc.knappsack.properties.SystemProperties.NOTIFICATION_EMAIL;

@Service("emailService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class EmailServiceImpl implements EmailService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("mailSender")
    @Autowired(required = true)
    private JavaMailSender mailSender;

    @Qualifier("templateEngine")
    @Autowired(required = true)
    private TemplateEngine templateEngine;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("domainRequestService")
    @Autowired(required = true)
    private DomainRequestService domainRequestService;

    @Value("${mail.send}")
    private boolean sendEmail;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Autowired(required = true)
    private DomainUserRequestService domainUserRequestService;

    @Qualifier("domainEntityServiceFactory")
    @Autowired(required = true)
    private DomainEntityServiceFactory domainEntityServiceFactory;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Value("${" + NOTIFICATION_EMAIL + "}")
    private String fromAddress;

    @Override
    public boolean sendDomainUserAccessRequestEmail(final Long domainUserRequestId) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (request != null && sendEmail) {

            DomainUserRequest domainUserRequest = domainUserRequestService.get(domainUserRequestId);
            if(domainUserRequest == null) {
                log.error("No DomainUserRequest found with ID: " + domainUserRequestId);
                return true;
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Domain Access Request");

                Locale locale = LocaleContextHolder.getLocale();

                message.setFrom(fromAddress);

                if (domainUserRequest != null) {
                    DomainEntityService domainEntityService = domainEntityServiceFactory.getDomainEntityService(domainUserRequest.getDomain().getDomainType());
                    Set<User> users = domainEntityService.getDomainRequestUsersForNotification(domainUserRequest.getDomain());

                    for (User user : users) {
                        Context ctx = new Context(locale);
                        ctx.setVariable("name", user.getFullName());
                        ctx.setVariable("request", domainUserRequest);

                        ctx.setVariable("url", request.generateURL("/manager"));
                        if(domainUserRequest.getDomain().getDomainType().equals(DomainType.ORGANIZATION)) {
                            applyBranding((Organization)domainUserRequest.getDomain(), ctx);
                        }

                        // Create the HTML body using Thymeleaf
                        final String htmlContent = this.templateEngine.process("email-domainUserAccessRequestTH", ctx);
                        message.setText(htmlContent, true /* isHtml */);
                        message.setTo(user.getEmail());

                        emailSent = sendMessage(mimeMessage);
                    }
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending a group access request for: %s", domainUserRequest.getUser().getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendActivationEmail(Long userId) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (request != null && sendEmail) {

            User user = userService.get(userId);
            if(user == null) {
                log.error("No User found with ID: " + userId);
                return true;
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {

                String organizationName = "";
                List<Organization> organizations = userService.getOrganizations(user, null);
                String subject = "Knappsack: Account Activation";
                Organization organization = null;
                if(organizations.size() == 1) {
                    organization = organizations.get(0);
                    organizationName = organizations.get(0).getName();
                    subject =  organizationName + ": Account Activation - Knappsack";
                }
                message.setSubject(subject);

                Locale locale = LocaleContextHolder.getLocale();

                message.setFrom(fromAddress);

                if (user != null && !user.isActivated()) {
                    Context ctx = new Context(locale);
                    ctx.setVariable("name", user.getFullName());
                    ctx.setVariable("activationCode", user.getActivationCode());
                    ctx.setVariable("organizationName", organizationName);

                    String servletPath = "/activate/" + user.getActivationCode();
                    ctx.setVariable("url", request.generateURL(servletPath));
                    applyBranding(organization, ctx);

                    // Create the HTML body using Thymeleaf
                    final String htmlContent = this.templateEngine.process("email-accountActivationTH", ctx);
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(user.getEmail());

                    emailSent = sendMessage(mimeMessage);
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending an activation for: %s", user.getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendActivationSuccessEmail(Long userId) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (request != null && sendEmail) {

            User user = userService.get(userId);
            if(user == null) {
                log.error("No User found with ID: " + userId);
                return true;
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                String organizationName = "";
                List<Organization> organizations = userService.getOrganizations(user, null);
                String subject = "Knappsack: Account Activation Success";
                Organization organization = null;
                if(organizations.size() == 1) {
                    organization = organizations.get(0);
                    organizationName = organizations.get(0).getName();
                    subject =  organizationName + ": Account Activation Success - Knappsack";
                }
                message.setSubject(subject);

                Locale locale = LocaleContextHolder.getLocale();

                message.setFrom(fromAddress);

                if (user != null && user.isActivated()) {
                    Context ctx = new Context(locale);
                    ctx.setVariable("name", user.getFullName());
                    ctx.setVariable("organizationName", organizationName);
                    String servletPath = "/";
                    ctx.setVariable("url", request.generateURL(servletPath));
                    applyBranding(organization, ctx);

                    final String htmlContent = this.templateEngine.process("email-accountActivationSuccessTH", ctx);
                    message.setText(htmlContent, true);
                    message.setTo(user.getEmail());

                    emailSent = sendMessage(mimeMessage);
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending an activation for: %s", user.getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public List<Long> sendInvitationsEmail(Long fromUserId, List<Long> invitationIds) {
        Set<Long> invitationsSent = new HashSet<Long>();
        WebRequest request = WebRequest.getInstance();

        // Get all Invitations for supplied IDs
        Set<Invitation> invitations = new HashSet<Invitation>();
        if (invitationIds != null) {
            for (Long invitationId : invitationIds) {
                Invitation invitation = invitationService.get(invitationId);
                if (invitation != null) {
                    invitations.add(invitation);
                }
            }
        }

        if (request != null && sendEmail && !CollectionUtils.isEmpty(invitations)) {

            User fromUser = null;
            if (fromUserId != null) {
                fromUser = userService.get(fromUserId);
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");

            Locale locale = LocaleContextHolder.getLocale();

            Context ctx = new Context(locale);
            if (fromUser != null) {
                ctx.setVariable("adminName", fromUser.getFullName());
            }

            // Convert Invitation List to Map with email addresses being keys
            Map<String, List<Invitation>> emailMap = new HashMap<String, List<Invitation>>();
            for (Invitation invitation : invitations) {
                String key = StringUtils.trimAllWhitespace(invitation.getEmail()).toLowerCase();
                if (emailMap.get(key) == null) {
                    emailMap.put(key, new ArrayList<Invitation>());
                }
                emailMap.get(key).add(invitation);
            }

            Organization parentDomain = null;

            // Loop over each email address and send out a single invitation for each
            for (String emailAddress : emailMap.keySet()) {

                // Create Map separating domains for the email addresses invitations
                Map<String, Set<Invitation>> invitationsMap = new HashMap<String, Set<Invitation>>();
                for (Invitation invitation : emailMap.get(emailAddress)) {
                    if (DomainType.ORGANIZATION.equals(invitation.getDomain().getDomainType())) {
                        if (invitationsMap.get(DomainType.ORGANIZATION.name()) == null) {
                            invitationsMap.put(DomainType.ORGANIZATION.name(), new TreeSet<Invitation>(new InvitationDomainNameComparator()));
                        }
                        invitationsMap.get(DomainType.ORGANIZATION.name()).add(invitation);
                        parentDomain = (Organization) invitation.getDomain();
                    } else if (DomainType.GROUP.equals(invitation.getDomain().getDomainType())) {
                        if (invitationsMap.get(DomainType.GROUP.name()) == null) {
                            invitationsMap.put(DomainType.GROUP.name(), new TreeSet<Invitation>(new InvitationDomainNameComparator()));
                        }
                        invitationsMap.get(DomainType.GROUP.name()).add(invitation);
                        if(parentDomain == null) {
                            Group group = (Group) invitation.getDomain();
                            parentDomain = group.getOrganization();
                        }
                    }
                }

                ctx.setVariable("domainTypes", invitationsMap.keySet());
                ctx.setVariable("groupInvitations", invitationsMap.get(DomainType.GROUP.name()));
                String parentDomainName = parentDomain != null ? parentDomain.getName() : "";
                ctx.setVariable("parentDomainName", StringUtils.trimTrailingWhitespace(parentDomainName));

                User invitee = userService.getByEmail(emailAddress);
                if (invitee == null) {
                    ctx.setVariable("existingUser", false);
                    NameValuePair emailParam = new BasicNameValuePair("email", emailAddress);
                    ctx.setVariable("url", request.generateURL("/auth/register", emailParam));
                } else {
                    ctx.setVariable("existingUser", true);
                    ctx.setVariable("url", request.generateURL(""));
                }
                applyBranding(parentDomain, ctx);

                // Create the HTML body using Thymeleaf
                final String htmlContent = this.templateEngine.process("email-invitationTH", ctx);
                try {
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(emailAddress);
                    message.setSubject(String.format("%s: Invitation to Knappsack", StringUtils.trimTrailingWhitespace(parentDomain.getName())));
                    message.setFrom(fromAddress);

                    if (sendMessage(mimeMessage)) {
                        for (Invitation invitation : emailMap.get(emailAddress)) {
                            invitationsSent.add(invitation.getId());
                        }
                    }
                } catch (MessagingException e) {
                    log.error(String.format("MessagingException sending an invitation for: %s", emailAddress), e);
                }
            }
        }

        return new ArrayList<Long>(invitationsSent);
    }

    @Override
    public boolean sendPasswordResetEmail(final Long userId, final String password) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (request != null && sendEmail) {

            User user = userService.get(userId);
            if(user == null) {
                log.error("No User found with ID: " + userId);
                return true;
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Password Changed");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                if (user != null) {
                    Context ctx = new Context(locale);
                    ctx.setVariable("name", user.getFullName());
                    ctx.setVariable("password", password);
                    ctx.setVariable("url", request.generateURL(""));

                    // Create the HTML body using Thymeleaf
                    final String htmlContent = this.templateEngine.process("email-passwordChangedTH", ctx);
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(user.getEmail());

                    emailSent = sendMessage(mimeMessage);
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending a password changed for: %s", user.getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendApplicationPublishRequestEmail(Long applicationVersionId, UserModel userModel) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (sendEmail && userModel != null && applicationVersionId != null && request != null) {

            ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);
            if(applicationVersion == null) {
                log.error("No ApplicationVersion with ID: " + applicationVersionId);
                return true;
            }
            Application application = applicationVersion.getApplication();
            if(application == null) {
                log.error("No application found for ApplicationVersion with ID: " + applicationVersionId);
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Application Publish Request");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                Organization organization = null;
                try {
                    organization = applicationVersion.getApplication().getCategory().getOrganization();
                } catch (NullPointerException ex) {
                    log.error("NPE caused getting organization from application version", ex);
                }

                if (organization != null) {
                    List<UserDomain> userdomains = userDomainService.getAll(organization.getId(), UserRole.ROLE_ORG_ADMIN);

                    for (UserDomain userDomain : userdomains) {
                        Context ctx = new Context(locale);
                        ctx.setVariable("name", userDomain.getUser().getFullName());
                        ctx.setVariable("userName", userModel.getFullName());
                        ctx.setVariable("applicationName", applicationVersion.getApplication().getName());
                        ctx.setVariable("applicationVersion", applicationVersion.getVersionName());
                        ctx.setVariable("organizationName", organization.getName());
                        ctx.setVariable("url", request.generateURL("/manager"));

                        // Create the HTML body using Thymeleaf
                        final String htmlContent = this.templateEngine.process("email-applicationPublishRequestTH", ctx);
                        applyBranding(organization, ctx);
                        message.setText(htmlContent, true /* isHtml */);
                        message.setTo(userDomain.getUser().getEmail());

                        emailSent = sendMessage(mimeMessage);
                    }
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending an application publish request for: %s", userModel.getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendDomainUserAccessConfirmationEmail(DomainUserRequestModel domainUserRequestModel) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (sendEmail && domainUserRequestModel != null && domainUserRequestModel.getDomain() != null && domainUserRequestModel.getUser() != null && request != null) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject(String.format("Knappsack: Domain Access Request %s", (Status.ACCEPTED.equals(domainUserRequestModel.getStatus()) ? "Accepted" : "Declined")));

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                User user = userService.get(domainUserRequestModel.getUser().getId());
                if (user != null) {
                    Context ctx = new Context(locale);
                    ctx.setVariable("name", user.getFullName());
                    ctx.setVariable("groupName", domainUserRequestModel.getDomain().getName());
                    ctx.setVariable("status", (Status.ACCEPTED.equals(domainUserRequestModel.getStatus()) ? true : false));
                    // Create the HTML body using Thymeleaf
                    final String htmlContent = this.templateEngine.process("email-domainUserAccessConfirmationTH", ctx);
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(user.getEmail());

                    emailSent = sendMessage(mimeMessage);
                }
            } catch (MessagingException e) {
                log.error("MessagingException sending a group access confirmation:", e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendOrganizationRegistrationEmail(Long organizationId, UserModel userModel) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (sendEmail && organizationId != null && userModel != null && StringUtils.hasText(userModel.getEmail()) && request != null) {
            Organization organization = organizationService.get(organizationId);
            if(organization == null) {
                log.error("No organization found with ID: " + organizationId);
                return true;
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject(String.format("Knappsack: %s Registration", organization.getName().trim()));

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                Context ctx = new Context(locale);
                ctx.setVariable("organization", organization);

                boolean existingUser = false;
                if (userService.getByEmail(userModel.getEmail().trim()) != null) {
                    existingUser = true;
                }
                ctx.setVariable("existingUser", existingUser);
                ctx.setVariable("user", userModel);
                ctx.setVariable("url", request.generateURL(""));
                applyBranding(organization, ctx);

                // Create the HTML body using Thymeleaf
                final String htmlContent = this.templateEngine.process("email-organizationRegistrationTH", ctx);
                message.setText(htmlContent, true /* isHtml */);
                message.setTo(userModel.getEmail().trim());

                emailSent = sendMessage(mimeMessage);
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending a organization registration for: %s", organization.getName()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendApplicationVersionBecameVisibleEmail(Long applicationVersionId, List<Long> userIds) {
        boolean emailsSent = false;
        WebRequest request = WebRequest.getInstance();
        if (sendEmail && request != null && applicationVersionId != null && userIds != null) {
            ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);
            if(applicationVersion == null) {
                log.error("No ApplicationVersion with ID: " + applicationVersionId);
                return true;
            }
            Application application = applicationVersion.getApplication();
            if(application == null) {
                log.error("No application found for ApplicationVersion with ID: " + applicationVersionId);
            }

            int numSent = 0;
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Application Visibility");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                Context ctx = new Context(locale);
                ctx.setVariable("applicationName", applicationVersion.getApplication().getName());
                ctx.setVariable("url", request.generateURL(String.format("/detail/%s", applicationVersion.getApplication().getId())));
                ctx.setVariable("applicationVersion", applicationVersion.getVersionName());
                applyBranding(application.getOwnedGroup().getOrganization(), ctx);

                List<User> users = userService.get(userIds);
                for (User user : users) {
                    ctx.setVariable("userName", user.getFullName());

                    final String htmlContent = this.templateEngine.process("email-applicationVersionBecameVisibleTH", ctx);
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(user.getEmail());

                    if (sendMessage(mimeMessage)) {
                        numSent += 1;
                    }
                }

                if (numSent == users.size()) {
                    emailsSent = true;
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending ApplicationVersionBecameVisible emails for applicationVersionId: %s", applicationVersion.getId()), e);
            }
        }

        return emailsSent;
    }

    @Override
    public boolean sendBandwidthLimitNotification(Long organizationId, List<UserModel> users) {
        boolean emailsSent = false;
        WebRequest webRequest = WebRequest.getInstance();
        if (sendEmail) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                Organization organization = organizationService.get(organizationId);
                if(organization == null) {
                    log.error("No organization found with ID: " + organizationId);
                    return true;
                }

                int numberOfEmails = 0;
                message.setSubject("Knappsack: Organization Bandwidth Limit Reached");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                if (users != null) {
                    String orgName = organization.getName();
                    String url = webRequest.generateURL("");
                    long bandwidthLimit = organization.getDomainConfiguration().getMegabyteBandwidthLimit();
                    for (UserModel user : users) {
                        Context ctx = new Context(locale);
                        ctx.setVariable("user", user);
                        ctx.setVariable("organizationName", orgName);
                        ctx.setVariable("url", url);
                        ctx.setVariable("bandwidthLimit", bandwidthLimit);

                        // Create the HTML body using Thymeleaf
                        final String htmlContent = this.templateEngine.process("email-bandwidthLimitTH", ctx);
                        message.setText(htmlContent, true /* isHtml */);
                        message.setTo(user.getEmail());

                        if (sendMessage(mimeMessage)) {
                            numberOfEmails += 1;
                        }
                    }

                    if (numberOfEmails == users.size()) {
                        emailsSent = true;
                    }
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending notifications to admins for Organization: %s", organizationId), e);
            }
        }


        return emailsSent;
    }

    @Override
    public boolean sendApplicationVersionErrorEmail(Long applicationVersionId, List<Long> userIds) {
        boolean emailsSent = false;
        WebRequest webRequest = WebRequest.getInstance();

        ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);
        if(applicationVersion == null) {
            log.error("No ApplicationVersion with ID: " + applicationVersionId);
            return true;
        }

        if (sendEmail && applicationVersion.getApplication() != null && AppState.ERROR.equals(applicationVersion.getAppState()) && userIds != null) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                int numberOfEmails = 0;
                message.setSubject("Knappsack: Application Version Error");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                Application parentApplication = applicationVersion.getApplication();
                if (parentApplication != null) {
                    String applicationName = parentApplication.getName();
                    String url = webRequest.generateURL(String.format("/manager/editVersion/%s/%s", parentApplication.getId(), applicationVersion.getId()));
                    List<User> users = userService.get(userIds);
                    for (User user : users) {
                        Context ctx = new Context(locale);
                        ctx.setVariable("user", user);
                        ctx.setVariable("url", url);
                        ctx.setVariable("applicationName", applicationName);
                        ctx.setVariable("applicationVersion", applicationVersion.getVersionName());
                        applyBranding(parentApplication.getOwnedGroup().getOrganization(), ctx);

                        // Create the HTML body using Thymeleaf
                        final String htmlContent = this.templateEngine.process("email-applicationVersionErrorTH", ctx);
                        message.setText(htmlContent, true /* isHtml */);
                        message.setTo(user.getEmail());

                        if (sendMessage(mimeMessage)) {
                            numberOfEmails += 1;
                        }
                    }
                    if (numberOfEmails == users.size()) {
                        emailsSent = true;
                    }
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending notifications to admins for Application Version: %s", applicationVersion.getId()), e);
            }
        }

        return emailsSent;
    }

    @Override
    public boolean sendApplicationVersionResignCompleteEmail(Long applicationVersionId, boolean resignSuccess, ResignErrorType resignErrorType, List<Long> userIds) {
        boolean emailsSent = false;
        WebRequest webRequest = WebRequest.getInstance();
        if (sendEmail && applicationVersionId != null && userIds != null) {

            ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);
            if(applicationVersion == null) {
                log.error("No ApplicationVersion with ID: " + applicationVersionId);
                return true;
            }
            Application application = applicationVersion.getApplication();
            if(application == null) {
                log.error("No application found for ApplicationVersion with ID: " + applicationVersionId);
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                int numberOfEmails = 0;
                if (resignSuccess) {
                    message.setSubject("Knappsack: Application Resign Complete");
                } else {
                    message.setSubject("Knappsack: Application Resign Error");
                }

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                Application parentApplication = applicationVersion.getApplication();
                if (parentApplication != null) {
                    String applicationName = parentApplication.getName();
                    String url;
                    if (resignSuccess) {
                        url = webRequest.generateURL(String.format("/detail/%s", parentApplication.getId()));
                    } else {
                        url = webRequest.generateURL(String.format("/manager/editVersion/%s/%s", parentApplication.getId(), applicationVersion.getId()));
                    }

                    List<User> users = userService.get(userIds);
                    for (User user : users) {
                        Context ctx = new Context(locale);
                        ctx.setVariable("userName", user.getFullName());
                        ctx.setVariable("url", url);
                        ctx.setVariable("resignSuccess", resignSuccess);
                        ctx.setVariable("resignErrorType", (resignErrorType != null ? resignErrorType : ResignErrorType.GENERIC));
                        ctx.setVariable("applicationName", applicationName);
                        ctx.setVariable("applicationVersion", applicationVersion.getVersionName());
                        applyBranding(parentApplication.getOwnedGroup().getOrganization(), ctx);

                        // Create the HTML body using Thymeleaf
                        final String htmlContent = this.templateEngine.process("email-applicationVersionResignCompleteTH", ctx);
                        message.setText(htmlContent, true /* isHtml */);
                        message.setTo(user.getEmail());

                        if (sendMessage(mimeMessage)) {
                            numberOfEmails += 1;
                        }
                    }
                    if (numberOfEmails == users.size()) {
                        emailsSent = true;
                    }
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending notifications to users for Application Resign Complete: %s", applicationVersion.getId()), e);
            }
        }

        return emailsSent;
    }

    @Override
    public boolean sendDomainAccessRequestEmail(Long domainRequestId) {
        boolean emailsSent = false;
        WebRequest webRequest = WebRequest.getInstance();
        if (sendEmail && webRequest != null && domainRequestId != null) {

            DomainRequest domainRequest = domainRequestService.get(domainRequestId);
            if(domainRequest == null) {
                log.error("No DomainUserRequest found with ID: " + domainRequestId);
                return true;
            }

            if(domainRequest.getDomain() == null) {
                log.error("No Domain found for DomainRequest with ID: " + domainRequestId);
                return true;
            }

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");

            try {
                int numberOfEmails = 0;
                message.setSubject("Knappsack: Domain Request");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                int emailCount = 0;

                Region region = domainRequest.getRegion();
                if (region != null && region.getEmails() != null && region.getEmails().size() > 0) {
                    emailCount = region.getEmails().size();
                    for (String email : region.getEmails()) {
                        Context ctx = new Context(locale);
                        ctx.setVariable("url", webRequest.generateURL("/manager/requestsPending/" + domainRequest.getDomain().getId()));
                        ctx.setVariable("domain", domainRequest.getDomain());
                        ctx.setVariable("requesterFirstName", domainRequest.getFirstName());
                        ctx.setVariable("requesterLastName", domainRequest.getLastName());
                        ctx.setVariable("requesterEmail", domainRequest.getEmailAddress());
                        ctx.setVariable("requesterAddress", domainRequest.getAddress());
                        ctx.setVariable("requesterCompanyName", domainRequest.getCompanyName());
                        ctx.setVariable("requesterPhone", domainRequest.getPhoneNumber());
                        ctx.setVariable("requesterDeviceType", domainRequest.getDeviceType());
                        ctx.setVariable("requesterRegion", domainRequest.getRegion());

                        List<Language> languages = new ArrayList<Language>();
                        languages.addAll(domainRequest.getLanguages());
                        Collections.sort(languages, new LanguageComparator());
                        ctx.setVariable("requesterLanguages", languages);
                        applyBranding(domainRequest.getDomain(), ctx);

                        // Create the HTML body using Thymeleaf
                        final String htmlContent = this.templateEngine.process("email-domainAccessRequestTH", ctx);
                        message.setText(htmlContent, true /* isHtml */);
                        message.setTo(email);

                        if (sendMessage(mimeMessage)) {
                            numberOfEmails += 1;
                        }
                    }
                } else {
                    List<User> users = domainService.getAllAdmins(domainRequest.getDomain(), true);
                    emailCount = (users == null ? 0 : users.size());

                    if (users != null) {
                        for (User user : users) {
                            Context ctx = new Context(locale);
                            ctx.setVariable("url", webRequest.generateURL("/manager/requestsPending/" + domainRequest.getDomain().getId()));
                            ctx.setVariable("domain", domainRequest.getDomain());
                            ctx.setVariable("requesterFirstName", domainRequest.getFirstName());
                            ctx.setVariable("requesterLastName", domainRequest.getLastName());
                            ctx.setVariable("requesterEmail", domainRequest.getEmailAddress());
                            ctx.setVariable("requesterAddress", domainRequest.getAddress());
                            ctx.setVariable("requesterCompanyName", domainRequest.getCompanyName());
                            ctx.setVariable("requesterPhone", domainRequest.getPhoneNumber());
                            ctx.setVariable("requesterDeviceType", domainRequest.getDeviceType());
                            ctx.setVariable("requesterRegion", domainRequest.getRegion());

                            List<Language> languages = new ArrayList<Language>();
                            languages.addAll(domainRequest.getLanguages());
                            Collections.sort(languages, new LanguageComparator());
                            ctx.setVariable("requesterLanguages", languages);

                            ctx.setVariable("user", user);

                            // Create the HTML body using Thymeleaf
                            final String htmlContent = this.templateEngine.process("email-domainAccessRequestTH", ctx);
                            message.setText(htmlContent, true /* isHtml */);
                            message.setTo(user.getEmail());

                            if (sendMessage(mimeMessage)) {
                                numberOfEmails += 1;
                            }
                        }
                    }

                }

                if (numberOfEmails == emailCount) {
                    emailsSent = true;
                }
            } catch (MessagingException e) {
                log.error(String.format("Messaging Exception sending notifications to users for DomainRequest: %s", domainRequest.getId()), e);
            }
        }

        return emailsSent;
    }

    private boolean sendMessage(MimeMessage message) {
        boolean success = false;
        if (message != null) {
            try {
                this.mailSender.send(message);
                success = true;
            } catch (MailSendException mailSendException) {
                try {
                    for (Address address : message.getRecipients(Message.RecipientType.TO)) {
                        log.error("Error sending email to " + address + ".", mailSendException);
                    }
                } catch (MessagingException messagingException) {
                    log.error("Error logging message recipients.", messagingException);
                }

                throw mailSendException;
            } catch (MailException mailException) {
                log.error("Error sending email.", mailException);
            }
        }
        return success;
    }

    private void applyBranding(Domain domain, Context context) {
        if(domain.getDomainType().equals(DomainType.ORGANIZATION)) {
            applyBranding((Organization)domain, context);
        }
    }

    private void applyBranding(Organization organization, Context context) {
        if(organization == null) {
            return;
        }

        if(organizationService.isCustomBrandingEnabled(organization)) {
            CustomBranding branding = organization.getCustomBranding();
            if(branding.getEmailHeader() != null && !branding.getEmailHeader().isEmpty()) {
                context.setVariable("emailHeader", branding.getEmailHeader());
            }
            if(branding.getEmailFooter() != null && !branding.getEmailFooter().isEmpty()) {
                context.setVariable("emailFooter", branding.getEmailFooter());
            }
            AppFile logo = organization.getCustomBranding().getLogo();
            if (logo != null) {
                context.setVariable("customLogoURL", appFileService.getImageUrl(logo));
                context.setVariable("customLogoOrganizationName", StringUtils.trimTrailingWhitespace(organization.getName()));
            }
        } else {
            context.setVariable("customLogoURL", WebRequest.getInstance().generateURL("/static/resources/img/logo.png"));
        }
    }
}
