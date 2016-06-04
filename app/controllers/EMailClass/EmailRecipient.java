package controllers.EMailClass;

/**
 * Created by enkhamgalan on 8/6/15.
 */
public class EmailRecipient {
    public String rtype;
    public String mail;
    public String address;

    public EmailData emailData;

    public EmailRecipient(EmailData emailData,String mail, String address,String rtype) {
        this.emailData = emailData;
        this.mail = mail;
        this.address = address;
        this.rtype = rtype;
    }

    public String getMail() {
        if (this.mail.length() > 2 && isLatinLetter(this.mail.charAt(0))) return this.mail;
        else return this.address;
    }
    public boolean isLatinLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
    }
}
