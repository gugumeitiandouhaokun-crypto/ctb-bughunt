/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.f.a;

import java.net.DatagramSocket;
import java.net.InetAddress;

public final class a {
    InetAddress aW;
    int port;
    private int id;
    private DatagramSocket aX;

    public a(InetAddress inetAddress, int n) {
        this.aW = inetAddress;
        this.port = n;
        this.aX = null;
        this.id = 10;
    }
}

