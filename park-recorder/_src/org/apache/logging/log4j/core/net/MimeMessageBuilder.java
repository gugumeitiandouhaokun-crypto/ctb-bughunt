/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.mail.Address
 *  javax.mail.Message$RecipientType
 *  javax.mail.Session
 *  javax.mail.internet.InternetAddress
 *  javax.mail.internet.MimeMessage
 */
package org.apache.logging.log4j.core.net;

import java.nio.charset.StandardCharsets;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.logging.log4j.core.util.Builder;

public class MimeMessageBuilder
implements Builder<MimeMessage> {
    private final MimeMessage message;

    public MimeMessageBuilder(Session session) {
        this.message = new MimeMessage(session);
    }

    public MimeMessageBuilder setFrom(String string) {
        if ((string = MimeMessageBuilder.parseAddress(string)) != null) {
            this.message.setFrom((Address)string);
        } else {
            try {
                this.message.setFrom();
            }
            catch (Exception exception) {
                this.message.setFrom(null);
            }
        }
        return this;
    }

    public MimeMessageBuilder setReplyTo(String internetAddressArray) {
        if ((internetAddressArray = MimeMessageBuilder.parseAddresses((String)internetAddressArray)) != null) {
            this.message.setReplyTo((Address[])internetAddressArray);
        }
        return this;
    }

    public MimeMessageBuilder setRecipients(Message.RecipientType recipientType, String internetAddressArray) {
        if ((internetAddressArray = MimeMessageBuilder.parseAddresses((String)internetAddressArray)) != null) {
            this.message.setRecipients(recipientType, (Address[])internetAddressArray);
        }
        return this;
    }

    public MimeMessageBuilder setSubject(String string) {
        if (string != null) {
            this.message.setSubject(string, StandardCharsets.UTF_8.name());
        }
        return this;
    }

    @Deprecated
    public MimeMessage getMimeMessage() {
        return this.build();
    }

    @Override
    public MimeMessage build() {
        return this.message;
    }

    private static InternetAddress parseAddress(String string) {
        if (string == null) {
            return null;
        }
        return new InternetAddress(string);
    }

    private static InternetAddress[] parseAddresses(String string) {
        if (string == null) {
            return null;
        }
        return InternetAddress.parse((String)string, (boolean)true);
    }
}

