/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.TemporaryUser;

import java.net.InetAddress;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author fel
 */
public class InvitationMailer {
    private static Session session;

    private static String smtpHost = "mail.inubit.com";
    private static String smtpUser = null;
    private static String smtpPwd = null;
    private static String mailFrom = "pes_info@inubit.com";

    private static class SmtpAuthenticator extends javax.mail.Authenticator {
        private PasswordAuthentication auth;
        public SmtpAuthenticator(String user, String pwd) {
            auth = new PasswordAuthentication(user, pwd);
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return auth;
        }

    }

    static {
        initializeSmtpConnection(smtpHost, smtpUser, smtpPwd);
    }

    public static String getSmtpHost() {
        return smtpHost;
    }

    public static String getSmtpUser() {
        return smtpUser;
    }

    public static String getSmtpPwd() {
        return smtpPwd;
    }

    public static String getMailFrom() {
        return mailFrom;
    }

    public static void setMailFrom( String mail ) {
        mailFrom = mail;
    }

    public static void inviteUser( String key, TemporaryUser user, String text, SingleUser invitationFrom, String modelName, RequestFacade req ) {
        try {
            Message msg = new MimeMessage( session );
            InternetAddress addressFrom = new InternetAddress( mailFrom );
            msg.setFrom( addressFrom );
            InternetAddress addressTo = new InternetAddress( user.getMail() );
            msg.setRecipient( Message.RecipientType.TO, addressTo );
            msg.setSubject( getSubjectText(invitationFrom, modelName) );
            msg.setContent( getContentText(key, text, user, invitationFrom, modelName, req), "text/plain" );
            Transport.send( msg );
        } catch (MessagingException m ) {
            m.printStackTrace();
        }
    }

    public static void initializeSmtpConnection( String host, String user, String pwd ) {
        Properties props = new Properties();
        props.put( "mail.smtp.host", host );

        smtpHost = host;
        smtpUser = null;
        smtpPwd = null;

        if (user != null && pwd != null) {
            props.put( "mail.smtp.auth", "true");
            SmtpAuthenticator auth = new SmtpAuthenticator(user, pwd);
            smtpUser = user;
            smtpPwd = pwd;
            
            session = Session.getInstance(props, auth);
            //session.setPasswordAuthentication(new URLName(host), new PasswordAuthentication(user, pwd));
        } else {
            session = Session.getDefaultInstance( props );
        }
    }

    private static String getSubjectText( SingleUser invitationFrom, String modelName) {
        StringBuilder b = new StringBuilder(100);

        b.append(invitationFrom.getRealName());
        b.append("(" + invitationFrom.getName() + ") ");
        b.append("invited you to comment on model \"");
        b.append(modelName);
        b.append("\"");

        return b.toString();
    }

    private static String getContentText( String key, String text , TemporaryUser user, SingleUser invitationFrom, String modelName, RequestFacade req ) {
        StringBuilder b = new StringBuilder(300);

        b.append(text);
        b.append("\n\n-----------------------------------------------------------------------\n\n");
        b.append(getDefaultText(key, user, invitationFrom, modelName, req));

        return b.toString();
    }

    private static String getDefaultText ( String key, TemporaryUser user, SingleUser invitationFrom, String modelName, RequestFacade req ) {
        StringBuilder b = new StringBuilder(200);

        b.append(getSubjectText(invitationFrom, modelName));

        b.append("\n\n");
        b.append("To make your comment, please follow the link below: \n\n");

        b.append(getLink(key, user, req));
        b.append("\n\nBest regards,\nThe Process-Editor-Server Team");


        return b.toString();
    }

    private static String getLink ( String key, TemporaryUser user, RequestFacade req ) {
        StringBuilder b = new StringBuilder(100);

        try {
            b.append(ProcessEditorServerHelper.getProtocol());
            b.append("://");
			b.append(InetAddress.getLocalHost().getHostName());
            b.append(":");
            b.append(req.getPort());
            b.append("/models/");
            b.append(user.getModelId());
            b.append("/versions/");
            b.append(user.getModelVersion());
            b.append("?key=");
            b.append(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return b.toString();
    }
}
