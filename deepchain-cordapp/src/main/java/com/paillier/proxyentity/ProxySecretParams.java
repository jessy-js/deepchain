package com.paillier.proxyentity;

import java.math.BigInteger;

/**
 * Created by jessy on 2019/6/3.
 */
public class ProxySecretParams {
    private BigInteger rcx;
    private BigInteger rcy;

    public BigInteger getRcx() {
        return rcx;
    }

    public void setRcx(BigInteger rcx) {
        this.rcx = rcx;
    }

    public BigInteger getRcy() {
        return rcy;
    }

    public void setRcy(BigInteger rcy) {
        this.rcy = rcy;
    }
}
