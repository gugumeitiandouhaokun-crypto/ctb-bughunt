/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.processor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry;

@SupportedAnnotationTypes(value={"org.apache.logging.log4j.core.config.plugins.*"})
public class PluginProcessor
extends AbstractProcessor {
    public static final String PLUGIN_CACHE_FILE = "META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat";
    private final PluginCache pluginCache = new PluginCache();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> object, RoundEnvironment set) {
        object = this.processingEnv.getMessager();
        object.printMessage(Diagnostic.Kind.NOTE, "Processing Log4j annotations");
        try {
            set = set.getElementsAnnotatedWith(Plugin.class);
            if (set.isEmpty()) {
                object.printMessage(Diagnostic.Kind.NOTE, "No elements to process");
                return false;
            }
            this.collectPlugins(set);
            Set<? extends Element> set2 = set;
            this.writeCacheFile(set2.toArray(new Element[set2.size()]));
            object.printMessage(Diagnostic.Kind.NOTE, "Annotations processed");
            return true;
        }
        catch (IOException iOException) {
            set = iOException;
            iOException.printStackTrace();
            this.error(((Throwable)((Object)set)).getMessage());
            return false;
        }
        catch (Exception exception) {
            set = exception;
            exception.printStackTrace();
            this.error(((Throwable)((Object)set)).getMessage());
            return false;
        }
    }

    private void error(CharSequence charSequence) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, charSequence);
    }

    private void collectPlugins(Iterable<? extends Element> object) {
        Object object2 = this.processingEnv.getElementUtils();
        PluginElementVisitor pluginElementVisitor = new PluginElementVisitor((Elements)object2);
        object2 = new PluginAliasesElementVisitor((Elements)object2);
        object = object.iterator();
        while (object.hasNext()) {
            Element element = (Element)object.next();
            Object object32 = element.getAnnotation(Plugin.class);
            if (object32 == null) continue;
            PluginEntry pluginEntry = element.accept(pluginElementVisitor, object32);
            Map<String, PluginEntry> map = this.pluginCache.getCategory(pluginEntry.getCategory());
            map.put(pluginEntry.getKey(), pluginEntry);
            for (Object object32 : (Collection)element.accept(object2, object32)) {
                map.put(((PluginEntry)object32).getKey(), (PluginEntry)object32);
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void writeCacheFile(Element ... object) {
        object = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", PLUGIN_CACHE_FILE, (Element[])object).openOutputStream();
        Throwable throwable = null;
        try {
            this.pluginCache.writeCache((OutputStream)object);
            if (object == null) return;
        }
        catch (Throwable throwable2) {
            try {
                Throwable throwable3 = throwable2;
                throwable = throwable2;
                throw throwable3;
            }
            catch (Throwable throwable4) {
                if (object == null) throw throwable4;
                if (throwable != null) {
                    try {
                        ((OutputStream)object).close();
                        throw throwable4;
                    }
                    catch (Throwable throwable5) {
                        throwable.addSuppressed(throwable5);
                    }
                    throw throwable4;
                } else {
                    ((OutputStream)object).close();
                }
                throw throwable4;
            }
        }
        ((OutputStream)object).close();
        return;
    }

    static class PluginAliasesElementVisitor
    extends SimpleElementVisitor7<Collection<PluginEntry>, Plugin> {
        private final Elements elements;

        private PluginAliasesElementVisitor(Elements elements) {
            super(Collections.emptyList());
            this.elements = elements;
        }

        @Override
        public Collection<PluginEntry> visitType(TypeElement typeElement, Plugin plugin) {
            String[] stringArray = typeElement.getAnnotation(PluginAliases.class);
            if (stringArray == null) {
                return (Collection)this.DEFAULT_VALUE;
            }
            ArrayList<PluginEntry> arrayList = new ArrayList<PluginEntry>(stringArray.value().length);
            for (String string : stringArray.value()) {
                PluginEntry pluginEntry = new PluginEntry();
                pluginEntry.setKey(string.toLowerCase(Locale.US));
                pluginEntry.setClassName(this.elements.getBinaryName(typeElement).toString());
                pluginEntry.setName("".equals(plugin.elementType()) ? string : plugin.elementType());
                pluginEntry.setPrintable(plugin.printObject());
                pluginEntry.setDefer(plugin.deferChildren());
                pluginEntry.setCategory(plugin.category());
                arrayList.add(pluginEntry);
            }
            return arrayList;
        }
    }

    static class PluginElementVisitor
    extends SimpleElementVisitor7<PluginEntry, Plugin> {
        private final Elements elements;

        private PluginElementVisitor(Elements elements) {
            this.elements = elements;
        }

        @Override
        public PluginEntry visitType(TypeElement typeElement, Plugin plugin) {
            Objects.requireNonNull(plugin, "Plugin annotation is null.");
            PluginEntry pluginEntry = new PluginEntry();
            pluginEntry.setKey(plugin.name().toLowerCase(Locale.US));
            pluginEntry.setClassName(this.elements.getBinaryName(typeElement).toString());
            pluginEntry.setName("".equals(plugin.elementType()) ? plugin.name() : plugin.elementType());
            pluginEntry.setPrintable(plugin.printObject());
            pluginEntry.setDefer(plugin.deferChildren());
            pluginEntry.setCategory(plugin.category());
            return pluginEntry;
        }
    }
}

