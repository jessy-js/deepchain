package com.paillier.entity;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jessy on 2019/6/3.
 */
public class PublicAndPrivateParams {
    private int bitLength;
    private BigInteger n;
    private BigInteger m;
    private BigInteger nsquare;
    private BigInteger g;
    private BigInteger delta;
    private BigInteger theta;
    private int partyNum;
    private BigInteger secretKey;
    private Map<Integer,BigInteger> secretShare = new HashMap<>();
    private BigInteger d;

    public BigInteger getD() {
        return d;
    }

    public void setD(BigInteger d) {
        this.d = d;
    }

    public int getBitLength() {
        return bitLength;
    }

    public void setBitLength(int bitLength) {
        this.bitLength = bitLength;
    }

    public BigInteger getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(BigInteger secretKey) {
        this.secretKey = secretKey;
    }

    public Map<Integer, BigInteger> getSecretShare() {
        return secretShare;
    }

    public void setSecretShare(Map<Integer, BigInteger> secretShare) {
        this.secretShare = secretShare;
    }

    public BigInteger getM() {
        return m;
    }

    public void setM(BigInteger m) {
        this.m = m;
    }

    public BigInteger getN() {
        return n;
    }

    public void setN(BigInteger n) {
        this.n = n;
    }

    public BigInteger getNsquare() {
        return nsquare;
    }

    public void setNsquare(BigInteger nsquare) {
        this.nsquare = nsquare;
    }

    public BigInteger getG() {
        return g;
    }

    public void setG(BigInteger g) {
        this.g = g;
    }

    public BigInteger getDelta() {
        return delta;
    }

    public void setDelta(BigInteger delta) {
        this.delta = delta;
    }

    public BigInteger getTheta() {
        return theta;
    }

    public void setTheta(BigInteger theta) {
        this.theta = theta;
    }

    public int getPartyNum() {
        return partyNum;
    }

    public void setPartyNum(int partyNum) {
        this.partyNum = partyNum;
    }
}
