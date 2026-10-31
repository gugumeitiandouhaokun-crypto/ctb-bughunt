/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.io.Serializable;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;

@Plugin(name="PatternMatch", category="Core", printObject=true)
public final class PatternMatch {
    private final String key;
    private final String pattern;

    public PatternMatch(String string, String string2) {
        this.key = string;
        this.pattern = string2;
    }

    public final String getKey() {
        return this.key;
    }

    public final String getPattern() {
        return this.pattern;
    }

    public final String toString() {
        return this.key + '=' + this.pattern;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public final int hashCode() {
        int n = 31 + (this.key == null ? 0 : this.key.hashCode());
        return 31 * n + (this.pattern == null ? 0 : this.pattern.hashCode());
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
        object = (PatternMatch)object;
        if (this.key == null ? ((PatternMatch)object).key != null : !this.key.equals(((PatternMatch)object).key)) {
            return false;
        }
        return !(this.pattern == null ? ((PatternMatch)object).pattern != null : !this.pattern.equals(((PatternMatch)object).pattern));
    }

    public static class Builder
    implements Serializable,
    org.apache.logging.log4j.core.util.Builder<PatternMatch> {
        private static final long serialVersionUID = 1L;
        @PluginBuilderAttribute
        private String key;
        @PluginBuilderAttribute
        private String pattern;

        public Builder setKey(String string) {
            this.key = string;
            return this;
        }

        public Builder setPattern(String string) {
            this.pattern = string;
            return this;
        }

        @Override
        public PatternMatch build() {
            return new PatternMatch(this.key, this.pattern);
        }

        protected Object readResolve() {
            return new PatternMatch(this.key, this.pattern);
        }
    }
}

