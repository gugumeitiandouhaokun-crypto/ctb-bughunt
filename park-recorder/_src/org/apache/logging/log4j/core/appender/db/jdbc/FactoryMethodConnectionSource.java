/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.db.jdbc.AbstractConnectionSource;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="ConnectionFactory", category="Core", elementType="connectionSource", printObject=true)
public final class FactoryMethodConnectionSource
extends AbstractConnectionSource {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final DataSource dataSource;
    private final String description;

    private FactoryMethodConnectionSource(DataSource dataSource, String string, String string2, String string3) {
        this.dataSource = dataSource;
        this.description = "factory{ public static " + string3 + ' ' + string + '.' + string2 + "() }";
    }

    @Override
    public final Connection getConnection() {
        return this.dataSource.getConnection();
    }

    @Override
    public final String toString() {
        return this.description;
    }

    @PluginFactory
    public static FactoryMethodConnectionSource createConnectionSource(@PluginAttribute(value="class") String string, @PluginAttribute(value="method") String string2) {
        Object object;
        if (Strings.isEmpty(string) || Strings.isEmpty(string2)) {
            LOGGER.error("No class name or method name specified for the connection factory method.");
            return null;
        }
        try {
            object = Loader.loadClass(string).getMethod(string2, new Class[0]);
        }
        catch (Exception exception) {
            LOGGER.error(exception.toString(), (Throwable)exception);
            return null;
        }
        Class<?> clazz = ((Method)object).getReturnType();
        String string3 = clazz.getName();
        if (clazz == DataSource.class) {
            try {
                object = (DataSource)((Method)object).invoke(null, new Object[0]);
                string3 = string3 + "[" + object + ']';
            }
            catch (Exception exception) {
                LOGGER.error(exception.toString(), (Throwable)exception);
                return null;
            }
        } else if (clazz == Connection.class) {
            object = new DataSource((Method)object){
                final /* synthetic */ Method val$method;
                {
                    this.val$method = method;
                }

                @Override
                public final Connection getConnection() {
                    try {
                        return (Connection)this.val$method.invoke(null, new Object[0]);
                    }
                    catch (Exception exception) {
                        throw new SQLException("Failed to obtain connection from factory method.", exception);
                    }
                }

                @Override
                public final Connection getConnection(String string, String string2) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public final int getLoginTimeout() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public final PrintWriter getLogWriter() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public final java.util.logging.Logger getParentLogger() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public final boolean isWrapperFor(Class<?> clazz) {
                    return false;
                }

                @Override
                public final void setLoginTimeout(int n) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public final void setLogWriter(PrintWriter printWriter) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public final <T> T unwrap(Class<T> clazz) {
                    return null;
                }
            };
        } else {
            LOGGER.error("Method [{}.{}()] returns unsupported type [{}].", (Object)string, (Object)string2, (Object)clazz.getName());
            return null;
        }
        return new FactoryMethodConnectionSource((DataSource)object, string, string2, string3);
    }
}

