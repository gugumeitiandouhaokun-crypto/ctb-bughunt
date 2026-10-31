/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.f.a;

import java.net.DatagramSocket;
import java.net.InetAddress;

public final class a {
    InetAddress aq;
    int port;
    private int id;
    private DatagramSocket ar;

    public a(InetAddress inetAddress, int n) {
        this.aq = inetAddress;
        this.port = n;
        this.ar = null;
        this.id = 10;
    }
}

