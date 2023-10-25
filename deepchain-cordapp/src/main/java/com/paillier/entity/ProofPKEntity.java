package com.paillier.entity;

import java.math.BigInteger;

/**
 * Created by jessy on 2019/6/3.
 */
public class ProofPKEntity {
    private BigInteger proof_N;
    private BigInteger proof_a;
    private BigInteger proof_u;
    private BigInteger proof_d;
    private BigInteger proof_e;
    private BigInteger proof_c;
    private BigInteger proof_upA;
    private BigInteger gElem;

    public BigInteger getProof_N() {
        return proof_N;
    }

    public void setProof_N(BigInteger proof_N) {
        this.proof_N = proof_N;
    }

    public BigInteger getProof_a() {
        return proof_a;
    }

    public void setProof_a(BigInteger proof_a) {
        this.proof_a = proof_a;
    }

    public BigInteger getProof_u() {
        return proof_u;
    }

    public void setProof_u(BigInteger proof_u) {
        this.proof_u = proof_u;
    }

    public BigInteger getProof_d() {
        return proof_d;
    }

    public void setProof_d(BigInteger proof_d) {
        this.proof_d = proof_d;
    }

    public BigInteger getProof_e() {
        return proof_e;
    }

    public void setProof_e(BigInteger proof_e) {
        this.proof_e = proof_e;
    }

    public BigInteger getProof_c() {
        return proof_c;
    }

    public void setProof_c(BigInteger proof_c) {
        this.proof_c = proof_c;
    }

    public BigInteger getProof_upA() {
        return proof_upA;
    }

    public void setProof_upA(BigInteger proof_upA) {
        this.proof_upA = proof_upA;
    }

    public BigInteger getgElem() {
        return gElem;
    }

    public void setgElem(BigInteger gElem) {
        this.gElem = gElem;
    }
}
