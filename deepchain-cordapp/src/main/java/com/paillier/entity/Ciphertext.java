package com.paillier.entity;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by jessy on 2019/6/3.
 */
public class Ciphertext {
    private BigInteger cipher;
    private Map<Integer,BigInteger> cipherShares;
    private Map<Integer,ProofCDEntity> csharesProof;
    public BigInteger getCipher() {
        return cipher;
    }
    private ProofPKEntity proofPKEntity;

    public ProofPKEntity getProofPKEntity() {
        return proofPKEntity;
    }

    public void setProofPKEntity(ProofPKEntity proofPKEntity) {
        this.proofPKEntity = proofPKEntity;
    }

    public void setCipher(BigInteger cipher) {
        this.cipher = cipher;
    }

    public Map<Integer, BigInteger> getCipherShares() {
        return cipherShares;
    }

    public void setCipherShares(Map<Integer, BigInteger> cipherShares) {
        this.cipherShares = cipherShares;
    }

    public Map<Integer, ProofCDEntity> getCsharesProof() {
        return csharesProof;
    }

    public void setCsharesProof(Map<Integer, ProofCDEntity> csharesProof) {
        this.csharesProof = csharesProof;
    }
}
