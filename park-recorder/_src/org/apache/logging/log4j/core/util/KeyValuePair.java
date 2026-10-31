/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;

@Plugin(name="KeyValuePair", category="Core", printObject=true)
public final class KeyValuePair {
    private final String key;
    private final String value;

    public KeyValuePair(String string, String string2) {
        this.key = string;
        this.value = string2;
    }

    public final String getKey() {
        return this.key;
    }

    public final String getValue() {
        return this.value;
    }

    public final String toString() {
        return this.key + '=' + this.value;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public final int hashCode() {
        int n = 31 + (this.key == null ? 0 : this.key.hashCode());
        return 31 * n + (this.value == null ? 0 : this.value.hashCode());
    }

    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        object = (KeyValuePair)object;
        if (this.key == null ? ((KeyValuePair)object).key != null : !this.key.equals(((KeyValuePair)object).key)) {
            return false;
        }
        return !(this.value == null ? ((KeyValuePair)object).value != null : !this.value.equals(((KeyValuePair)object).value));
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<KeyValuePair> {
        @PluginBuilderAttribute
        private String key;
        @PluginBuilderAttribute
        private String value;

        public Builder setKey(String string) {
            this.key = string;
            return this;
        }

        public Builder setValue(String string) {
            this.value = string;
            return this;
        }

        @Override
        public KeyValuePair build() {
            return new KeyValuePair(this.key, this.value);
        }
    }
}

