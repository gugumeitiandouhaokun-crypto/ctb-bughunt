/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.f.a;

import com.huawei.tools.f.a.a;
import java.net.InetAddress;

public final class b {
    public byte[] data;
    private InetAddress aY;
    private int port;
    private a aZ;

    public b(byte[] byArray, InetAddress inetAddress, int n) {
        this.data = byArray;
        this.aY = inetAddress;
        this.port = n;
        this.aZ = new a(inetAddress, n);
    }

    public final String toString() {
        return "Data: " + new String(this.data) + "\n From: " + this.aZ.aW + ":" + this.aZ.port + "port:";
    }
}

