/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.net.Advertiser;

public abstract class AbstractFileAppender<M extends OutputStreamManager>
extends AbstractOutputStreamAppender<M> {
    private final String fileName;
    private final Advertiser advertiser;
    private final Object advertisement;

    private AbstractFileAppender(String string, Layout<? extends Serializable> layout, Filter object, M m, String string2, boolean bl, boolean bl2, Advertiser advertiser, Property[] propertyArray) {
        super(string, layout, (Filter)object, bl, bl2, propertyArray, m);
        if (advertiser != null) {
            object = new HashMap<String, String>(layout.getContentFormat());
            object.putAll(((AbstractManager)m).getContentFormat());
            object.put("contentType", layout.getContentType());
            object.put("name", string);
            this.advertisement = advertiser.advertise((Map<String, String>)object);
        } else {
            this.advertisement = null;
        }
        this.fileName = string2;
        this.advertiser = advertiser;
    }

    public String getFileName() {
        return this.fileName;
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        super.stop(l, timeUnit, false);
        if (this.advertiser != null) {
            this.advertiser.unadvertise(this.advertisement);
        }
        this.setStopped();
        return true;
    }

    public static abstract class Builder<B extends Builder<B>>
    extends AbstractOutputStreamAppender.Builder<B> {
        @PluginBuilderAttribute
        @Required
        private String fileName;
        @PluginBuilderAttribute
        private boolean append = true;
        @PluginBuilderAttribute
        private boolean locking;
        @PluginBuilderAttribute
        private boolean advertise;
        @PluginBuilderAttribute
        private String advertiseUri;
        @PluginBuilderAttribute
        private boolean createOnDemand;
        @PluginBuilderAttribute
        private String filePermissions;
        @PluginBuilderAttribute
        private String fileOwner;
        @PluginBuilderAttribute
        private String fileGroup;

        public String getAdvertiseUri() {
            return this.advertiseUri;
        }

        public String getFileName() {
            return this.fileName;
        }

        public boolean isAdvertise() {
            return this.advertise;
        }

        public boolean isAppend() {
            return this.append;
        }

        public boolean isCreateOnDemand() {
            return this.createOnDemand;
        }

        public boolean isLocking() {
            return this.locking;
        }

        public String getFilePermissions() {
            return this.filePermissions;
        }

        public String getFileOwner() {
            return this.fileOwner;
        }

        public String getFileGroup() {
            return this.fileGroup;
        }

        public B withAdvertise(boolean bl) {
            this.advertise = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withAdvertiseUri(String string) {
            this.advertiseUri = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withAppend(boolean bl) {
            this.append = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withFileName(String string) {
            this.fileName = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withCreateOnDemand(boolean bl) {
            this.createOnDemand = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withLocking(boolean bl) {
            this.locking = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withFilePermissions(String string) {
            this.filePermissions = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withFileOwner(String string) {
            this.fileOwner = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withFileGroup(String string) {
            this.fileGroup = string;
            return (B)((Builder)this.asBuilder());
        }
    }
}

