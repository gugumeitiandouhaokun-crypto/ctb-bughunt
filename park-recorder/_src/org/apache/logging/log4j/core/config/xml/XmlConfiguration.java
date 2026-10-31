/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;
import org.apache.logging.log4j.core.config.status.StatusConfiguration;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.Patterns;
import org.apache.logging.log4j.core.util.Throwables;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlConfiguration
extends AbstractConfiguration
implements Reconfigurable {
    private static final String XINCLUDE_FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language";
    private static final String XINCLUDE_FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris";
    private static final String[] VERBOSE_CLASSES = new String[]{ResolverUtil.class.getName()};
    private static final String LOG4J_XSD = "Log4j-config.xsd";
    private final List<Status> status;
    private Element rootElement;
    private boolean strict;
    private String schemaResource;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public XmlConfiguration(LoggerContext object, ConfigurationSource configurationSource) {
        block36: {
            Object object2;
            Object object3;
            Object object4;
            super((LoggerContext)object, configurationSource);
            this.status = new ArrayList<Status>();
            configurationSource.getFile();
            object = null;
            try {
                object4 = configurationSource.getInputStream();
                try {
                    object = XmlConfiguration.toByteArray((InputStream)object4);
                }
                finally {
                    Closer.closeSilently((AutoCloseable)object4);
                }
                InputSource inputSource = new InputSource(new ByteArrayInputStream((byte[])object));
                inputSource.setSystemId(configurationSource.getLocation());
                DocumentBuilder documentBuilder = XmlConfiguration.newDocumentBuilder(true);
                try {
                    object3 = documentBuilder.parse(inputSource);
                }
                catch (Exception exception) {
                    object2 = exception;
                    if (!(Throwables.getRootCause(exception) instanceof UnsupportedOperationException)) throw object2;
                    LOGGER.warn("The DocumentBuilder {} does not support an operation: {}.Trying again without XInclude...", (Object)documentBuilder, object2);
                    object3 = XmlConfiguration.newDocumentBuilder(false).parse(inputSource);
                }
                this.rootElement = object3.getDocumentElement();
                XmlConfiguration xmlConfiguration = this;
                object2 = xmlConfiguration.processAttributes(xmlConfiguration.rootNode, this.rootElement);
                object3 = new StatusConfiguration().withVerboseClasses(VERBOSE_CLASSES).withStatus(this.getDefaultStatus());
                int n = 0;
                for (Map.Entry entry : object2.entrySet()) {
                    object2 = (String)entry.getKey();
                    String string = this.getStrSubstitutor().replace((String)entry.getValue());
                    if ("status".equalsIgnoreCase((String)object2)) {
                        ((StatusConfiguration)object3).withStatus(string);
                        continue;
                    }
                    if ("dest".equalsIgnoreCase((String)object2)) {
                        ((StatusConfiguration)object3).withDestination(string);
                        continue;
                    }
                    if ("shutdownHook".equalsIgnoreCase((String)object2)) {
                        this.isShutdownHookEnabled = !"disable".equalsIgnoreCase(string);
                        continue;
                    }
                    if ("shutdownTimeout".equalsIgnoreCase((String)object2)) {
                        this.shutdownTimeoutMillis = Long.parseLong(string);
                        continue;
                    }
                    if ("verbose".equalsIgnoreCase((String)object2)) {
                        ((StatusConfiguration)object3).withVerbosity(string);
                        continue;
                    }
                    if ("packages".equalsIgnoreCase((String)object2)) {
                        this.pluginPackages.addAll(Arrays.asList(string.split(Patterns.COMMA_SEPARATOR)));
                        continue;
                    }
                    if ("name".equalsIgnoreCase((String)object2)) {
                        this.setName(string);
                        continue;
                    }
                    if ("strict".equalsIgnoreCase((String)object2)) {
                        this.strict = Boolean.parseBoolean(string);
                        continue;
                    }
                    if ("schema".equalsIgnoreCase((String)object2)) {
                        this.schemaResource = string;
                        continue;
                    }
                    if ("monitorInterval".equalsIgnoreCase((String)object2)) {
                        n = Integer.parseInt(string);
                        continue;
                    }
                    if (!"advertiser".equalsIgnoreCase((String)object2)) continue;
                    this.createAdvertiser(string, configurationSource, (byte[])object, "text/xml");
                }
                XmlConfiguration xmlConfiguration2 = this;
                xmlConfiguration2.initializeWatchers(xmlConfiguration2, configurationSource, n);
                ((StatusConfiguration)object3).initialize();
            }
            catch (IOException | ParserConfigurationException | SAXException exception) {
                LOGGER.error("Error parsing " + configurationSource.getLocation(), (Throwable)exception);
            }
            if (this.strict && this.schemaResource != null && object != null) {
                try {
                    object4 = Loader.getResourceAsStream(this.schemaResource, XmlConfiguration.class.getClassLoader());
                    Throwable throwable = null;
                    try {
                        if (object4 != null) {
                            StreamSource streamSource = new StreamSource((InputStream)object4, LOG4J_XSD);
                            object3 = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                            object2 = null;
                            try {
                                object2 = ((SchemaFactory)object3).newSchema(streamSource);
                            }
                            catch (SAXException sAXException) {
                                LOGGER.error("Error parsing Log4j schema", (Throwable)sAXException);
                            }
                            if (object2 != null) {
                                object3 = ((Schema)object2).newValidator();
                                try {
                                    ((Validator)object3).validate(new StreamSource(new ByteArrayInputStream((byte[])object)));
                                }
                                catch (IOException iOException) {
                                    LOGGER.error("Error reading configuration for validation", (Throwable)iOException);
                                }
                                catch (SAXException sAXException) {
                                    LOGGER.error("Error validating configuration", (Throwable)sAXException);
                                }
                            }
                        }
                        if (object4 == null) break block36;
                    }
                    catch (Throwable throwable2) {
                        try {
                            Throwable throwable3 = throwable2;
                            Throwable throwable4 = throwable2;
                            throw throwable3;
                        }
                        catch (Throwable throwable5) {
                            if (object4 == null) throw throwable5;
                            if (throwable == null) {
                                ((InputStream)object4).close();
                                throw throwable5;
                            }
                            try {
                                ((InputStream)object4).close();
                                throw throwable5;
                            }
                            catch (Throwable throwable6) {
                                throwable.addSuppressed(throwable6);
                                throw throwable5;
                            }
                        }
                    }
                    ((InputStream)object4).close();
                }
                catch (Exception exception) {
                    LOGGER.error("Unable to access schema {}", (Object)this.schemaResource, (Object)exception);
                }
            }
        }
        if (this.getName() != null) return;
        this.setName(configurationSource.getLocation());
    }

    static DocumentBuilder newDocumentBuilder(boolean bl) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        XmlConfiguration.disableDtdProcessing(documentBuilderFactory);
        if (bl) {
            XmlConfiguration.enableXInclude(documentBuilderFactory);
        }
        return documentBuilderFactory.newDocumentBuilder();
    }

    private static void disableDtdProcessing(DocumentBuilderFactory documentBuilderFactory) {
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        XmlConfiguration.setFeature(documentBuilderFactory, "http://xml.org/sax/features/external-general-entities", false);
        XmlConfiguration.setFeature(documentBuilderFactory, "http://xml.org/sax/features/external-parameter-entities", false);
        XmlConfiguration.setFeature(documentBuilderFactory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    }

    private static void setFeature(DocumentBuilderFactory documentBuilderFactory, String string, boolean bl) {
        try {
            documentBuilderFactory.setFeature(string, bl);
            return;
        }
        catch (Exception | LinkageError throwable) {
            Throwable throwable2 = throwable;
            XmlConfiguration.getStatusLogger().error("Caught {} setting feature {} to {} on DocumentBuilderFactory {}: {}", (Object)throwable.getClass().getCanonicalName(), (Object)string, (Object)bl, (Object)documentBuilderFactory, (Object)throwable2, (Object)throwable2);
            return;
        }
    }

    private static void enableXInclude(DocumentBuilderFactory documentBuilderFactory) {
        try {
            documentBuilderFactory.setXIncludeAware(true);
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            LOGGER.warn("The DocumentBuilderFactory [{}] does not support XInclude: {}", (Object)documentBuilderFactory, (Object)unsupportedOperationException);
        }
        catch (AbstractMethodError | NoSuchMethodError incompatibleClassChangeError) {
            LOGGER.warn("The DocumentBuilderFactory [{}] is out of date and does not support XInclude: {}", (Object)documentBuilderFactory, (Object)incompatibleClassChangeError);
        }
        try {
            documentBuilderFactory.setFeature(XINCLUDE_FIXUP_BASE_URIS, true);
        }
        catch (ParserConfigurationException parserConfigurationException) {
            LOGGER.warn("The DocumentBuilderFactory [{}] does not support the feature [{}]: {}", (Object)documentBuilderFactory, (Object)XINCLUDE_FIXUP_BASE_URIS, (Object)parserConfigurationException);
        }
        catch (AbstractMethodError abstractMethodError) {
            LOGGER.warn("The DocumentBuilderFactory [{}] is out of date and does not support setFeature: {}", (Object)documentBuilderFactory, (Object)abstractMethodError);
        }
        try {
            documentBuilderFactory.setFeature(XINCLUDE_FIXUP_LANGUAGE, true);
            return;
        }
        catch (ParserConfigurationException parserConfigurationException) {
            LOGGER.warn("The DocumentBuilderFactory [{}] does not support the feature [{}]: {}", (Object)documentBuilderFactory, (Object)XINCLUDE_FIXUP_LANGUAGE, (Object)parserConfigurationException);
            return;
        }
        catch (AbstractMethodError abstractMethodError) {
            LOGGER.warn("The DocumentBuilderFactory [{}] is out of date and does not support setFeature: {}", (Object)documentBuilderFactory, (Object)abstractMethodError);
            return;
        }
    }

    @Override
    public void setup() {
        if (this.rootElement == null) {
            LOGGER.error("No logging configuration");
            return;
        }
        XmlConfiguration xmlConfiguration = this;
        xmlConfiguration.constructHierarchy(xmlConfiguration.rootNode, this.rootElement);
        if (this.status.size() > 0) {
            for (Status status : this.status) {
                LOGGER.error("Error processing element {} ({}): {}", (Object)status.name, (Object)status.element, (Object)status.errorType);
            }
            return;
        }
        this.rootElement = null;
    }

    @Override
    public Configuration reconfigure() {
        try {
            Object object = this.getConfigurationSource().resetInputStream();
            if (object == null) {
                return null;
            }
            object = new XmlConfiguration(this.getLoggerContext(), (ConfigurationSource)object);
            if (((XmlConfiguration)object).rootElement == null) {
                return null;
            }
            return object;
        }
        catch (IOException iOException) {
            LOGGER.error("Cannot locate file {}", (Object)this.getConfigurationSource(), (Object)iOException);
            return null;
        }
    }

    private void constructHierarchy(Node node, Element element) {
        this.processAttributes(node, element);
        StringBuilder stringBuilder = new StringBuilder();
        NodeList nodeList = element.getChildNodes();
        List<Node> list = node.getChildren();
        for (int j = 0; j < nodeList.getLength(); ++j) {
            Object object = nodeList.item(j);
            if (object instanceof Element) {
                object = (Element)object;
                String string = this.getType((Element)object);
                PluginType<?> pluginType = this.pluginManager.getPluginType(string);
                Node node2 = new Node(node, string, pluginType);
                this.constructHierarchy(node2, (Element)object);
                if (pluginType == null) {
                    object = node2.getValue();
                    if (!node2.hasChildren() && object != null) {
                        node.getAttributes().put(string, (String)object);
                        continue;
                    }
                    this.status.add(new Status(string, element, ErrorType.CLASS_NOT_FOUND));
                    continue;
                }
                list.add(node2);
                continue;
            }
            if (!(object instanceof Text)) continue;
            object = (Text)object;
            stringBuilder.append(object.getData());
        }
        String string = stringBuilder.toString().trim();
        if (string.length() > 0 || !node.hasChildren() && !node.isRoot()) {
            node.setValue(string);
        }
    }

    private String getType(Element object) {
        if (this.strict) {
            NamedNodeMap namedNodeMap = object.getAttributes();
            for (int j = 0; j < namedNodeMap.getLength(); ++j) {
                org.w3c.dom.Node node = namedNodeMap.item(j);
                if (!(node instanceof Attr) || !(node = (Attr)node).getName().equalsIgnoreCase("type")) continue;
                object = node.getValue();
                namedNodeMap.removeNamedItem(node.getName());
                return object;
            }
        }
        return object.getTagName();
    }

    private Map<String, String> processAttributes(Node object, Element object2) {
        object2 = object2.getAttributes();
        object = ((Node)object).getAttributes();
        for (int j = 0; j < object2.getLength(); ++j) {
            org.w3c.dom.Node node = object2.item(j);
            if (!(node instanceof Attr) || (node = (Attr)node).getName().equals("xml:base")) continue;
            object.put(node.getName(), node.getValue());
        }
        return object;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[location=" + this.getConfigurationSource() + "]";
    }

    static class Status {
        private final Element element;
        private final String name;
        private final ErrorType errorType;

        public Status(String string, Element element, ErrorType errorType) {
            this.name = string;
            this.element = element;
            this.errorType = errorType;
        }

        public String toString() {
            return "Status [name=" + this.name + ", element=" + this.element + ", errorType=" + (Object)((Object)this.errorType) + "]";
        }
    }

    static enum ErrorType {
        CLASS_NOT_FOUND;

    }
}

