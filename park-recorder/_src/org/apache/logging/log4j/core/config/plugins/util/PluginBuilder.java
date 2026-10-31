/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.plugins.validation.ConstraintValidator;
import org.apache.logging.log4j.core.config.plugins.validation.ConstraintValidators;
import org.apache.logging.log4j.core.config.plugins.visitors.PluginVisitor;
import org.apache.logging.log4j.core.config.plugins.visitors.PluginVisitors;
import org.apache.logging.log4j.core.util.Builder;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.core.util.TypeUtil;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StringBuilders;

public class PluginBuilder
implements Builder<Object> {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final PluginType<?> pluginType;
    private final Class<?> clazz;
    private Configuration configuration;
    private Node node;
    private LogEvent event;

    public PluginBuilder(PluginType<?> pluginType) {
        this.pluginType = pluginType;
        this.clazz = pluginType.getPluginClass();
    }

    public PluginBuilder withConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public PluginBuilder withConfigurationNode(Node node) {
        this.node = node;
        return this;
    }

    public PluginBuilder forLogEvent(LogEvent logEvent) {
        this.event = logEvent;
        return this;
    }

    @Override
    public Object build() {
        Object object;
        this.verify();
        try {
            LOGGER.debug("Building Plugin[name={}, class={}].", (Object)this.pluginType.getElementName(), (Object)this.pluginType.getPluginClass().getName());
            object = PluginBuilder.createBuilder(this.clazz);
            if (object != null) {
                this.injectFields((Builder<?>)object);
                return object.build();
            }
        }
        catch (ConfigurationException configurationException) {
            LOGGER.error("Could not create plugin of type {} for element {}", (Object)this.clazz, (Object)this.node.getName(), (Object)configurationException);
            return null;
        }
        catch (Exception exception) {
            LOGGER.error("Could not create plugin of type {} for element {}: {}", (Object)this.clazz, (Object)this.node.getName(), (Object)(exception instanceof InvocationTargetException ? ((InvocationTargetException)exception).getCause() : exception).toString(), (Object)exception);
        }
        try {
            object = PluginBuilder.findFactoryMethod(this.clazz);
            Object[] objectArray = this.generateParameters((Method)object);
            return ((Method)object).invoke(null, objectArray);
        }
        catch (Exception exception) {
            LOGGER.error("Unable to invoke factory method in {} for element {}: {}", (Object)this.clazz, (Object)this.node.getName(), (Object)(exception instanceof InvocationTargetException ? ((InvocationTargetException)exception).getCause() : exception).toString(), (Object)exception);
            return null;
        }
    }

    private void verify() {
        Objects.requireNonNull(this.configuration, "No Configuration object was set.");
        Objects.requireNonNull(this.node, "No Node object was set.");
    }

    private static Builder<?> createBuilder(Class<?> methodArray) {
        for (Method method : methodArray.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(PluginBuilderFactory.class) || !Modifier.isStatic(method.getModifiers()) || !TypeUtil.isAssignable(Builder.class, method.getReturnType())) continue;
            ReflectionUtil.makeAccessible(method);
            return (Builder)method.invoke(null, new Object[0]);
        }
        return null;
    }

    private void injectFields(Builder<?> builder) {
        Object object = TypeUtil.getAllDeclaredFields(builder.getClass());
        AccessibleObject.setAccessible(object.toArray(new Field[0]), true);
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl = false;
        String string = "";
        object = object.iterator();
        while (object.hasNext()) {
            Field field = (Field)object.next();
            StringBuilder stringBuilder2 = stringBuilder;
            stringBuilder2.append(stringBuilder2.length() == 0 ? PluginBuilder.simpleName(builder) + "(" : ", ");
            Annotation[] annotationArray = field.getDeclaredAnnotations();
            String[] stringArray = PluginBuilder.extractPluginAliases(annotationArray);
            Object object2 = annotationArray;
            int n = annotationArray.length;
            for (int j = 0; j < n; ++j) {
                PluginVisitor<? extends Annotation> pluginVisitor;
                Object object3 = object2[j];
                if (object3 instanceof PluginAliases || (pluginVisitor = PluginVisitors.findVisitor(object3.annotationType())) == null || (object3 = pluginVisitor.setAliases(stringArray).setAnnotation((Annotation)object3).setConversionType(field.getType()).setStrSubstitutor(this.configuration.getStrSubstitutor()).setMember(field).visit(this.configuration, this.node, this.event, stringBuilder)) == null) continue;
                field.set(builder, object3);
            }
            object2 = ConstraintValidators.findValidators(annotationArray);
            Object object4 = field.get(builder);
            Iterator iterator = object2.iterator();
            while (iterator.hasNext()) {
                if (((ConstraintValidator)iterator.next()).isValid(field.getName(), object4)) continue;
                bl = true;
                if (!string.isEmpty()) {
                    string = string + ", ";
                }
                string = string + "field '" + field.getName() + "' has invalid value '" + object4 + "'";
            }
        }
        StringBuilder stringBuilder3 = stringBuilder;
        stringBuilder3.append(stringBuilder3.length() == 0 ? builder.getClass().getSimpleName() + "()" : ")");
        LOGGER.debug(stringBuilder.toString());
        if (bl) {
            throw new ConfigurationException("Arguments given for element " + this.node.getName() + " are invalid: " + string);
        }
        this.checkForRemainingAttributes();
        this.verifyNodeChildrenUsed();
    }

    private static String simpleName(Object object) {
        if (object == null) {
            return "null";
        }
        int n = ((String)(object = object.getClass().getName())).lastIndexOf(46);
        if (n < 0) {
            return object;
        }
        return ((String)object).substring(n + 1);
    }

    private static Method findFactoryMethod(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(PluginFactory.class) || !Modifier.isStatic(method.getModifiers())) continue;
            ReflectionUtil.makeAccessible(method);
            return method;
        }
        throw new IllegalStateException("No factory method found for class " + clazz.getName());
    }

    private Object[] generateParameters(Method method) {
        StringBuilder stringBuilder = new StringBuilder();
        Class<?>[] classArray = method.getParameterTypes();
        Annotation[][] annotationArray = method.getParameterAnnotations();
        Object[] objectArray = new Object[annotationArray.length];
        boolean bl = false;
        for (int j = 0; j < annotationArray.length; ++j) {
            StringBuilder stringBuilder2 = stringBuilder;
            stringBuilder2.append(stringBuilder2.length() == 0 ? method.getName() + "(" : ", ");
            String[] stringArray = PluginBuilder.extractPluginAliases(annotationArray[j]);
            for (Annotation annotation : annotationArray[j]) {
                Object object;
                PluginVisitor<? extends Annotation> pluginVisitor;
                if (annotation instanceof PluginAliases || (pluginVisitor = PluginVisitors.findVisitor(annotation.annotationType())) == null || (object = pluginVisitor.setAliases(stringArray).setAnnotation(annotation).setConversionType(classArray[j]).setStrSubstitutor(this.configuration.getStrSubstitutor()).setMember(method).visit(this.configuration, this.node, this.event, stringBuilder)) == null) continue;
                objectArray[j] = object;
            }
            Collection<ConstraintValidator<?>> collection = ConstraintValidators.findValidators(annotationArray[j]);
            Object object3 = objectArray[j];
            String string = "arg[" + j + "](" + PluginBuilder.simpleName(object3) + ")";
            Iterator object2 = collection.iterator();
            while (object2.hasNext()) {
                if (((ConstraintValidator)object2.next()).isValid(string, object3)) continue;
                bl = true;
            }
        }
        StringBuilder stringBuilder3 = stringBuilder;
        stringBuilder3.append(stringBuilder3.length() == 0 ? method.getName() + "()" : ")");
        this.checkForRemainingAttributes();
        this.verifyNodeChildrenUsed();
        LOGGER.debug(stringBuilder.toString());
        if (bl) {
            throw new ConfigurationException("Arguments given for element " + this.node.getName() + " are invalid");
        }
        return objectArray;
    }

    private static String[] extractPluginAliases(Annotation ... annotationArray) {
        String[] stringArray = null;
        for (Annotation annotation : annotationArray) {
            if (!(annotation instanceof PluginAliases)) continue;
            stringArray = ((PluginAliases)annotation).value();
        }
        return stringArray;
    }

    private void checkForRemainingAttributes() {
        Map<String, String> map = this.node.getAttributes();
        if (!map.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String string : map.keySet()) {
                if (stringBuilder.length() == 0) {
                    stringBuilder.append(this.node.getName());
                    stringBuilder.append(" contains ");
                    if (map.size() == 1) {
                        stringBuilder.append("an invalid element or attribute ");
                    } else {
                        stringBuilder.append("invalid attributes ");
                    }
                } else {
                    stringBuilder.append(", ");
                }
                StringBuilders.appendDqValue(stringBuilder, string);
            }
            LOGGER.error(stringBuilder.toString());
        }
    }

    private void verifyNodeChildrenUsed() {
        Object object = this.node.getChildren();
        if (!this.pluginType.isDeferChildren() && !object.isEmpty()) {
            object = object.iterator();
            while (object.hasNext()) {
                Node node = (Node)object.next();
                String string = this.node.getType().getElementName();
                string = string.equals(this.node.getName()) ? this.node.getName() : string + ' ' + this.node.getName();
                LOGGER.error("{} has no parameter that matches element {}", (Object)string, (Object)node.getName());
            }
        }
    }
}

