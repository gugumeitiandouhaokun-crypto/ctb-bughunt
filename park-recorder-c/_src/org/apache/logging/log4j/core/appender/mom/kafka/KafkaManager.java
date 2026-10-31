/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.kafka.clients.producer.Callback
 *  org.apache.kafka.clients.producer.Producer
 *  org.apache.kafka.clients.producer.ProducerRecord
 *  org.apache.kafka.clients.producer.RecordMetadata
 */
package org.apache.logging.log4j.core.appender.mom.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.mom.kafka.DefaultKafkaProducerFactory;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaProducerFactory;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.util.Log4jThread;

public class KafkaManager
extends AbstractManager {
    public static final String DEFAULT_TIMEOUT_MILLIS = "30000";
    static KafkaProducerFactory producerFactory = new DefaultKafkaProducerFactory();
    private final Properties config = new Properties();
    private Producer<byte[], byte[]> producer;
    private final int timeoutMillis;
    private final String topic;
    private final String key;
    private final boolean syncSend;
    private static final KafkaManagerFactory factory = new KafkaManagerFactory();

    public KafkaManager(LoggerContext propertyArray, String string, String string2, boolean bl, Property[] propertyArray2, String string3) {
        super((LoggerContext)propertyArray, string);
        this.topic = Objects.requireNonNull(string2, "topic");
        this.syncSend = bl;
        this.config.setProperty("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        this.config.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        this.config.setProperty("batch.size", "0");
        propertyArray = propertyArray2;
        int n = propertyArray2.length;
        for (int i2 = 0; i2 < n; ++i2) {
            Property property = propertyArray[i2];
            this.config.setProperty(property.getName(), property.getValue());
        }
        this.key = string3;
        this.timeoutMillis = Integer.parseInt(this.config.getProperty("timeout.ms", DEFAULT_TIMEOUT_MILLIS));
    }

    @Override
    public boolean releaseSub(long l, TimeUnit timeUnit) {
        if (l > 0L) {
            this.closeProducer(l, timeUnit);
        } else {
            KafkaManager kafkaManager = this;
            kafkaManager.closeProducer(kafkaManager.timeoutMillis, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    private void closeProducer(long l, TimeUnit timeUnit) {
        if (this.producer != null) {
            Log4jThread log4jThread = new Log4jThread(new Runnable(){

                @Override
                public void run() {
                    if (KafkaManager.this.producer != null) {
                        KafkaManager.this.producer.close();
                    }
                }
            }, "KafkaManager-CloseThread");
            log4jThread.setDaemon(true);
            log4jThread.start();
            try {
                log4jThread.join(timeUnit.toMillis(l));
                return;
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void send(byte[] object) {
        if (this.producer != null) {
            byte[] byArray = null;
            if (this.key != null && this.key.contains("${")) {
                byArray = this.getLoggerContext().getConfiguration().getStrSubstitutor().replace(this.key).getBytes(StandardCharsets.UTF_8);
            } else if (this.key != null) {
                byArray = this.key.getBytes(StandardCharsets.UTF_8);
            }
            object = new ProducerRecord(this.topic, (Object)byArray, object);
            if (this.syncSend) {
                this.producer.send((ProducerRecord)object).get(this.timeoutMillis, TimeUnit.MILLISECONDS);
                return;
            }
            this.producer.send((ProducerRecord)object, new Callback(){

                public void onCompletion(RecordMetadata recordMetadata, Exception exception) {
                    if (exception != null) {
                        LOGGER.error("Unable to write to Kafka in appender [" + KafkaManager.this.getName() + "]", (Throwable)exception);
                    }
                }
            });
        }
    }

    public void startup() {
        this.producer = producerFactory.newKafkaProducer(this.config);
    }

    public String getTopic() {
        return this.topic;
    }

    public static KafkaManager getManager(LoggerContext loggerContext, String charSequence, String string, boolean bl, Property[] propertyArray, String string2) {
        charSequence = new StringBuilder((String)charSequence);
        Property[] propertyArray2 = propertyArray;
        int n = propertyArray.length;
        for (int i2 = 0; i2 < n; ++i2) {
            Property property = propertyArray2[i2];
            ((StringBuilder)charSequence).append(" ").append(property.getName()).append("=").append(property.getValue());
        }
        return KafkaManager.getManager(((StringBuilder)charSequence).toString(), factory, new FactoryData(loggerContext, string, bl, propertyArray, string2));
    }

    static class KafkaManagerFactory
    implements ManagerFactory<KafkaManager, FactoryData> {
        private KafkaManagerFactory() {
        }

        @Override
        public KafkaManager createManager(String string, FactoryData factoryData) {
            return new KafkaManager(factoryData.loggerContext, string, factoryData.topic, factoryData.syncSend, factoryData.properties, factoryData.key);
        }
    }

    static class FactoryData {
        private final LoggerContext loggerContext;
        private final String topic;
        private final boolean syncSend;
        private final Property[] properties;
        private final String key;

        public FactoryData(LoggerContext loggerContext, String string, boolean bl, Property[] propertyArray, String string2) {
            this.loggerContext = loggerContext;
            this.topic = string;
            this.syncSend = bl;
            this.properties = propertyArray;
            this.key = string2;
        }
    }
}

