/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import java.util.Objects;
import javax.script.Bindings;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderSet;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.script.AbstractScript;
import org.apache.logging.log4j.core.script.ScriptManager;

@Plugin(name="ScriptAppenderSelector", category="Core", elementType="appender", printObject=true)
public class ScriptAppenderSelector
extends AbstractAppender {
    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    private ScriptAppenderSelector(String string, Filter filter, Layout<? extends Serializable> layout, Property[] propertyArray) {
        super(string, filter, layout, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent logEvent) {
    }

    public static final class Builder
    implements org.apache.logging.log4j.core.util.Builder<Appender> {
        @PluginElement(value="AppenderSet")
        @Required
        private AppenderSet appenderSet;
        @PluginConfiguration
        @Required
        private Configuration configuration;
        @PluginBuilderAttribute
        @Required
        private String name;
        @PluginElement(value="Script")
        @Required
        private AbstractScript script;

        @Override
        public final Appender build() {
            if (this.name == null) {
                LOGGER.error("Name missing.");
                return null;
            }
            if (this.script == null) {
                LOGGER.error("Script missing for ScriptAppenderSelector appender {}", (Object)this.name);
                return null;
            }
            if (this.appenderSet == null) {
                LOGGER.error("AppenderSet missing for ScriptAppenderSelector appender {}", (Object)this.name);
                return null;
            }
            if (this.configuration == null) {
                LOGGER.error("Configuration missing for ScriptAppenderSelector appender {}", (Object)this.name);
                return null;
            }
            Object object = this.configuration.getScriptManager();
            ((ScriptManager)object).addScript(this.script);
            Bindings bindings = ((ScriptManager)object).createBindings(this.script);
            LOGGER.debug("ScriptAppenderSelector '{}' executing {} '{}': {}", (Object)this.name, (Object)this.script.getLanguage(), (Object)this.script.getName(), (Object)this.script.getScriptText());
            object = Objects.toString(((ScriptManager)object).execute(this.script.getName(), bindings), null);
            LOGGER.debug("ScriptAppenderSelector '{}' selected '{}'", (Object)this.name, object);
            return this.appenderSet.createAppender((String)object, this.name);
        }

        public final AppenderSet getAppenderSet() {
            return this.appenderSet;
        }

        public final Configuration getConfiguration() {
            return this.configuration;
        }

        public final String getName() {
            return this.name;
        }

        public final AbstractScript getScript() {
            return this.script;
        }

        public final Builder withAppenderNodeSet(AppenderSet appenderSet) {
            this.appenderSet = appenderSet;
            return this;
        }

        public final Builder withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public final Builder withName(String string) {
            this.name = string;
            return this;
        }

        public final Builder withScript(AbstractScript abstractScript) {
            this.script = abstractScript;
            return this;
        }
    }
}

