package com.paillier.proxyentity;

import java.math.BigInteger;

/**
 * Created by jessy on 2019/6/3.
 */
public class ProxyPublicParams {
    private BigInteger x1;
    private BigInteger y1;
    private BigInteger ax;
    private BigInteger ay;
    private BigInteger cx;
    private BigInteger cy;

    public BigInteger getX1() {
        return x1;
    }

    public void setX1(BigInteger x1) {
        this.x1 = x1;
    }

    public BigInteger getY1() {
        return y1;
    }

    public void setY1(BigInteger y1) {
        this.y1 = y1;
    }

    public BigInteger getAx() {
        return ax;
    }

    public void setAx(BigInteger ax) {
        this.ax = ax;
    }

    public BigInteger getAy() {
        return ay;
    }

    public void setAy(BigInteger ay) {
        this.ay = ay;
    }

    public BigInteger getCx() {
        return cx;
    }

    public void setCx(BigInteger cx) {
        this.cx = cx;
    }

    public BigInteger getCy() {
        return cy;
    }

    public void setCy(BigInteger cy) {
        this.cy = cy;
    }
}
