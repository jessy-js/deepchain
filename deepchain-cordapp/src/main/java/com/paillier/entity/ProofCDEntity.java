package com.paillier.entity;

import java.math.BigInteger;

/**
 * Created by jessy on 2019/6/3.
 */
public class ProofCDEntity {
    private BigInteger proof_v;
    private BigInteger proof_vi;
    private BigInteger proof_a;
    private BigInteger proof_b;
    private BigInteger proof_c;
    private BigInteger proof_r;
    private BigInteger fake;

    public BigInteger getFake() {
        return fake;
    }

    public void setFake(BigInteger fake) {
        this.fake = fake;
    }

    public BigInteger getProof_v() {
        return proof_v;
    }

    public void setProof_v(BigInteger proof_v) {
        this.proof_v = proof_v;
    }

    public BigInteger getProof_vi() {
        return proof_vi;
    }

    public void setProof_vi(BigInteger proof_vi) {
        this.proof_vi = proof_vi;
    }

    public BigInteger getProof_a() {
        return proof_a;
    }

    public void setProof_a(BigInteger proof_a) {
        this.proof_a = proof_a;
    }

    public BigInteger getProof_b() {
        return proof_b;
    }

    public void setProof_b(BigInteger proof_b) {
        this.proof_b = proof_b;
    }

    public BigInteger getProof_c() {
        return proof_c;
    }

    public void setProof_c(BigInteger proof_c) {
        this.proof_c = proof_c;
    }

    public BigInteger getProof_r() {
        return proof_r;
    }

    public void setProof_r(BigInteger proof_r) {
        this.proof_r = proof_r;
    }
}
