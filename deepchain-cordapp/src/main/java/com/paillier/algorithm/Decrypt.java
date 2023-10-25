package com.paillier.algorithm;

import com.paillier.entity.Ciphertext;
import com.paillier.entity.ProofCDEntity;
import com.paillier.entity.PublicAndPrivateParams;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by jessy on 2019/6/4.
 */
public class Decrypt {

    /***
     * Given a group of secret shares, it outputs a group of cipher shares.
     * @param ciphertext
     * @param secretShareMap
     * @param params
     * @return
     */
    public static Ciphertext verifiableDecryptBySecretShareList(
            Ciphertext ciphertext, Map<Integer,BigInteger> secretShareMap,
            PublicAndPrivateParams params){
        Map<Integer,BigInteger> cipherShareMap = new HashMap<>();
        Map<Integer, ProofCDEntity> CiProofMap = new HashMap<>();
        BigInteger C = ciphertext.getCipher();
        for (Map.Entry<Integer, BigInteger> secretShare : secretShareMap.entrySet()) {
            Ciphertext Ci = verifiableDecryptBySecretshare(C, secretShare.getKey(), secretShare.getValue(), params);
            cipherShareMap.put(secretShare.getKey(), Ci.getCipherShares().get(secretShare.getKey()));
            CiProofMap.put(secretShare.getKey(), Ci.getCsharesProof().get(secretShare.getKey()));
        }
        ciphertext.setCipherShares(cipherShareMap);
        ciphertext.setCsharesProof(CiProofMap);
        return ciphertext;
    }

    /**
     * verifiable version
     * @param C
     * @param shareKey
     * @param shareValue
     * @param params
     * @return
     */
    public static Ciphertext verifiableDecryptBySecretshare(BigInteger C, Integer shareKey, BigInteger shareValue,
                                                     PublicAndPrivateParams params){
        Ciphertext ciphertext = new Ciphertext();
        Map<Integer,BigInteger> CiMap = new HashMap<>();
        Map<Integer, ProofCDEntity> Ci_proof = new HashMap();
        BigInteger Ci = C.modPow(shareValue.multiply(params.getDelta().
                multiply(BigInteger.valueOf(2))), params.getNsquare());
        ProofSystem proofSystem = new ProofSystem(params.getBitLength(), params.getN());
        ProofCDEntity proofCDEntity = proofSystem.generateCDProof(C, Ci, params.getDelta(), shareValue);
        Ci_proof.put(shareKey, proofCDEntity);
        CiMap.put(shareKey, Ci);
        ciphertext.setCipher(C);
        ciphertext.setCipherShares(CiMap);
        ciphertext.setCsharesProof(Ci_proof);
        return ciphertext;
    }

    /***
     * Note that it is for the cipher which is encrypted without using alpha params.
     * Given a group of cipher shares, it recover the cleartext.
     * @param cipherShareMap
     * @param params
     * @return
     */
    public static BigInteger decryptWithoutAlphaByCipherShares(Map<Integer,BigInteger> cipherShareMap, PublicAndPrivateParams params){
        BigInteger delta = params.getDelta();
        BigInteger theta = params.getTheta();
        BigInteger nsquare = params.getNsquare();
        BigInteger n = params.getN();
        int partyNum = params.getPartyNum();
        BigInteger tempU = BigInteger.ONE;
        for (Map.Entry<Integer, BigInteger> cipherShare : cipherShareMap.entrySet()) {
            int i = cipherShare.getKey()-1;
            double miu = delta.doubleValue();
            for (int j = 0; j < partyNum; ++j) {
                if (j == i) {
                    continue;
                }
                double temp = (j + 1.0) / (double)(j - i);
                miu *= temp;
            }
            miu = miu * 2;
            BigInteger temp = cipherShare.getValue().modPow(BigInteger.valueOf((long)miu), nsquare);
            tempU = tempU.multiply(temp).mod(params.getNsquare());
        }
        tempU = funL(tempU, n);
        BigInteger daiyue = theta.multiply(delta.multiply(delta).multiply(BigInteger.valueOf(4))).modInverse(n);
        tempU = tempU.multiply(daiyue).mod(n);
        return tempU;
    }

    /***
     * It is for the cipher which is encrypted using alpha params.
     * @param cipherShareMap
     * @param alphaSplitList
     * @param params
     * @return
     */
    public static List<BigInteger> decryptWithAlphaByCipherShares(Map<Integer,BigInteger> cipherShareMap,
                       List<BigInteger> alphaSplitList, PublicAndPrivateParams params){
        BigInteger tempU = decryptWithoutAlphaByCipherShares(cipherShareMap, params);
        //pre-process alphaList
        /*List<BigInteger> alphaList_temp = new LinkedList<>();
        for(BigInteger alpha:alphaSplitList){
            alphaList_temp.add(alpha.multiply(BigInteger.valueOf(params.getPartyNum())));
        }*/
        alphaSplitList.set(0, alphaSplitList.get(0).multiply(BigInteger.valueOf(params.getPartyNum())));
        return restoreInputVector(tempU, alphaSplitList, params);
    }

    /**
     * 将通过 alpha 加到一起的向量的每个元素还原成单个向量的形式
     * @param combinedClearText
     * @param alphaSplitList
     * @param params
     * @return
     */
    private static List<BigInteger> restoreInputVector(BigInteger combinedClearText,
                                        List<BigInteger> alphaSplitList, PublicAndPrivateParams params) {
        List<BigInteger> MCombinedList = new LinkedList<>();
        List<BigInteger> MList = new LinkedList<>();
        BigInteger xi = combinedClearText;
        int size = alphaSplitList.size();
        for (int i = 1; i < size; ++i) {
            BigInteger xTemp;
            xTemp = xi.mod(alphaSplitList.get(size - i));
            MList.add(xi.subtract(xTemp).divide(alphaSplitList.get(size - i)));
            xi = xTemp;
        }
        MList.add(xi);
        Collections.reverse(MList);
        MCombinedList.addAll(MList);


        return MCombinedList;
    }

    private static BigInteger funL(BigInteger c, BigInteger n){
        return c.subtract(BigInteger.ONE).divide(n).mod(n);
    }

}
