/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractLayout;
import org.apache.logging.log4j.message.Message;

@Plugin(name="MessageLayout", category="Core", elementType="layout", printObject=true)
public class MessageLayout
extends AbstractLayout<Message> {
    public MessageLayout() {
        super(null, null, null);
    }

    public MessageLayout(Configuration configuration, byte[] byArray, byte[] byArray2) {
        super(configuration, byArray, byArray2);
    }

    @Override
    public byte[] toByteArray(LogEvent logEvent) {
        return null;
    }

    @Override
    public Message toSerializable(LogEvent logEvent) {
        return logEvent.getMessage();
    }

    @Override
    public String getContentType() {
        return null;
    }

    @PluginFactory
    public static Layout<?> createLayout() {
        return new MessageLayout();
    }
}

