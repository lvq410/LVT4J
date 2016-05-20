package com.lvt4j.office;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import com.lvt4j.basic.TVerify;
import com.sun.mail.pop3.POP3Message;

/**
 * <pre>
 * 收发邮件工具类
 *   使用前请先使用setSmtpHost等静态方法设置smtp,pop3,imap的地址及用户名密码
 * </pre>
 * 
 * @author LV
 * 
 */
public class TMail {

    private InternetAddress sender;
    private InternetAddress[] tos;
    private InternetAddress[] copyTos;
    private InternetAddress[] blindCopyTos;
    private Date date;
    private String subject;
    private String text;
    private String html;

    public InternetAddress getSender() {
        return sender;
    }

    public void setSender(InternetAddress sender) {
        this.sender = sender;
    }

    public void setSender(String sender) throws AddressException {
        this.sender = new InternetAddress(sender);
        try {
            this.sender.setPersonal(this.sender.getPersonal(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            //ignore
        }
    }

    public InternetAddress[] getTos() {
        return tos;
    }

    public void setTos(InternetAddress[] tos) {
        this.tos = tos;
    }

    public void setTos(String tos) throws AddressException {
        String[] to = tos.split(";");
        this.tos = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) {
            this.tos[i] = new InternetAddress(to[i]);
            try {
                this.tos[i].setPersonal(this.tos[i].getPersonal(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                //ignore
            }
        }
    }

    public InternetAddress[] getCopyTos() {
        return copyTos;
    }

    public void setCopyTos(InternetAddress[] copyTos) {
        this.copyTos = copyTos;
    }

    public void setCopyTos(String copyTos) throws AddressException {
        String[] copyTo = copyTos.split(";");
        this.copyTos = new InternetAddress[copyTo.length];
        for (int i = 0; i < copyTo.length; i++) {
            this.copyTos[i] = new InternetAddress(copyTo[i]);
        }
    }

    public InternetAddress[] getblindCopyTos() {
        return blindCopyTos;
    }

    public void setblindCopyTos(InternetAddress[] blindCopyTos) {
        this.blindCopyTos = blindCopyTos;
    }

    public void setblindCopyTos(String blindCopyTos) throws AddressException {
        String[] blindCopyTo = blindCopyTos.split(";");
        this.blindCopyTos = new InternetAddress[blindCopyTo.length];
        for (int i = 0; i < blindCopyTo.length; i++) {
            this.blindCopyTos[i] = new InternetAddress(blindCopyTo[i]);
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public TMail() {
        super();
    }

    /**
     * 构造函数
     * 
     * @param mimeMessage
     */
    public TMail(MimeMessage mimeMessage) {
        super();
        setMimeMessage(mimeMessage);
    }

    /**
     * MimeMessage设定
     * 
     * @param mimeMessage
     */
    public void setMimeMessage(MimeMessage mimeMessage) {
        parseMessage(mimeMessage);
    }

    public MimeMessage genMimeMessage(Session session) throws Exception {
        MimeMessage sendMess = new MimeMessage(session);
        sendMess.setSubject(subject, "utf-8");
        sendMess.setFrom(sender);
        sendMess.setSender(sender);
        sendMess.setRecipients(Message.RecipientType.TO, tos);
        if (copyTos != null)
            sendMess.setRecipients(Message.RecipientType.CC, copyTos);
        if (blindCopyTos != null)
            sendMess.setRecipients(Message.RecipientType.BCC, blindCopyTos);
        MimeMultipart mmp = new MimeMultipart();
        MimeBodyPart mbp = new MimeBodyPart();
        if (!TVerify.strNullOrEmpty(text))
            mbp.setContent(text, "text/plain; charset=utf-8");
        if (!TVerify.strNullOrEmpty(html))
            mbp.setContent(html, "text/html; charset=utf-8");
        mmp.addBodyPart(mbp);
        sendMess.setContent(mmp);
        return sendMess;
    }

    /**
     * 解析邮件内容
     */
    private boolean parseMessage(MimeMessage msg) {
        try {
            sender = ((InternetAddress[]) msg.getFrom())[0];
            tos = (InternetAddress[]) msg
                    .getRecipients(Message.RecipientType.TO);
            copyTos = (InternetAddress[]) msg
                    .getRecipients(Message.RecipientType.CC);
            blindCopyTos = (InternetAddress[]) msg
                    .getRecipients(Message.RecipientType.BCC);
            date = msg.getSentDate();
            subject = MimeUtility.decodeText(msg.getSubject());
            parseMultiPart((Part) msg);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 解析邮件的正文和附件
     * 
     * @param part
     * @throws Exception
     */
    private void parseMultiPart(Part part) throws Exception {
        if (part.isMimeType("text/html")) {
            html = (String) part.getContent();
        } else if (part.isMimeType("text/plain")) {
            text = (String) part.getContent();
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                Part p = multipart.getBodyPart(i);
                if (p.getDisposition() != null
                        && p.getDisposition().startsWith("attachment")) {
                    /*
                     * 附件文件的处理 String attachName =
                     * MimeUtility.decodeText(p.getFileName()); long size = 0;
                     * InputStream is = p.getInputStream(); byte[] buff = new
                     * byte[1024]; int len; while( ( len = is.read( buff ) ) !=
                     * -1 )size += len; is.close();
                     */
                } else if (p.isMimeType("text/html")) {
                    html = (String) p.getContent();
                } else if (p.isMimeType("text/plain")) {
                    text = (String) p.getContent();
                } else if (p.isMimeType("multipart/*")) {
                    parseMultiPart(p);
                }
            }
        }
    }

    private static String smtpHost;
    private static String pop3Host;
    private static String imapHost;
    private static String userName;
    private static String password;
    private static Properties props = new Properties();

    static {
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "25");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.pop3.auth", "true");
        props.put("mail.pop3.port", "110");
        props.put("mail.store.protocol", "pop3");
    }

    // -- getters & setters
    public static String getSmtpHost() {
        return smtpHost;
    }

    public static void setSmtpHost(String smtpHost) {
        TMail.smtpHost = smtpHost;
        props.setProperty("mail.smtp.host", smtpHost);
    }

    public static String getPop3Host() {
        return pop3Host;
    }

    public static void setPop3Host(String pop3Host) {
        TMail.pop3Host = pop3Host;
        props.setProperty("mail.pop3.host", pop3Host);
    }

    public static String getImapHost() {
        return imapHost;
    }

    public static void setImapHost(String imapHost) {
        TMail.imapHost = imapHost;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        TMail.userName = userName;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        TMail.password = password;
    }

    /**
     * 取得所有收件邮件
     * 
     * @return
     * @throws Exception
     */
    public static List<TMail> getAllMails() throws Exception {
        List<TMail> mails = new ArrayList<TMail>();
        Auth auth = new Auth(userName, password);
        Session session = Session.getInstance(props, auth);
        session.setPasswordAuthentication(new URLName(pop3Host),
                auth.getPasswordAuthentication());
        Store store = session.getStore("pop3");
        store.connect(pop3Host, userName, password);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        Message[] msgs = folder.getMessages();
        for (Message msg : msgs) {
            if (!msg.getFolder().isOpen())
                msg.getFolder().open(Folder.READ_ONLY);
            mails.add(new TMail((MimeMessage) msg));
            ((POP3Message) msg).invalidate(true);
        }
        folder.close(false);
        store.close();
        return mails;
    }

    /**
     * 取得与主题关键词对应的邮件
     * 
     * @param subjectPattern
     * @return
     * @throws Exception
     */
    public static List<TMail> getMailBySubject(String subjectPattern)
            throws Exception {
        List<TMail> mails = new ArrayList<TMail>();
        Auth auth = new Auth(userName, password);
        Session session = Session.getInstance(props, auth);
        session.setPasswordAuthentication(new URLName(pop3Host),
                auth.getPasswordAuthentication());
        Store store = session.getStore("pop3");
        store.connect(pop3Host, userName, password);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        SearchTerm st = new SubjectTerm(subjectPattern);
        Message[] msgs = folder.search(st);
        for (Message msg : msgs) {
            if (!msg.getFolder().isOpen())
                msg.getFolder().open(Folder.READ_ONLY);
            mails.add(new TMail((MimeMessage) msg));
            ((POP3Message) msg).invalidate(true);
        }
        folder.close(false);
        store.close();
        return mails;
    }

    /**
     * 根据邮件主题删除邮件
     * 
     * @param mail
     * @throws Exception
     */
    public static void delMailBySubject(TMail mail) throws Exception {
        Auth auth = new Auth(userName, password);
        Session session = Session.getInstance(props, auth);
        session.setPasswordAuthentication(new URLName(pop3Host),
                auth.getPasswordAuthentication());
        Store store = session.getStore("pop3");
        store.connect(pop3Host, userName, password);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        Message[] msgs = folder.search(new SubjectTerm(mail.getSubject()));
        for (Message msg : msgs) {
            if (!msg.getFolder().isOpen())
                msg.getFolder().open(Folder.READ_WRITE);
            msg.setFlag(Flag.DELETED, true);
        }
        folder.close(true);
        store.close();
    }

    /**
     * 发送邮件
     * 
     * @param mail
     * @throws Exception
     */
    public static void sendMail(TMail mail) throws Exception {
        Auth auth = new Auth(userName, password);
        Session session = Session.getInstance(props);
        session.setPasswordAuthentication(new URLName(smtpHost),
                auth.getPasswordAuthentication());
        Transport transport = session.getTransport("smtp");
        transport.connect(smtpHost, userName, password);
        MimeMessage msg = mail.genMimeMessage(session);
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();
    }

    /**
     * 保存草稿 需要设置imap地址
     * 
     * @param mail
     * @throws Exception
     */
    public static void saveDraft(TMail mail) throws Exception {
        Session session = Session.getInstance(props);
        Store store = session.getStore("imap");
        store.connect(imapHost, userName, password);
        Folder folder = store.getFolder("草稿箱");
        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }
        folder.appendMessages(new MimeMessage[] { mail.genMimeMessage(session) });
        folder.close(true);
        store.close();
    }

    /**
     * 根据邮件主题删除草稿
     * 
     * @param mail
     * @throws Exception
     */
    public static void delDraftBySubject(TMail mail) throws Exception {
        Auth auth = new Auth(userName, password);
        Session session = Session.getInstance(props, auth);
        session.setPasswordAuthentication(new URLName(pop3Host),
                auth.getPasswordAuthentication());
        Store store = session.getStore("imap");
        store.connect(imapHost, userName, password);
        Folder folder = store.getFolder("草稿箱");
        folder.open(Folder.READ_WRITE);
        for (Message msg : folder.getMessages()) {
            try {
                if (msg.getSubject().equals(mail.subject)) {
                    msg.setFlag(Flag.DELETED, true);
                }
            } catch (Exception e) {
            }
        }
        folder.close(true);
        store.close();
    }

    private static class Auth extends Authenticator {
        private String userID;
        private String pwd;

        public Auth(String userID, String pwd) {
            super();
            this.userID = userID;
            this.pwd = pwd;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userID, pwd);
        }
    }
}