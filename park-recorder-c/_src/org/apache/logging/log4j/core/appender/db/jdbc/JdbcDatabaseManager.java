/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.io.Serializable;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseManager;
import org.apache.logging.log4j.core.appender.db.ColumnMapping;
import org.apache.logging.log4j.core.appender.db.DbAppenderLoggingException;
import org.apache.logging.log4j.core.appender.db.jdbc.ColumnConfig;
import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.apache.logging.log4j.core.config.plugins.convert.DateTypeConverter;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.core.util.Log4jThread;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.spi.ThreadContextStack;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.Strings;

public final class JdbcDatabaseManager
extends AbstractDatabaseManager {
    private static final JdbcDatabaseManagerFactory INSTANCE = new JdbcDatabaseManagerFactory();
    private final List<ColumnConfig> columnConfigs;
    private final String sqlStatement;
    private final FactoryData factoryData;
    private volatile Connection connection;
    private volatile PreparedStatement statement;
    private volatile Reconnector reconnector;
    private volatile boolean isBatchSupported;
    private volatile Map<String, ResultSetColumnMetaData> columnMetaData;

    private static void appendColumnName(int n, String string, StringBuilder stringBuilder) {
        if (n > 1) {
            stringBuilder.append(',');
        }
        stringBuilder.append(string);
    }

    private static void appendColumnNames(String string, FactoryData factoryData, StringBuilder stringBuilder) {
        int n = 1;
        if (factoryData.columnMappings != null) {
            String string2;
            for (ColumnMapping object : factoryData.columnMappings) {
                string2 = object.getName();
                JdbcDatabaseManager.appendColumnName(n, string2, stringBuilder);
                JdbcDatabaseManager.logger().trace("Appending {} {}[{}]: {}={} ", (Object)string, (Object)object.getClass().getSimpleName(), (Object)n, (Object)string2, (Object)object);
                ++n;
            }
            if (factoryData.columnConfigs != null) {
                for (ColumnConfig columnConfig : factoryData.columnConfigs) {
                    string2 = columnConfig.getColumnName();
                    JdbcDatabaseManager.appendColumnName(n, string2, stringBuilder);
                    JdbcDatabaseManager.logger().trace("Appending {} {}[{}]: {}={} ", (Object)string, (Object)columnConfig.getClass().getSimpleName(), (Object)n, (Object)string2, (Object)columnConfig);
                    ++n;
                }
            }
        }
    }

    private static JdbcDatabaseManagerFactory getFactory() {
        return INSTANCE;
    }

    @Deprecated
    public static JdbcDatabaseManager getJDBCDatabaseManager(String string, int n, ConnectionSource connectionSource, String string2, ColumnConfig[] columnConfigArray) {
        return JdbcDatabaseManager.getManager(string, new FactoryData(n, null, connectionSource, string2, columnConfigArray, new ColumnMapping[0], false, 5000L, true), JdbcDatabaseManager.getFactory());
    }

    @Deprecated
    public static JdbcDatabaseManager getManager(String string, int n, Layout<? extends Serializable> layout, ConnectionSource connectionSource, String string2, ColumnConfig[] columnConfigArray, ColumnMapping[] columnMappingArray) {
        return JdbcDatabaseManager.getManager(string, new FactoryData(n, layout, connectionSource, string2, columnConfigArray, columnMappingArray, false, 5000L, true), JdbcDatabaseManager.getFactory());
    }

    @Deprecated
    public static JdbcDatabaseManager getManager(String string, int n, Layout<? extends Serializable> layout, ConnectionSource connectionSource, String string2, ColumnConfig[] columnConfigArray, ColumnMapping[] columnMappingArray, boolean bl, long l) {
        return JdbcDatabaseManager.getManager(string, new FactoryData(n, null, connectionSource, string2, columnConfigArray, columnMappingArray, false, 5000L, true), JdbcDatabaseManager.getFactory());
    }

    public static JdbcDatabaseManager getManager(String string, int n, Layout<? extends Serializable> layout, ConnectionSource connectionSource, String string2, ColumnConfig[] columnConfigArray, ColumnMapping[] columnMappingArray, boolean bl, long l, boolean bl2) {
        return JdbcDatabaseManager.getManager(string, new FactoryData(n, layout, connectionSource, string2, columnConfigArray, columnMappingArray, bl, l, bl2), JdbcDatabaseManager.getFactory());
    }

    private JdbcDatabaseManager(String string, String string2, List<ColumnConfig> list, FactoryData factoryData) {
        super(string, factoryData.getBufferSize());
        this.sqlStatement = string2;
        this.columnConfigs = list;
        this.factoryData = factoryData;
    }

    private void checkConnection() {
        boolean bl = true;
        try {
            JdbcDatabaseManager jdbcDatabaseManager = this;
            bl = jdbcDatabaseManager.isClosed(jdbcDatabaseManager.connection);
        }
        catch (SQLException sQLException) {}
        boolean bl2 = true;
        try {
            JdbcDatabaseManager jdbcDatabaseManager = this;
            bl2 = jdbcDatabaseManager.isClosed(jdbcDatabaseManager.statement);
        }
        catch (SQLException sQLException) {}
        if (!this.isRunning() || bl || bl2) {
            this.closeResources(false);
            if (this.reconnector != null && !this.factoryData.immediateFail) {
                this.reconnector.latch();
                if (this.connection == null) {
                    throw new AppenderLoggingException("Error writing to JDBC Manager '%s': JDBC connection not available [%s]", this.getName(), this.fieldsToString());
                }
                if (this.statement == null) {
                    throw new AppenderLoggingException("Error writing to JDBC Manager '%s': JDBC statement not available [%s].", this.getName(), this.connection, this.fieldsToString());
                }
            }
        }
    }

    protected final void closeResources(boolean bl) {
        AutoCloseable autoCloseable;
        block5: {
            autoCloseable = this.statement;
            this.statement = null;
            try {
                Closer.close(autoCloseable);
            }
            catch (Exception exception) {
                if (!bl) break block5;
                this.logWarn("Failed to close SQL statement logging event or flushing buffer", exception);
            }
        }
        autoCloseable = this.connection;
        this.connection = null;
        try {
            Closer.close(autoCloseable);
            return;
        }
        catch (Exception exception) {
            if (bl) {
                this.logWarn("Failed to close database connection logging event or flushing buffer", exception);
            }
            return;
        }
    }

    @Override
    public final boolean commitAndClose() {
        block8: {
            try {
                if (this.connection == null || this.connection.isClosed()) break block8;
                if (this.isBuffered() && this.isBatchSupported && this.statement != null) {
                    int[] nArray;
                    JdbcDatabaseManager.logger().debug("Executing batch PreparedStatement {}", (Object)this.statement);
                    try {
                        nArray = this.statement.executeBatch();
                    }
                    catch (SQLTransactionRollbackException sQLTransactionRollbackException) {
                        JdbcDatabaseManager.logger().debug("{} executing batch PreparedStatement {}, retrying.", (Object)sQLTransactionRollbackException, (Object)this.statement);
                        nArray = this.statement.executeBatch();
                    }
                    JdbcDatabaseManager.logger().debug("Batch result: {}", (Object)Arrays.toString(nArray));
                }
                JdbcDatabaseManager.logger().debug("Committing Connection {}", (Object)this.connection);
                this.connection.commit();
            }
            catch (SQLException sQLException) {
                throw new DbAppenderLoggingException(sQLException, "Failed to commit transaction logging event or flushing buffer [%s]", this.fieldsToString());
            }
            finally {
                this.closeResources(true);
            }
        }
        return true;
    }

    private boolean commitAndCloseAll() {
        if (this.connection != null || this.statement != null) {
            try {
                this.commitAndClose();
                return true;
            }
            catch (AppenderLoggingException appenderLoggingException) {
                AppenderLoggingException appenderLoggingException2 = appenderLoggingException;
                Throwable throwable = appenderLoggingException.getCause();
                Throwable throwable2 = throwable = throwable == null ? appenderLoggingException2 : throwable;
                JdbcDatabaseManager.logger().debug("{} committing and closing connection: {}", (Object)throwable2, (Object)throwable2.getClass().getSimpleName(), (Object)appenderLoggingException2.toString(), (Object)appenderLoggingException2);
            }
        }
        if (this.factoryData.connectionSource != null) {
            this.factoryData.connectionSource.stop();
        }
        return true;
    }

    private void connectAndPrepare() {
        JdbcDatabaseManager.logger().debug("Acquiring JDBC connection from {}", (Object)this.getConnectionSource());
        this.connection = this.getConnectionSource().getConnection();
        JdbcDatabaseManager.logger().debug("Acquired JDBC connection {}", (Object)this.connection);
        JdbcDatabaseManager.logger().debug("Getting connection metadata {}", (Object)this.connection);
        DatabaseMetaData databaseMetaData = this.connection.getMetaData();
        JdbcDatabaseManager.logger().debug("Connection metadata {}", (Object)databaseMetaData);
        this.isBatchSupported = databaseMetaData.supportsBatchUpdates();
        JdbcDatabaseManager.logger().debug("Connection supportsBatchUpdates: {}", (Object)this.isBatchSupported);
        this.connection.setAutoCommit(false);
        JdbcDatabaseManager.logger().debug("Preparing SQL {}", (Object)this.sqlStatement);
        this.statement = this.connection.prepareStatement(this.sqlStatement);
        JdbcDatabaseManager.logger().debug("Prepared SQL {}", (Object)this.statement);
        if (this.factoryData.truncateStrings) {
            this.initColumnMetaData();
        }
    }

    @Override
    public final void connectAndStart() {
        this.checkConnection();
        JdbcDatabaseManager jdbcDatabaseManager = this;
        synchronized (jdbcDatabaseManager) {
            try {
                this.connectAndPrepare();
            }
            catch (SQLException sQLException) {
                this.reconnectOn(sQLException);
            }
            return;
        }
    }

    private Reconnector createReconnector() {
        Reconnector reconnector = new Reconnector();
        reconnector.setDaemon(true);
        reconnector.setPriority(1);
        return reconnector;
    }

    private String createSqlSelect() {
        StringBuilder stringBuilder = new StringBuilder("select ");
        JdbcDatabaseManager.appendColumnNames("SELECT", this.factoryData, stringBuilder);
        stringBuilder.append(" from ");
        stringBuilder.append(this.factoryData.tableName);
        stringBuilder.append(" where 1=0");
        return stringBuilder.toString();
    }

    private String fieldsToString() {
        return String.format("columnConfigs=%s, sqlStatement=%s, factoryData=%s, connection=%s, statement=%s, reconnector=%s, isBatchSupported=%s, columnMetaData=%s", this.columnConfigs, this.sqlStatement, this.factoryData, this.connection, this.statement, this.reconnector, this.isBatchSupported, this.columnMetaData);
    }

    public final ConnectionSource getConnectionSource() {
        return this.factoryData.connectionSource;
    }

    public final String getSqlStatement() {
        return this.sqlStatement;
    }

    public final String getTableName() {
        return this.factoryData.tableName;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void initColumnMetaData() {
        Object object = this.createSqlSelect();
        JdbcDatabaseManager.logger().debug("Getting SQL metadata for table {}: {}", (Object)this.factoryData.tableName, object);
        object = this.connection.prepareStatement((String)object);
        Throwable throwable = null;
        try {
            ResultSetMetaData resultSetMetaData = object.getMetaData();
            JdbcDatabaseManager.logger().debug("SQL metadata: {}", (Object)resultSetMetaData);
            if (resultSetMetaData != null) {
                int n = resultSetMetaData.getColumnCount();
                this.columnMetaData = new HashMap<String, ResultSetColumnMetaData>(n);
                int n2 = 0;
                int n3 = 1;
                while (n2 < n) {
                    ResultSetColumnMetaData resultSetColumnMetaData = new ResultSetColumnMetaData(resultSetMetaData, n3);
                    this.columnMetaData.put(resultSetColumnMetaData.getNameKey(), resultSetColumnMetaData);
                    ++n2;
                    ++n3;
                }
            } else {
                JdbcDatabaseManager.logger().warn("{}: truncateStrings is true and ResultSetMetaData is null for statement: {}; manager will not perform truncation.", (Object)this.getClass().getSimpleName(), object);
            }
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
                        object.close();
                        throw throwable4;
                    }
                    catch (Throwable throwable5) {
                        throwable.addSuppressed(throwable5);
                    }
                    throw throwable4;
                } else {
                    object.close();
                }
                throw throwable4;
            }
        }
        object.close();
        return;
    }

    private boolean isClosed(Statement statement) {
        return statement == null || statement.isClosed();
    }

    private boolean isClosed(Connection connection) {
        return connection == null || connection.isClosed();
    }

    private void reconnectOn(Exception exception) {
        block4: {
            if (!this.factoryData.retry) {
                throw new AppenderLoggingException("Cannot connect and prepare", exception);
            }
            if (this.reconnector == null) {
                this.reconnector = this.createReconnector();
                try {
                    this.reconnector.reconnect();
                    return;
                }
                catch (SQLException sQLException) {
                    JdbcDatabaseManager.logger().debug("Cannot reestablish JDBC connection to {}: {}; starting reconnector thread {}", (Object)this.factoryData, (Object)sQLException, (Object)this.reconnector.getName(), (Object)sQLException);
                    this.reconnector.start();
                    this.reconnector.latch();
                    if (this.connection != null && this.statement != null) break block4;
                    throw new AppenderLoggingException(exception, "Error sending to %s for %s [%s]", this.getName(), this.factoryData, this.fieldsToString());
                }
            }
        }
    }

    private void setFields(MapMessage<?, ?> serializable) {
        serializable = ((MapMessage)serializable).getIndexedReadOnlyStringMap();
        String string = this.statement.getClass().getName();
        int n = 1;
        if (this.factoryData.columnMappings != null) {
            for (ColumnMapping columnMapping : this.factoryData.columnMappings) {
                if (columnMapping.getLiteralValue() != null) continue;
                String string2 = columnMapping.getSource();
                string2 = Strings.isEmpty(string2) ? columnMapping.getName() : string2;
                Object v = serializable.getValue(string2);
                if (JdbcDatabaseManager.logger().isTraceEnabled()) {
                    String string3 = v instanceof String ? "\"" + v + "\"" : Objects.toString(v, null);
                    JdbcDatabaseManager.logger().trace("{} setObject({}, {}) for key '{}' and mapping '{}'", (Object)string, (Object)n, (Object)string3, (Object)string2, (Object)columnMapping.getName());
                }
                this.setStatementObject(n, columnMapping.getNameKey(), v);
                ++n;
            }
        }
    }

    private void setStatementObject(int n, String string, Object object) {
        if (this.statement == null) {
            throw new AppenderLoggingException("Cannot set a value when the PreparedStatement is null.");
        }
        if (object == null) {
            if (this.columnMetaData == null) {
                throw new AppenderLoggingException("Cannot set a value when the column metadata is null.");
            }
            this.statement.setNull(n, this.columnMetaData.get(string).getType());
            return;
        }
        this.statement.setObject(n, this.truncate(string, object));
    }

    @Override
    public final boolean shutdownInternal() {
        if (this.reconnector != null) {
            this.reconnector.shutdown();
            this.reconnector.interrupt();
            this.reconnector = null;
        }
        return this.commitAndCloseAll();
    }

    @Override
    public final void startupInternal() {
    }

    private Object truncate(String string, Object object) {
        if (object != null && this.factoryData.truncateStrings && this.columnMetaData != null) {
            ResultSetColumnMetaData resultSetColumnMetaData = this.columnMetaData.get(string);
            if (resultSetColumnMetaData != null) {
                if (resultSetColumnMetaData.isStringType()) {
                    object = resultSetColumnMetaData.truncate(object.toString());
                }
            } else {
                JdbcDatabaseManager.logger().error("Missing ResultSetColumnMetaData for {}, connection={}, statement={}", (Object)string, (Object)this.connection, (Object)this.statement);
            }
        }
        return object;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public final void writeInternal(LogEvent logEvent, Serializable serializable) {
        StringReader stringReader = null;
        try {
            block32: {
                block31: {
                    if (!this.isRunning()) break block31;
                    JdbcDatabaseManager jdbcDatabaseManager = this;
                    if (jdbcDatabaseManager.isClosed(jdbcDatabaseManager.connection)) break block31;
                    JdbcDatabaseManager jdbcDatabaseManager2 = this;
                    if (!jdbcDatabaseManager2.isClosed(jdbcDatabaseManager2.statement)) break block32;
                }
                throw new AppenderLoggingException("Cannot write logging event; JDBC manager not connected to the database, running=%s, [%s]).", this.isRunning(), this.fieldsToString());
            }
            this.statement.clearParameters();
            if (serializable instanceof MapMessage) {
                this.setFields((MapMessage)serializable);
            }
            int n = 1;
            if (this.factoryData.columnMappings != null) {
                for (ColumnMapping columnMapping : this.factoryData.columnMappings) {
                    if (ThreadContextMap.class.isAssignableFrom(columnMapping.getType()) || ReadOnlyStringMap.class.isAssignableFrom(columnMapping.getType())) {
                        this.statement.setObject(n++, logEvent.getContextData().toMap());
                        continue;
                    }
                    if (ThreadContextStack.class.isAssignableFrom(columnMapping.getType())) {
                        this.statement.setObject(n++, logEvent.getContextStack().asList());
                        continue;
                    }
                    if (Date.class.isAssignableFrom(columnMapping.getType())) {
                        this.statement.setObject(n++, DateTypeConverter.fromMillis(logEvent.getTimeMillis(), columnMapping.getType().asSubclass(Date.class)));
                        continue;
                    }
                    StringLayout stringLayout = columnMapping.getLayout();
                    if (stringLayout == null) continue;
                    if (Clob.class.isAssignableFrom(columnMapping.getType())) {
                        this.statement.setClob(n++, new StringReader((String)stringLayout.toSerializable(logEvent)));
                        continue;
                    }
                    if (NClob.class.isAssignableFrom(columnMapping.getType())) {
                        this.statement.setNClob(n++, new StringReader((String)stringLayout.toSerializable(logEvent)));
                        continue;
                    }
                    stringLayout = TypeConverters.convert((String)stringLayout.toSerializable(logEvent), columnMapping.getType(), null);
                    this.setStatementObject(n++, columnMapping.getNameKey(), stringLayout);
                }
            }
            for (ColumnConfig columnConfig : this.columnConfigs) {
                if (columnConfig.isEventTimestamp()) {
                    this.statement.setTimestamp(n++, new Timestamp(logEvent.getTimeMillis()));
                    continue;
                }
                if (columnConfig.isClob()) {
                    stringReader = new StringReader(columnConfig.getLayout().toSerializable(logEvent));
                    if (columnConfig.isUnicode()) {
                        this.statement.setNClob(n++, stringReader);
                        continue;
                    }
                    this.statement.setClob(n++, stringReader);
                    continue;
                }
                if (columnConfig.isUnicode()) {
                    this.statement.setNString(n++, Objects.toString(this.truncate(columnConfig.getColumnNameKey(), columnConfig.getLayout().toSerializable(logEvent)), null));
                    continue;
                }
                this.statement.setString(n++, Objects.toString(this.truncate(columnConfig.getColumnNameKey(), columnConfig.getLayout().toSerializable(logEvent)), null));
            }
            if (this.isBuffered() && this.isBatchSupported) {
                JdbcDatabaseManager.logger().debug("addBatch for {}", (Object)this.statement);
                this.statement.addBatch();
            } else {
                int n2 = this.statement.executeUpdate();
                JdbcDatabaseManager.logger().debug("executeUpdate = {} for {}", (Object)n2, (Object)this.statement);
                if (n2 == 0) {
                    throw new AppenderLoggingException("No records inserted in database table for log event in JDBC manager [%s].", this.fieldsToString());
                }
            }
        }
        catch (SQLException sQLException) {
            try {
                throw new DbAppenderLoggingException(sQLException, "Failed to insert record for log event in JDBC manager: %s [%s]", sQLException, this.fieldsToString());
            }
            catch (Throwable throwable) {
                try {
                    if (this.statement != null) {
                        this.statement.clearParameters();
                    }
                }
                catch (SQLException sQLException2) {}
                Closer.closeSilently(stringReader);
                throw throwable;
            }
        }
        try {
            if (this.statement != null) {
                this.statement.clearParameters();
            }
        }
        catch (SQLException sQLException) {}
        Closer.closeSilently(stringReader);
    }

    @Override
    public final void writeThrough(LogEvent logEvent, Serializable serializable) {
        this.connectAndStart();
        try {
            try {
                this.writeInternal(logEvent, serializable);
                return;
            }
            finally {
                this.commitAndClose();
            }
        }
        catch (DbAppenderLoggingException dbAppenderLoggingException) {
            this.reconnectOn(dbAppenderLoggingException);
            try {
                this.writeInternal(logEvent, serializable);
                return;
            }
            finally {
                this.commitAndClose();
            }
        }
    }

    static final class ResultSetColumnMetaData {
        private final String schemaName;
        private final String catalogName;
        private final String tableName;
        private final String name;
        private final String nameKey;
        private final String label;
        private final int displaySize;
        private final int type;
        private final String typeName;
        private final String className;
        private final int precision;
        private final int scale;
        private final boolean isStringType;

        public ResultSetColumnMetaData(ResultSetMetaData resultSetMetaData, int n) {
            this(resultSetMetaData.getSchemaName(n), resultSetMetaData.getCatalogName(n), resultSetMetaData.getTableName(n), resultSetMetaData.getColumnName(n), resultSetMetaData.getColumnLabel(n), resultSetMetaData.getColumnDisplaySize(n), resultSetMetaData.getColumnType(n), resultSetMetaData.getColumnTypeName(n), resultSetMetaData.getColumnClassName(n), resultSetMetaData.getPrecision(n), resultSetMetaData.getScale(n));
        }

        private ResultSetColumnMetaData(String string, String string2, String string3, String string4, String string5, int n, int n2, String string6, String string7, int n3, int n4) {
            this.schemaName = string;
            this.catalogName = string2;
            this.tableName = string3;
            this.name = string4;
            this.nameKey = ColumnMapping.toKey(string4);
            this.label = string5;
            this.displaySize = n;
            this.type = n2;
            this.typeName = string6;
            this.className = string7;
            this.precision = n3;
            this.scale = n4;
            this.isStringType = n2 == 1 || n2 == -16 || n2 == -1 || n2 == -9 || n2 == 12;
        }

        public final String getCatalogName() {
            return this.catalogName;
        }

        public final String getClassName() {
            return this.className;
        }

        public final int getDisplaySize() {
            return this.displaySize;
        }

        public final String getLabel() {
            return this.label;
        }

        public final String getName() {
            return this.name;
        }

        public final String getNameKey() {
            return this.nameKey;
        }

        public final int getPrecision() {
            return this.precision;
        }

        public final int getScale() {
            return this.scale;
        }

        public final String getSchemaName() {
            return this.schemaName;
        }

        public final String getTableName() {
            return this.tableName;
        }

        public final int getType() {
            return this.type;
        }

        public final String getTypeName() {
            return this.typeName;
        }

        public final boolean isStringType() {
            return this.isStringType;
        }

        public final String toString() {
            return String.format("ColumnMetaData [schemaName=%s, catalogName=%s, tableName=%s, name=%s, nameKey=%s, label=%s, displaySize=%s, type=%s, typeName=%s, className=%s, precision=%s, scale=%s, isStringType=%s]", this.schemaName, this.catalogName, this.tableName, this.name, this.nameKey, this.label, this.displaySize, this.type, this.typeName, this.className, this.precision, this.scale, this.isStringType);
        }

        public final String truncate(String string) {
            if (this.precision > 0) {
                return Strings.left(string, this.precision);
            }
            return string;
        }
    }

    final class Reconnector
    extends Log4jThread {
        private final CountDownLatch latch;
        private volatile boolean shutdown;

        private Reconnector() {
            super("JdbcDatabaseManager-Reconnector");
            this.latch = new CountDownLatch(1);
            this.shutdown = false;
        }

        public final void latch() {
            try {
                this.latch.await();
                return;
            }
            catch (InterruptedException interruptedException) {
                return;
            }
        }

        final void reconnect() {
            JdbcDatabaseManager.this.closeResources(false);
            JdbcDatabaseManager.this.connectAndPrepare();
            JdbcDatabaseManager.this.reconnector = null;
            this.shutdown = true;
            JdbcDatabaseManager.logger().debug("Connection reestablished to {}", (Object)JdbcDatabaseManager.this.factoryData);
        }

        @Override
        public final void run() {
            while (!this.shutdown) {
                try {
                    Reconnector.sleep(JdbcDatabaseManager.this.factoryData.reconnectIntervalMillis);
                    this.reconnect();
                }
                catch (InterruptedException | SQLException exception) {
                    JdbcDatabaseManager.logger().debug("Cannot reestablish JDBC connection to {}: {}", (Object)JdbcDatabaseManager.this.factoryData, (Object)exception.getLocalizedMessage(), (Object)exception);
                }
                finally {
                    this.latch.countDown();
                }
            }
        }

        public final void shutdown() {
            this.shutdown = true;
        }

        @Override
        public final String toString() {
            return String.format("Reconnector [latch=%s, shutdown=%s]", this.latch, this.shutdown);
        }
    }

    static final class JdbcDatabaseManagerFactory
    implements ManagerFactory<JdbcDatabaseManager, FactoryData> {
        private static final char PARAMETER_MARKER = '?';

        private JdbcDatabaseManagerFactory() {
        }

        @Override
        public final JdbcDatabaseManager createManager(String string, FactoryData factoryData) {
            StringBuilder stringBuilder = new StringBuilder("insert into ").append(factoryData.tableName).append(" (");
            JdbcDatabaseManager.appendColumnNames("INSERT", factoryData, stringBuilder);
            stringBuilder.append(") values (");
            int n = 1;
            if (factoryData.columnMappings != null) {
                for (ColumnMapping columnMapping : factoryData.columnMappings) {
                    String string2 = columnMapping.getName();
                    if (Strings.isNotEmpty(columnMapping.getLiteralValue())) {
                        JdbcDatabaseManager.logger().trace("Adding INSERT VALUES literal for ColumnMapping[{}]: {}={} ", (Object)n, (Object)string2, (Object)columnMapping.getLiteralValue());
                        stringBuilder.append(columnMapping.getLiteralValue());
                    } else if (Strings.isNotEmpty(columnMapping.getParameter())) {
                        JdbcDatabaseManager.logger().trace("Adding INSERT VALUES parameter for ColumnMapping[{}]: {}={} ", (Object)n, (Object)string2, (Object)columnMapping.getParameter());
                        stringBuilder.append(columnMapping.getParameter());
                    } else {
                        JdbcDatabaseManager.logger().trace("Adding INSERT VALUES parameter marker for ColumnMapping[{}]: {}={} ", (Object)n, (Object)string2, (Object)Character.valueOf('?'));
                        stringBuilder.append('?');
                    }
                    stringBuilder.append(',');
                    ++n;
                }
            }
            int n2 = factoryData.columnConfigs == null ? 0 : factoryData.columnConfigs.length;
            ArrayList<ColumnConfig> arrayList = new ArrayList<ColumnConfig>(n2);
            if (factoryData.columnConfigs != null) {
                for (ColumnConfig columnConfig : factoryData.columnConfigs) {
                    if (Strings.isNotEmpty(columnConfig.getLiteralValue())) {
                        stringBuilder.append(columnConfig.getLiteralValue());
                    } else {
                        stringBuilder.append('?');
                        arrayList.add(columnConfig);
                    }
                    stringBuilder.append(',');
                }
            }
            StringBuilder stringBuilder2 = stringBuilder;
            stringBuilder2.setCharAt(stringBuilder2.length() - 1, ')');
            String string3 = stringBuilder.toString();
            return new JdbcDatabaseManager(string, string3, arrayList, factoryData);
        }
    }

    static final class FactoryData
    extends AbstractDatabaseManager.AbstractFactoryData {
        private final ConnectionSource connectionSource;
        private final String tableName;
        private final ColumnConfig[] columnConfigs;
        private final ColumnMapping[] columnMappings;
        private final boolean immediateFail;
        private final boolean retry;
        private final long reconnectIntervalMillis;
        private final boolean truncateStrings;

        protected FactoryData(int n, Layout<? extends Serializable> layout, ConnectionSource connectionSource, String string, ColumnConfig[] columnConfigArray, ColumnMapping[] columnMappingArray, boolean bl, long l, boolean bl2) {
            super(n, layout);
            this.connectionSource = connectionSource;
            this.tableName = string;
            this.columnConfigs = columnConfigArray;
            this.columnMappings = columnMappingArray;
            this.immediateFail = bl;
            this.retry = l > 0L;
            this.reconnectIntervalMillis = l;
            this.truncateStrings = bl2;
        }

        public final String toString() {
            return String.format("FactoryData [connectionSource=%s, tableName=%s, columnConfigs=%s, columnMappings=%s, immediateFail=%s, retry=%s, reconnectIntervalMillis=%s, truncateStrings=%s]", this.connectionSource, this.tableName, Arrays.toString(this.columnConfigs), Arrays.toString(this.columnMappings), this.immediateFail, this.retry, this.reconnectIntervalMillis, this.truncateStrings);
        }
    }
}

