package com.paillier.algorithm;

import com.paillier.entity.Ciphertext;
import com.paillier.entity.PublicAndPrivateParams;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jessy on 2019/6/4.
 */
public class Mine {
    /**
     * to mutiply multiple cipher
     * @param cListsFromParties
     * @param params
     * @return
     */
    public static Ciphertext mine(List<Ciphertext> cListsFromParties, PublicAndPrivateParams params) {
        Ciphertext ciphertext = new Ciphertext();
        BigInteger C = BigInteger.ONE;
        for (Ciphertext c: cListsFromParties) {
            C = C.multiply(c.getCipher()).mod(params.getNsquare());
        }
        ciphertext.setCipher(C);
        return ciphertext;
    }
}
