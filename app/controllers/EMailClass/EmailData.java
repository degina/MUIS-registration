package controllers.EMailClass;

import org.apache.commons.lang.StringEscapeUtils;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 8/6/15.
 */
public class EmailData {

    public int mid;
    public String subject;
    public Date dateReceived;
    public boolean isSeen;
    public boolean isAttach;

    public List<EmailRecipient> recipients;

    public EmailContentData contentData;

    public EmailData(Message message, boolean content) throws MessagingException {
        this.subject = message.getSubject();
        this.dateReceived = message.getReceivedDate();
        this.mid = message.getMessageNumber();
        this.recipients = new ArrayList<EmailRecipient>();
        this.recipients.add(getRecipient(message.getFrom()[0].toString(), "From"));
        if (content) {
            Address[] toAddresses = message.getRecipients(Message.RecipientType.TO);
            Address[] ccAddresses = message.getRecipients(Message.RecipientType.CC);
            Address[] bccAddresses = message.getRecipients(Message.RecipientType.BCC);
            if (toAddresses != null) {
                for (Address address : toAddresses) {
                    this.recipients.add(getRecipient(address.toString(), "To"));
                }
            }
            if (ccAddresses != null) {
                for (Address address : ccAddresses)
                    this.recipients.add(getRecipient(address.toString(), "Cc"));
            }
            if (bccAddresses != null) {
                for (Address address : bccAddresses)
                    this.recipients.add(getRecipient(address.toString(), "Bcc"));
            }
        }
        this.isSeen = message.getFlags().contains(Flags.Flag.SEEN);
        this.isAttach = false;
    }

    public EmailRecipient getRecipient(String fullname, String rtype) {
        String mail = "", address = "";
        for (int i = 0; i < fullname.length(); i++) {
            if ((int) fullname.charAt(i) != 60) mail += fullname.charAt(i);
            else {
                if (mail.length() > 2) mail = mail.substring(0, mail.length() - 1);
                address = fullname.substring(i, fullname.length());
                i = fullname.length();
            }
        }
        if (address.length() == 0) address = mail;
        mail = mail.replaceAll("'", "");
        mail = mail.replaceAll("\"", "");
        address = address.replaceAll("<", "");
        address = address.replaceAll(">", "");
        return new EmailRecipient(this, mail, address, rtype);
    }

    public EmailRecipient getRecipientsFrom() {
        for (EmailRecipient recipient : this.recipients) {
            if (recipient.rtype.equals("From")) return recipient;
        }
        return null;
    }

    public List<EmailRecipient> getRecipientsCc() {
        List<EmailRecipient> emailRecipients = new ArrayList<EmailRecipient>();
        for (EmailRecipient recipient : this.recipients) {
            if (recipient.rtype.equals("Cc")) emailRecipients.add(recipient);
        }
        return emailRecipients;
    }

    public List<EmailRecipient> getRecipientsBcc() {
        List<EmailRecipient> emailRecipients = new ArrayList<EmailRecipient>();
        for (EmailRecipient recipient : this.recipients) {
            if (recipient.rtype.equals("Bcc")) emailRecipients.add(recipient);
        }
        return emailRecipients;
    }

    public List<EmailRecipient> getRecipientsTo() {
        List<EmailRecipient> emailRecipients = new ArrayList<EmailRecipient>();
        for (EmailRecipient recipient : this.recipients) {
            if (recipient.rtype.equals("To")) emailRecipients.add(recipient);
        }
        return emailRecipients;
    }
}
