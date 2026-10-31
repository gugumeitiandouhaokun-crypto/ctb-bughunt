/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.f.a;

import com.huawei.tools.f.a.a;
import java.net.InetAddress;

public final class b {
    public byte[] data;
    private InetAddress as;
    private int port;
    private a at;

    public b(byte[] byArray, InetAddress inetAddress, int n) {
        this.data = byArray;
        this.as = inetAddress;
        this.port = n;
        this.at = new a(inetAddress, n);
    }

    public final String toString() {
        return "Data: " + new String(this.data) + "\n From: " + this.at.aq + ":" + this.at.port + "port:";
    }
}

