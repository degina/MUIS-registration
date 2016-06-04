package controllers;

import controllers.EMailClass.*;
import models.Email;
import models.EmailAddress;
import models.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.cache.Cache;
import play.mvc.With;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.*;

/**
 * Created by enkhamgalan on 3/22/15.
 */
@With(Secure.class)
public class Emails extends CRUD {
    public static void close() {
        try {
            System.out.println("authenticate mail close");
            EMailDAO emailDAO = Cache.get(session.get("mail_uuid"), EMailDAO.class);
            if (emailDAO != null) emailDAO.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean authenticate(User user) {
        Email email = user.emailAccount;
        if (email != null && email.username != null && email.password != null) {
            try {
                boolean relogin;
                if (!session.contains("mail_uuid")) relogin = true;
                else {
                    EMailDAO emailDAO = Cache.get(session.get("mail_uuid"), EMailDAO.class);
                    if (emailDAO == null) relogin = true;
                    else {
                        if (!emailDAO.isConnected())
                            emailDAO.connect();
                        return true;
                    }
                }
                if (relogin) {
                    System.out.println("relogin relogin relogin");
                    EMailDAO emailDAO = new EMailDAO(CompanyConf.mail_protocol, CompanyConf.mail_protocol + "." + CompanyConf.mail_domain, CompanyConf.mail_port, email.username, email.password);
                    try {
                        emailDAO.connect();
                        String uuid = CompanyConf.name + "_mail_" + user.id;
                        session.put("mail_uuid", uuid);
                        Cache.add(uuid, emailDAO);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        flash.put("error", "Incorrect login/password");
                        emailDAO.close();
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static EMailDAO getEMailDAO() {
        return Cache.get(session.get("mail_uuid"), EMailDAO.class);
    }

    public static void folders(String name) {
        boolean logged;
        int messageCount = 0;
        User user = Users.getUser();
        Email email = user.emailAccount;
        if (email == null) forbidden();
        logged = authenticate(user);
        if (logged) {
            EMailDAO emailDAO = getEMailDAO();
            try {
                Folder folder = emailDAO.openFolder(name, Folder.READ_ONLY);
                messageCount = folder.getMessageCount();
            } catch (MessagingException e) {
            }
        }
        render(name, logged, messageCount);
    }

    public static void folderGetMails(boolean first, int mid, int counter, String name) {
        User user = Users.getUser();
        Date dateS = new Date();
        System.out.println(mid + " mail: " + user.emailAccount.username);
        System.out.println("Sta: " + Consts.myDateFormat.format(dateS));
        EmailData mail = getMail(user, name, mid);

        Date dateF = new Date();
        long seconds = (dateF.getTime() - dateS.getTime()) / 1000;
        System.out.println("Total Sek: " + seconds + " == " + Consts.myDateFormat.format(dateF) + " => " + (mail != null));

        render(mail, name, mid, first, counter);
    }

    public static EmailData getMail(User user, String name, int mid) {
        boolean logged;
        Email email = user.emailAccount;
        if (email == null) forbidden();
        logged = authenticate(user);
        if (logged) {
            EMailDAO emailDAO = getEMailDAO();
            try {
                Folder folder = emailDAO.openFolder(name, Folder.READ_ONLY);
                if (0 == mid) return null;
                Message message = folder.getMessage(mid);
                return new EmailData(message, false);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void getContentData(String name, int mid) {
        EmailData emailData = null;
        User user = Users.getUser();
        if (authenticate(user)) {
            EMailDAO emailDAO = getEMailDAO();
            try {
                Folder folder = emailDAO.openFolder(name, Folder.READ_WRITE);
                Message message = folder.getMessage(mid);
                emailData = new EmailData(message, true);
                try {
                    EmailContentData contentData = emailDAO.getContent(null, message, user.id, false);
                    if (StringUtils.isEmpty(contentData.body))
                        contentData = emailDAO.getContent(null, message, "text/plain", user.id, false);
                    if (contentData.attach.length() > 0) emailData.isAttach = true;
                    contentData.emailData = emailData;
                    emailData.contentData = contentData;
                    if (!message.getFlags().contains(Flags.Flag.SEEN)) message.setFlag(Flags.Flag.SEEN, true);
                } catch (IOException e) {
                    System.out.println(e);
                }
            } catch (MessagingException e) {
                System.out.println(e);
            }
        }
        render(emailData, name, mid);
    }

    public static void getAttachment(String[] folderNames, int mid, String fileName, String extension, String filenameOrg) {
        User user = Users.getUser();
        boolean logged;
        Email email = user.emailAccount;
        if (email == null) forbidden();
        logged = authenticate(user);
        if (logged) {
            try {
                EMailDAO emailDAO = getEMailDAO();
                Message message = emailDAO.getMessage(folderNames, Folder.READ_ONLY, mid);
                InputStream is = emailDAO.getAttachInputStream(message, filenameOrg);
                String absolutePath = Functions.cleanUrl(Consts.uploadEMailPath + "/" + user.id + "/");

                File f = new File(Play.applicationPath.getAbsolutePath() + absolutePath);
                if (!f.exists()) {
                    if (!f.mkdirs()) throw new Exception("Unable to create upload dir");
                }
                FileUtils.cleanDirectory(f);

                absolutePath += fileName + "." + extension;

                FileOutputStream fos = new FileOutputStream(new File(Play.applicationPath.getAbsolutePath() + absolutePath));
                IOUtils.copy(is, fos);
//                byte[] buf = new byte[4096];
//                int bytesRead;
//                while ((bytesRead = is.read(buf)) != -1) {
//                    fos.write(buf, 0, bytesRead);
//                }
                fos.close();
                renderText(absolutePath);
            } catch (Exception e) {
                System.out.println("Emails.getAttachent(): " + e);
            }
        }
    }

    public static void composeNewMail(String success, String name, Long mid, String rtype) {
        User user = Users.getUser();
        Email email = user.emailAccount;
        if (email == null) forbidden();
        if (success != null) flash.success("Амжилттай илгээгдлээ");
        EmailData emailData = null;
        if (mid != null) { // Draft bol
            if (authenticate(user)) {
                EMailDAO emailDAO = getEMailDAO();
                try {
                    Folder folder = emailDAO.openFolder(name, Folder.READ_WRITE);
                    Message message = folder.getMessage(mid.intValue());
                    try {
                        emailData = new EmailData(message, true);
                        EmailContentData contentData = emailDAO.getContent(null, message, user.id, true);
                        if (StringUtils.isEmpty(contentData.body))
                            contentData = emailDAO.getContent(null, message, "text/plain", user.id, true);
                        if (contentData.attach.length() > 0) emailData.isAttach = true;
                        contentData.emailData = emailData;
                        emailData.contentData = contentData;
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                } catch (MessagingException e) {
                    System.out.println(e);
                }
            }
        }
        EmailDraft emailDraft = null;
        if (emailData != null) {
            emailDraft = new EmailDraft();
            int i = 1;
            if (rtype == null || rtype.length() == 0) {
                List<EmailRecipient> recipients = emailData.getRecipientsTo();
                for (EmailRecipient recipient : recipients) {
                    emailDraft.to += recipient.address;
                    if (i != recipients.size()) emailDraft.to += ",";
                    i++;
                }
                i = 1;
                recipients = emailData.getRecipientsCc();
                for (EmailRecipient recipient : recipients) {
                    emailDraft.cc += recipient.address;
                    if (i != recipients.size()) emailDraft.cc += ",";
                    i++;
                }
                i = 1;
                recipients = emailData.getRecipientsCc();
                for (EmailRecipient recipient : recipients) {
                    emailDraft.bcc += recipient.address;
                    if (i != recipients.size()) emailDraft.bcc += ",";
                    i++;
                }
                if (emailData.contentData != null) {
                    emailDraft.body = emailData.contentData.body;
                    emailDraft.attach = emailData.contentData.attach;
                }
                emailDraft.subject = emailData.subject;
            } else {

                if (rtype.equals("reply") || rtype.equals("replyAll")) {
                    emailDraft.subject = "Re: " + emailData.subject;
                    emailDraft.to = emailData.getRecipientsFrom().address;
                    if (emailData.contentData != null) {
                        emailDraft.body = "<br><div><blockquote type = 'cite'><div> On " + Consts.myDateFormat.format(emailData.dateReceived) + ", " + emailData.getRecipientsFrom().mail +
                                " &lt;" + emailData.getRecipientsFrom().address + "&gt; wrote:</div ><br class='Apple-interchange-newline'><div><p>" + emailData.contentData.body + " </p></div></blockquote></div>";
                        emailDraft.attach = emailData.contentData.attach;
                    }
                    if (rtype.equals("replyAll")) {
                        i = 1;
                        List<EmailRecipient> recipients = emailData.getRecipientsTo();
                        for (EmailRecipient recipient : recipients) {
                            if (!recipient.address.equals(email.username)) {
                                emailDraft.cc += recipient.address;
                                if (i != recipients.size()) emailDraft.cc += ",";
                                i++;
                            }
                        }
                        i = 1;
                        recipients = emailData.getRecipientsCc();
                        for (EmailRecipient recipient : recipients) {
                            if (!recipient.address.equals(email.username)) {
                                emailDraft.cc += recipient.address;
                                if (i != recipients.size()) emailDraft.cc += ",";
                            }
                            i++;
                        }
                    }
                } else if (rtype.equals("forward")) {
                    emailDraft.subject = "Fwd: " + emailData.subject;
                    if (emailData.contentData != null) {
                        String to = "", cc = "";
                        i = 1;
                        List<EmailRecipient> recipients = emailData.getRecipientsTo();
                        for (EmailRecipient recipient : recipients) {
                            to += recipient.address;
                            if (i != recipients.size()) to += ",";
                            i++;
                        }
                        i = 1;
                        recipients = emailData.getRecipientsCc();
                        for (EmailRecipient recipient : recipients) {
                            cc += recipient.address;
                            if (i != recipients.size()) cc += ",";
                            i++;
                        }
                        emailDraft.body = "<br><blockquote type='cite'><p> ---------- Original Message ----------<br>" +
                                "From: " + emailData.getRecipientsFrom().mail + " &lt;" + emailData.getRecipientsFrom().address + "&gt;<br>" +
                                "To: " + to + "<br>" + (cc.length() > 0 ? "Cc: " + cc + "<br>" : "") +
                                "Date: " + Consts.myDateFormat.format(emailData.dateReceived) + "<br>" +
                                "Subject: " + emailData.subject + "<br><br>" + emailData.contentData.body +
                                "</p></blockquote>";
                        emailDraft.attach = emailData.contentData.attach;
                    }
                }
            }
        }
        render(email, mid, name, emailDraft);
    }

    public static void sendEmail(String addressTo, String addressCc, String addressBcc, String subject, String mailcontent, String stype, String name, int mid, String[] attachments) {
        User user = Users.getUser();
        final Email email = user.emailAccount;
        if (email != null) {
            // Get a Properties object
            try {

                Properties props = System.getProperties();
                props.put("mail.smtp.host", "smtp." + CompanyConf.mail_domain);
                props.put("mail.smtp.auth", "true");
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email.username, email.password);
                    }
                });
                MimeMessage mail = new MimeMessage(session);
                mail.setFrom(new InternetAddress(email.fullname + "<" + email.username + ">"));
                // TODO: use a better structure
                if (addressTo != null && addressTo.length() > 2) {
//                    addressTo="Amgaa Bayern <enkhamgalan.ch@icloud.com>";
                    mail.setRecipients(Message.RecipientType.TO, InternetAddress.parse(addressTo, false));
                }
                if (addressCc != null && addressCc.length() > 2) {
                    mail.setRecipients(Message.RecipientType.CC, InternetAddress.parse(addressCc, false));
                }
                if (addressBcc != null && addressBcc.length() > 2) {
                    mail.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(addressBcc, false));
                }
                if (!StringUtils.isEmpty(subject)) mail.setSubject(subject);
                else mail.setSubject("(no subject)");
                if (attachments != null && attachments.length > 0) {
                    // Attach the specified file.
                    // We need a multipart message to hold the attachment.
                    MimeBodyPart mbp1 = new MimeBodyPart();
                    mbp1.setContent(mailcontent, "text/html; charset=utf-8");
                    MimeMultipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);

                    for (String s : attachments) {
                        MimeBodyPart mbp2 = new MimeBodyPart();
                        mbp2.attachFile(new File(Play.applicationPath.getAbsolutePath() + s));
                        mp.addBodyPart(mbp2);
                    }
                    mail.setContent(mp);
                } else {
                    mail.setContent(mailcontent, "text/html; charset=utf-8");
                }
                mail.setSentDate(new Date());
                String folderName = "Sent Items";
                if (stype.equals("send")) {
                    if (name != null && name.length() > 0 && mid > 0 && name.toLowerCase().startsWith("draft")) {
                        if (authenticate(user)) {
                            EMailDAO emailDAO = getEMailDAO();
                            Folder folder = emailDAO.openFolder(name, Folder.READ_WRITE);
                            Message message = folder.getMessage(mid);
                            message.setFlag(Flags.Flag.DELETED, true);
                            folder.expunge();
                        }
                    }
                    Map<String, EmailAddress> emailAddressHashMap = new HashMap<String, EmailAddress>();
                    for (EmailAddress emailAddress : user.emailAddresses)
                        emailAddressHashMap.put(emailAddress.address, emailAddress);
                    for (Address address : mail.getAllRecipients()) {
                        if (emailAddressHashMap.get(address.toString()) == null) {
                            EmailAddress emailAddress = new EmailAddress();
                            emailAddress.address = address.toString();
                            emailAddress.user = user;
                            emailAddress.create();
                        }
                    }
                    Transport.send(mail);
                } else folderName = "Drafts";

                // store to Sent Items
                if (authenticate(user)) {
                    EMailDAO emailDAO = getEMailDAO();
                    try {
                        Folder folder = emailDAO.openFolder(folderName, Folder.READ_WRITE);
                        Message[] msgs = new Message[1];
                        msgs[0] = mail;
                        msgs[0].setFlag(Flags.Flag.SEEN, true);
                        folder.appendMessages(msgs);
                    } catch (MessagingException e) {
                        System.out.println(e);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            composeNewMail("success", null, null, null);
        } else composeNewMail(null, null, null, null);
    }

    public static void uploadFile(String uploadPath, String filename) throws Exception {
        boolean success = true;
        String extension = "null";
        String filesize = "0";
        if (request.isNew) {
            FileOutputStream moveTo = null;
            try {
                String[] length = filename.split("\\.");
                extension = length[length.length - 1];
                filename = filename.replace("." + extension, "");
                InputStream data = request.body;
                System.out.println(uploadPath + filename + "." + extension);
                File file = new File(Play.applicationPath.getAbsolutePath() + uploadPath);
                if (!file.exists()) {
                    if (!file.mkdirs()) throw new Exception("Unable to create upload dir");
                }
                uploadPath += filename;
                file = new File(Play.applicationPath.getAbsolutePath() + uploadPath + "." + extension);
                moveTo = new FileOutputStream(file);
                IOUtils.copy(data, moveTo);
                moveTo.close();
                filesize = Functions.getFloatFormat((float) file.length() / 1024, 2);
            } catch (Exception ex) {
                success = false;
                ex.printStackTrace();
                if (moveTo != null) moveTo.close();
            }
        }
        renderJSON("{success: " + success + ",filedir:'" + uploadPath + "',filename:'" + filename + "',extension:'" + extension + "',filesize:'" + filesize + "'}");
    }

    public static void directSendEmail(String name, Long mid) {
        User user = Users.getUser();
        final Email email = user.emailAccount;
        if (email != null) {
            try {
                Properties props = System.getProperties();
                props.put("mail.smtp.host", "smtp." + CompanyConf.mail_domain);
                props.put("mail.smtp.auth", "true");
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email.username, email.password);
                    }
                });
                if (authenticate(user)) {
                    EMailDAO emailDAO = getEMailDAO();
                    Folder folder = emailDAO.openFolder(name, Folder.READ_WRITE);
                    Message message = folder.getMessage(mid.intValue());
                    Transport transport = session.getTransport("smtp");
                    transport.connect();
                    transport.sendMessage(message, message.getAllRecipients());
                    Folder sentFolder = emailDAO.openFolder("Sent Items", Folder.READ_WRITE);
                    message.setFlag(Flags.Flag.SEEN, true);
                    folder.copyMessages(new Message[]{message}, sentFolder);
                    message.setFlag(Flags.Flag.DELETED, true);
                    folder.expunge();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteEmail(String name, Long mid) {
        try {
            User user = Users.getUser();
            if (authenticate(user)) {
                EMailDAO emailDAO = getEMailDAO();
                Folder folder = emailDAO.openFolder(name, Folder.READ_WRITE);
                Message message = folder.getMessage(mid.intValue());
                if (!name.toLowerCase().startsWith("trash")) {
                    Folder sentFolder = emailDAO.openFolder("Trash", Folder.READ_WRITE);
                    folder.copyMessages(new Message[]{message}, sentFolder);
                }
                message.setFlag(Flags.Flag.DELETED, true);
                folder.expunge();
            }
        } catch (MessagingException e) {
            System.out.println("deleteEmail:" + e);
        }
    }
}
