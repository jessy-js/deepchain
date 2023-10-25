package com.paillier.algorithm;

import com.paillier.entity.Ciphertext;
import com.paillier.entity.ProofPKEntity;
import com.paillier.entity.PublicAndPrivateParams;
import com.paillier.utils.PaillierUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by jessy on 2019/6/3.
 */
public class Encrypt {

    /**
     *
     * @param proCleartext
     * @param gSplitList
     * @param params
     * @return
     */
    public static Ciphertext encryptWithAlpha(List<BigInteger> proCleartext, List<BigInteger> gSplitList, PublicAndPrivateParams params){
        Ciphertext ciphertext = new Ciphertext();
        /*List<BigInteger> gSplitList = PaillierUtils.calculateGSplitList(alphaSplitList, params);*/
        BigInteger nsquare = params.getNsquare();
        // generate cipher text
        int j = 0;
        BigInteger cj = BigInteger.ONE;
        for (BigInteger gElem: gSplitList) {
            cj = cj.multiply(gElem.modPow(proCleartext.get(j), nsquare)).mod(nsquare);
            ++j;
        }
        BigInteger rj = PaillierUtils.randomSelectZn(params.getBitLength(),params.getN());
        cj = cj.multiply(rj.modPow(params.getN(), nsquare)).mod(nsquare);
        ciphertext.setCipher(cj);
        return ciphertext;
    }

    /**
     *
     * @param proCleartext
     * @param alphaSplitList
     * @param gSplitList
     * @param params
     * @return
     */
    public static Ciphertext verifiableEncryptWithAlpha(List<BigInteger> proCleartext, List<BigInteger> alphaSplitList,
                                                 List<BigInteger> gSplitList, PublicAndPrivateParams params){
        ProofSystem proofSystem = new ProofSystem(params.getBitLength(), params.getN());
        Ciphertext ciphertext = new Ciphertext();
        BigInteger nsquare = params.getNsquare();
        // generate cipher text
        int j = 0;
        BigInteger cj = BigInteger.ONE;
        for (BigInteger gElem: gSplitList) {
            cj = cj.multiply(gElem.modPow(proCleartext.get(j), nsquare)).mod(nsquare);
            ++j;
        }
        BigInteger rj = PaillierUtils.randomSelectZn(params.getBitLength(),params.getN());
        cj = cj.multiply(rj.modPow(params.getN(), nsquare)).mod(nsquare);
        ProofPKEntity proofPKEntity = proofSystem.generatePKProof(cj,params.getG(),PaillierUtils.proofHelper(alphaSplitList, proCleartext, params),rj);
        ciphertext.setProofPKEntity(proofPKEntity);
        ciphertext.setCipher(cj);
        return ciphertext;
    }

    /**
     *
     * @param cleartext
     * @param params
     * @return
     */
    public static List<Ciphertext> encryptWithoutAlpha(List<BigInteger> cleartext,PublicAndPrivateParams params){
        List<Ciphertext> ciphertextList = new LinkedList<>();
        for(BigInteger ct : cleartext) {
            Ciphertext ciphertext = new Ciphertext();
            BigInteger g = params.getG();
            BigInteger nsquare = params.getNsquare();
            BigInteger r = PaillierUtils.randomSelectZn(params.getBitLength(), params.getN());
            BigInteger c = g.modPow(ct, nsquare).multiply(r.modPow(params.getN(), nsquare)).mod(nsquare);
            ciphertext.setCipher(c);
            ciphertextList.add(ciphertext);
        }
        return ciphertextList;
    }

    /**
     *
     * @param cleartext
     * @param params
     * @return
     */
    public static List<Ciphertext> verifiableEncryptWithoutAlpha(List<BigInteger> cleartext, PublicAndPrivateParams params){
        ProofSystem proofSystem = new ProofSystem(params.getBitLength(), params.getN());

        List<Ciphertext> ciphertextList = new LinkedList<>();
        for(BigInteger ct : cleartext) {
            Ciphertext ciphertext = new Ciphertext();
            BigInteger g = params.getG();
            BigInteger nsquare = params.getNsquare();
            BigInteger r = PaillierUtils.randomSelectZn(params.getBitLength(), params.getN());
            BigInteger c = g.modPow(ct, nsquare).multiply(r.modPow(params.getN(), nsquare)).mod(nsquare);
            ciphertext.setCipher(c);
            ProofPKEntity proofPKEntity = proofSystem.generatePKProof(c,g,ct,r);
            ciphertext.setProofPKEntity(proofPKEntity);
            ciphertextList.add(ciphertext);
        }
        return ciphertextList;
    }

}
