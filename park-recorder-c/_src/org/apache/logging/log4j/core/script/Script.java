/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.script;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginValue;
import org.apache.logging.log4j.core.script.AbstractScript;

@Plugin(name="Script", category="Core", printObject=true)
public class Script
extends AbstractScript {
    private static final String ATTR_LANGUAGE = "language";
    private static final String ATTR_SCRIPT_TEXT = "scriptText";
    static final String PLUGIN_NAME = "Script";

    public Script(String string, String string2, String string3) {
        super(string, string2, string3);
    }

    @PluginFactory
    public static Script createScript(@PluginAttribute(value="name") String string, @PluginAttribute(value="language") String string2, @PluginValue(value="scriptText") String string3) {
        if (string2 == null) {
            LOGGER.error("No '{}' attribute provided for {} plugin '{}'", (Object)ATTR_LANGUAGE, (Object)PLUGIN_NAME, (Object)string);
            string2 = "JavaScript";
        }
        if (string3 == null) {
            LOGGER.error("No '{}' attribute provided for {} plugin '{}'", (Object)ATTR_SCRIPT_TEXT, (Object)PLUGIN_NAME, (Object)string);
            return null;
        }
        return new Script(string, string2, string3);
    }

    public String toString() {
        if (this.getName() != null) {
            return this.getName();
        }
        return super.toString();
    }
}

