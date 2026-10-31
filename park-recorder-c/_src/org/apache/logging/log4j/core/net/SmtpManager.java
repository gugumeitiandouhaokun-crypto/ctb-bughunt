/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.mail.Authenticator
 *  javax.mail.BodyPart
 *  javax.mail.Message
 *  javax.mail.Message$RecipientType
 *  javax.mail.MessagingException
 *  javax.mail.Multipart
 *  javax.mail.PasswordAuthentication
 *  javax.mail.Session
 *  javax.mail.Transport
 *  javax.mail.internet.InternetHeaders
 *  javax.mail.internet.MimeBodyPart
 *  javax.mail.internet.MimeMessage
 *  javax.mail.internet.MimeMultipart
 *  javax.mail.internet.MimeUtility
 *  javax.mail.util.ByteArrayDataSource
 */
package org.apache.logging.log4j.core.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.net.MimeMessageBuilder;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.util.CyclicBuffer;
import org.apache.logging.log4j.core.util.NameUtil;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public class SmtpManager
extends AbstractManager {
    private static final SMTPManagerFactory FACTORY = new SMTPManagerFactory();
    private final Session session;
    private final CyclicBuffer<LogEvent> buffer;
    private volatile MimeMessage message;
    private final FactoryData data;

    private static MimeMessage createMimeMessage(FactoryData factoryData, Session session, LogEvent logEvent) {
        return new MimeMessageBuilder(session).setFrom(factoryData.from).setReplyTo(factoryData.replyto).setRecipients(Message.RecipientType.TO, factoryData.to).setRecipients(Message.RecipientType.CC, factoryData.cc).setRecipients(Message.RecipientType.BCC, factoryData.bcc).setSubject(factoryData.subject.toSerializable(logEvent)).build();
    }

    protected SmtpManager(String string, Session session, MimeMessage mimeMessage, FactoryData factoryData) {
        super(null, string);
        this.session = session;
        this.message = mimeMessage;
        this.data = factoryData;
        this.buffer = new CyclicBuffer<LogEvent>(LogEvent.class, factoryData.numElements);
    }

    public void add(LogEvent logEvent) {
        if (logEvent instanceof Log4jLogEvent && logEvent.getMessage() instanceof ReusableMessage) {
            ((Log4jLogEvent)logEvent).makeMessageImmutable();
        } else if (logEvent instanceof MutableLogEvent) {
            logEvent = ((MutableLogEvent)logEvent).createMemento();
        }
        this.buffer.add(logEvent);
    }

    public static SmtpManager getSmtpManager(Configuration object, String string, String string2, String string3, String string4, String string5, String string6, String string7, String string8, int n, String string9, String string10, boolean bl, String string11, int n2, SslConfiguration sslConfiguration) {
        if (Strings.isEmpty(string7)) {
            string7 = "smtp";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (string != null) {
            stringBuilder.append(string);
        }
        stringBuilder.append(':');
        if (string2 != null) {
            stringBuilder.append(string2);
        }
        stringBuilder.append(':');
        if (string3 != null) {
            stringBuilder.append(string3);
        }
        stringBuilder.append(':');
        if (string4 != null) {
            stringBuilder.append(string4);
        }
        stringBuilder.append(':');
        if (string5 != null) {
            stringBuilder.append(string5);
        }
        stringBuilder.append(':');
        if (string6 != null) {
            stringBuilder.append(string6);
        }
        stringBuilder.append(':');
        stringBuilder.append(string7).append(':').append(string8).append(':').append("port:");
        if (string9 != null) {
            stringBuilder.append(string9);
        }
        stringBuilder.append(':');
        if (string10 != null) {
            stringBuilder.append(string10);
        }
        stringBuilder.append(bl ? ":debug:" : "::");
        stringBuilder.append(string11);
        string11 = "SMTP:" + NameUtil.md5(stringBuilder.toString());
        object = PatternLayout.newSerializerBuilder().setConfiguration((Configuration)object).setPattern(string6).build();
        return SmtpManager.getManager(string11, FACTORY, new FactoryData(string, string2, string3, string4, string5, (AbstractStringLayout.Serializer)object, string7, string8, n, string9, string10, bl, n2, sslConfiguration));
    }

    public void sendEvents(Layout<?> object, LogEvent object2) {
        if (this.message == null) {
            this.connect((LogEvent)object2);
        }
        try {
            Object object3 = this.buffer.removeAll();
            object2 = this.formatContentToBytes((LogEvent[])object3, (LogEvent)object2, (Layout<?>)object);
            object = object.getContentType();
            object3 = this.getEncoding((byte[])object2, (String)object);
            object2 = this.encodeContentToBytes((byte[])object2, (String)object3);
            object = this.getHeaders((String)object, (String)object3);
            object = this.getMimeMultipart((byte[])object2, (InternetHeaders)object);
            SmtpManager smtpManager = this;
            smtpManager.sendMultipartMessage(smtpManager.message, (MimeMultipart)object);
            return;
        }
        catch (IOException | RuntimeException | MessagingException throwable) {
            this.logError("Caught exception while sending e-mail notification.", throwable);
            throw new LoggingException("Error occurred while sending email", throwable);
        }
    }

    protected byte[] formatContentToBytes(LogEvent[] logEventArray, LogEvent logEvent, Layout<?> layout) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.writeContent(logEventArray, logEvent, layout, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void writeContent(LogEvent[] logEventArray, LogEvent logEvent, Layout<?> layout, ByteArrayOutputStream byteArrayOutputStream) {
        this.writeHeader(layout, byteArrayOutputStream);
        this.writeBuffer(logEventArray, logEvent, layout, byteArrayOutputStream);
        this.writeFooter(layout, byteArrayOutputStream);
    }

    protected void writeHeader(Layout<?> object, OutputStream outputStream) {
        byte[] byArray = object.getHeader();
        object = byArray;
        if (byArray != null) {
            outputStream.write((byte[])object);
        }
    }

    /*
     * WARNING - void declaration
     */
    protected void writeBuffer(LogEvent[] objectArray, LogEvent logEvent, Layout<?> layout, OutputStream outputStream) {
        void var2_3;
        void var4_5;
        void var3_4;
        for (Object object : objectArray) {
            byte[] byArray = var3_4.toByteArray((LogEvent)object);
            var4_5.write(byArray);
        }
        byte[] byArray = var3_4.toByteArray((LogEvent)var2_3);
        var4_5.write(byArray);
    }

    protected void writeFooter(Layout<?> object, OutputStream outputStream) {
        byte[] byArray = object.getFooter();
        object = byArray;
        if (byArray != null) {
            outputStream.write((byte[])object);
        }
    }

    protected String getEncoding(byte[] byArray, String string) {
        return MimeUtility.getEncoding((DataSource)new ByteArrayDataSource(byArray, string));
    }

    protected byte[] encodeContentToBytes(byte[] byArray, String string) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.encodeContent(byArray, string, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void encodeContent(byte[] object, String object2, ByteArrayOutputStream object3) {
        OutputStream outputStream;
        Throwable throwable;
        outputStream = MimeUtility.encode((OutputStream)((Object)throwable), (String)((Object)outputStream));
        throwable = null;
        try {
            outputStream.write((byte[])object);
            if (outputStream == null) return;
        }
        catch (Throwable throwable2) {
            try {
                Throwable throwable3 = throwable2;
                throwable = throwable2;
                throw throwable3;
            }
            catch (Throwable throwable4) {
                if (outputStream == null) throw throwable4;
                if (throwable != null) {
                    try {
                        outputStream.close();
                        throw throwable4;
                    }
                    catch (Throwable throwable5) {
                        throwable.addSuppressed(throwable5);
                    }
                    throw throwable4;
                } else {
                    outputStream.close();
                }
                throw throwable4;
            }
        }
        outputStream.close();
        return;
    }

    protected InternetHeaders getHeaders(String string, String string2) {
        InternetHeaders internetHeaders = new InternetHeaders();
        internetHeaders.setHeader("Content-Type", string + "; charset=UTF-8");
        internetHeaders.setHeader("Content-Transfer-Encoding", string2);
        return internetHeaders;
    }

    protected MimeMultipart getMimeMultipart(byte[] object, InternetHeaders internetHeaders) {
        MimeMultipart mimeMultipart = new MimeMultipart();
        object = new MimeBodyPart(internetHeaders, object);
        mimeMultipart.addBodyPart((BodyPart)object);
        return mimeMultipart;
    }

    protected void sendMultipartMessage(MimeMessage mimeMessage, MimeMultipart mimeMultipart) {
        MimeMessage mimeMessage2 = mimeMessage;
        synchronized (mimeMessage2) {
            mimeMessage.setContent((Multipart)mimeMultipart);
            mimeMessage.setSentDate(new Date());
            Transport.send((Message)mimeMessage);
            return;
        }
    }

    private synchronized void connect(LogEvent logEvent) {
        if (this.message != null) {
            return;
        }
        try {
            this.message = SmtpManager.createMimeMessage(this.data, this.session, logEvent);
            return;
        }
        catch (MessagingException messagingException) {
            this.logError("Could not set SmtpAppender message options", messagingException);
            this.message = null;
            return;
        }
    }

    static class SMTPManagerFactory
    implements ManagerFactory<SmtpManager, FactoryData> {
        private SMTPManagerFactory() {
        }

        @Override
        public SmtpManager createManager(String string, FactoryData factoryData) {
            SslConfiguration sslConfiguration;
            Authenticator authenticator;
            String string2 = "mail." + factoryData.protocol;
            Properties properties = PropertiesUtil.getSystemProperties();
            properties.setProperty("mail.transport.protocol", factoryData.protocol);
            if (properties.getProperty("mail.host") == null) {
                properties.setProperty("mail.host", NetUtils.getLocalHostname());
            }
            if (null != factoryData.host) {
                properties.setProperty(string2 + ".host", factoryData.host);
            }
            if (factoryData.port > 0) {
                properties.setProperty(string2 + ".port", String.valueOf(factoryData.port));
            }
            if ((authenticator = this.buildAuthenticator(factoryData.username, factoryData.password)) != null) {
                properties.setProperty(string2 + ".auth", "true");
            }
            if (factoryData.protocol.equals("smtps") && (sslConfiguration = factoryData.sslConfiguration) != null) {
                SSLSocketFactory sSLSocketFactory = sslConfiguration.getSslSocketFactory();
                properties.put(string2 + ".ssl.socketFactory", sSLSocketFactory);
                properties.setProperty(string2 + ".ssl.checkserveridentity", Boolean.toString(sslConfiguration.isVerifyHostName()));
            }
            sslConfiguration = Session.getInstance((Properties)properties, (Authenticator)authenticator);
            sslConfiguration.setProtocolForAddress("rfc822", factoryData.protocol);
            sslConfiguration.setDebug(factoryData.isDebug);
            return new SmtpManager(string, (Session)sslConfiguration, null, factoryData);
        }

        private Authenticator buildAuthenticator(final String string, final String string2) {
            if (string2 != null && string != null) {
                return new Authenticator(){
                    private final PasswordAuthentication passwordAuthentication;
                    {
                        this.passwordAuthentication = new PasswordAuthentication(string, string2);
                    }

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return this.passwordAuthentication;
                    }
                };
            }
            return null;
        }
    }

    static class FactoryData {
        private final String to;
        private final String cc;
        private final String bcc;
        private final String from;
        private final String replyto;
        private final AbstractStringLayout.Serializer subject;
        private final String protocol;
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final boolean isDebug;
        private final int numElements;
        private final SslConfiguration sslConfiguration;

        public FactoryData(String string, String string2, String string3, String string4, String string5, AbstractStringLayout.Serializer serializer, String string6, String string7, int n, String string8, String string9, boolean bl, int n2, SslConfiguration sslConfiguration) {
            this.to = string;
            this.cc = string2;
            this.bcc = string3;
            this.from = string4;
            this.replyto = string5;
            this.subject = serializer;
            this.protocol = string6;
            this.host = string7;
            this.port = n;
            this.username = string8;
            this.password = string9;
            this.isDebug = bl;
            this.numElements = n2;
            this.sslConfiguration = sslConfiguration;
        }
    }
}

