/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import java.net.Socket;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.net.Rfc1349TrafficClass;
import org.apache.logging.log4j.core.net.SocketPerformancePreferences;
import org.apache.logging.log4j.core.util.Builder;

@Plugin(name="SocketOptions", category="Core", printObject=true)
public class SocketOptions
implements Cloneable,
Builder<SocketOptions> {
    @PluginBuilderAttribute
    private Boolean keepAlive;
    @PluginBuilderAttribute
    private Boolean oobInline;
    @PluginElement(value="PerformancePreferences")
    private SocketPerformancePreferences performancePreferences;
    @PluginBuilderAttribute
    private Integer receiveBufferSize;
    @PluginBuilderAttribute
    private Boolean reuseAddress;
    @PluginBuilderAttribute
    private Rfc1349TrafficClass rfc1349TrafficClass;
    @PluginBuilderAttribute
    private Integer sendBufferSize;
    @PluginBuilderAttribute
    private Integer soLinger;
    @PluginBuilderAttribute
    private Integer soTimeout;
    @PluginBuilderAttribute
    private Boolean tcpNoDelay;
    @PluginBuilderAttribute
    private Integer trafficClass;

    @PluginBuilderFactory
    public static SocketOptions newBuilder() {
        return new SocketOptions();
    }

    public void apply(Socket socket) {
        Integer n;
        if (this.keepAlive != null) {
            socket.setKeepAlive(this.keepAlive);
        }
        if (this.oobInline != null) {
            socket.setOOBInline(this.oobInline);
        }
        if (this.reuseAddress != null) {
            socket.setReuseAddress(this.reuseAddress);
        }
        if (this.performancePreferences != null) {
            this.performancePreferences.apply(socket);
        }
        if (this.receiveBufferSize != null) {
            socket.setReceiveBufferSize(this.receiveBufferSize);
        }
        if (this.soLinger != null) {
            socket.setSoLinger(true, this.soLinger);
        }
        if (this.soTimeout != null) {
            socket.setSoTimeout(this.soTimeout);
        }
        if (this.tcpNoDelay != null) {
            socket.setTcpNoDelay(this.tcpNoDelay);
        }
        if ((n = this.getActualTrafficClass()) != null) {
            socket.setTrafficClass(n);
        }
    }

    @Override
    public SocketOptions build() {
        try {
            return (SocketOptions)this.clone();
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            throw new IllegalStateException(cloneNotSupportedException);
        }
    }

    public Integer getActualTrafficClass() {
        if (this.trafficClass != null && this.rfc1349TrafficClass != null) {
            throw new IllegalStateException("You MUST not set both customTrafficClass and trafficClass.");
        }
        if (this.trafficClass != null) {
            return this.trafficClass;
        }
        if (this.rfc1349TrafficClass != null) {
            return this.rfc1349TrafficClass.value();
        }
        return null;
    }

    public SocketPerformancePreferences getPerformancePreferences() {
        return this.performancePreferences;
    }

    public Integer getReceiveBufferSize() {
        return this.receiveBufferSize;
    }

    public Rfc1349TrafficClass getRfc1349TrafficClass() {
        return this.rfc1349TrafficClass;
    }

    public Integer getSendBufferSize() {
        return this.sendBufferSize;
    }

    public Integer getSoLinger() {
        return this.soLinger;
    }

    public Integer getSoTimeout() {
        return this.soTimeout;
    }

    public Integer getTrafficClass() {
        return this.trafficClass;
    }

    public Boolean isKeepAlive() {
        return this.keepAlive;
    }

    public Boolean isOobInline() {
        return this.oobInline;
    }

    public Boolean isReuseAddress() {
        return this.reuseAddress;
    }

    public Boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }

    public SocketOptions setKeepAlive(boolean bl) {
        this.keepAlive = bl;
        return this;
    }

    public SocketOptions setOobInline(boolean bl) {
        this.oobInline = bl;
        return this;
    }

    public SocketOptions setPerformancePreferences(SocketPerformancePreferences socketPerformancePreferences) {
        this.performancePreferences = socketPerformancePreferences;
        return this;
    }

    public SocketOptions setReceiveBufferSize(int n) {
        this.receiveBufferSize = n;
        return this;
    }

    public SocketOptions setReuseAddress(boolean bl) {
        this.reuseAddress = bl;
        return this;
    }

    public SocketOptions setRfc1349TrafficClass(Rfc1349TrafficClass rfc1349TrafficClass) {
        this.rfc1349TrafficClass = rfc1349TrafficClass;
        return this;
    }

    public SocketOptions setSendBufferSize(int n) {
        this.sendBufferSize = n;
        return this;
    }

    public SocketOptions setSoLinger(int n) {
        this.soLinger = n;
        return this;
    }

    public SocketOptions setSoTimeout(int n) {
        this.soTimeout = n;
        return this;
    }

    public SocketOptions setTcpNoDelay(boolean bl) {
        this.tcpNoDelay = bl;
        return this;
    }

    public SocketOptions setTrafficClass(int n) {
        this.trafficClass = n;
        return this;
    }

    public String toString() {
        return "SocketOptions [keepAlive=" + this.keepAlive + ", oobInline=" + this.oobInline + ", performancePreferences=" + this.performancePreferences + ", receiveBufferSize=" + this.receiveBufferSize + ", reuseAddress=" + this.reuseAddress + ", rfc1349TrafficClass=" + (Object)((Object)this.rfc1349TrafficClass) + ", sendBufferSize=" + this.sendBufferSize + ", soLinger=" + this.soLinger + ", soTimeout=" + this.soTimeout + ", tcpNoDelay=" + this.tcpNoDelay + ", trafficClass=" + this.trafficClass + "]";
    }
}

