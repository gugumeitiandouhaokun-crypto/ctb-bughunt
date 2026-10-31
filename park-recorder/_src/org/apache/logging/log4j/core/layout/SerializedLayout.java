/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractLayout;

@Deprecated
@Plugin(name="SerializedLayout", category="Core", elementType="layout", printObject=true)
public final class SerializedLayout
extends AbstractLayout<LogEvent> {
    private static byte[] serializedHeader;

    private SerializedLayout() {
        super(null, null, null);
        LOGGER.warn("SerializedLayout is deprecated due to the inherent security weakness in Java Serialization, see https://www.owasp.org/index.php/Deserialization_of_untrusted_data Consider using another layout, e.g. JsonLayout");
    }

    @Override
    public final byte[] toByteArray(LogEvent serializable) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            PrivateObjectOutputStream privateObjectOutputStream = new PrivateObjectOutputStream(byteArrayOutputStream);
            Throwable throwable = null;
            try {
                privateObjectOutputStream.writeObject(serializable);
                privateObjectOutputStream.reset();
            }
            catch (Throwable throwable2) {
                try {
                    serializable = throwable2;
                    throwable = throwable2;
                    throw serializable;
                }
                catch (Throwable throwable3) {
                    if (throwable != null) {
                        try {
                            privateObjectOutputStream.close();
                        }
                        catch (Throwable throwable4) {
                            throwable.addSuppressed(throwable4);
                        }
                    } else {
                        privateObjectOutputStream.close();
                    }
                    throw throwable3;
                }
            }
            privateObjectOutputStream.close();
        }
        catch (IOException iOException) {
            LOGGER.error("Serialization of LogEvent failed.", (Throwable)iOException);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public final LogEvent toSerializable(LogEvent logEvent) {
        return logEvent;
    }

    @Deprecated
    @PluginFactory
    public static SerializedLayout createLayout() {
        return new SerializedLayout();
    }

    @Override
    public final byte[] getHeader() {
        return serializedHeader;
    }

    @Override
    public final String getContentType() {
        return "application/octet-stream";
    }

    static {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(byteArrayOutputStream).close();
            serializedHeader = byteArrayOutputStream.toByteArray();
        }
        catch (Exception exception) {
            LOGGER.error("Unable to generate Object stream header", (Throwable)exception);
        }
    }

    class PrivateObjectOutputStream
    extends ObjectOutputStream {
        public PrivateObjectOutputStream(OutputStream outputStream) {
            super(outputStream);
        }

        @Override
        protected void writeStreamHeader() {
        }
    }
}

