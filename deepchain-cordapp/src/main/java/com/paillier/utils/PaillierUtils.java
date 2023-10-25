package com.paillier.utils;

import com.paillier.entity.PublicAndPrivateParams;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by lenovo on 2019/6/4.
 */
public class PaillierUtils {
    public static int emax = 24;
//    private static int alphaLen = 10;

    public static BigInteger randomSelectZn(int bitLength, BigInteger n) {
        BigInteger randomZn = new BigInteger(bitLength, new Random());
        while (randomZn.compareTo(n) >= 0 || randomZn.gcd(n).intValue() != 1) {
            randomZn = new BigInteger(bitLength, new Random());
        }
        return randomZn;
    }

    public static List<BigInteger> cleartextPreProcess(List<BigDecimal> cleartextList){

        // turn the vector to integer
        List<BigInteger> proCleartext = new LinkedList<>();
        for (BigDecimal bd: cleartextList) {
            // to balance cost and performance
            // some small numbers will be ignored
            if (bd.scale() > emax) {
                bd = bd.setScale(emax, RoundingMode.HALF_UP);
            }
            // to make sure that the result is positive,
            // so that the algorithm works well,
            // ten is added
            BigInteger toAdd = (bd.add(BigDecimal.TEN))
                    .multiply(new BigDecimal(Double.toString(Math.pow(10, emax)))).toBigInteger();
            proCleartext.add(toAdd);
        }
        return proCleartext;
    }

    public static List<BigDecimal> cleartextProProcess(List<BigInteger> cleartextList, PublicAndPrivateParams params) {

        List<BigDecimal> decimalCombinedList = new LinkedList<>();
        for (BigInteger MElem: cleartextList) {
            BigDecimal tempDecimal = new BigDecimal(MElem);
            // subtract 10 * N from the result
            decimalCombinedList.add(tempDecimal.divide(new BigDecimal(Double.toString(Math.pow(10, PaillierUtils.emax))))
            .subtract(BigDecimal.TEN.multiply(BigDecimal.valueOf(params.getPartyNum()))));
        }
        return decimalCombinedList;
    }

    public static List<BigInteger> generateAlphaList(PublicAndPrivateParams params, int alphaLen) {
        // generate alpha vector
        List<BigInteger> alphaList = new LinkedList<>();
        alphaList.add(BigInteger.ONE);
        BigInteger addAlpha = params.getD().multiply(BigInteger.valueOf(params.getPartyNum()));
        for (int i = 2; i <= alphaLen; ++i) {
            addAlpha = chooseAlpha(addAlpha, params.getD(), alphaList, params);
            if (addAlpha.compareTo(params.getN()) > 0) {
                System.out.println("alphaLen 选取不当");
            }
            System.out.println("Generating alpha_" + i + "/" + alphaLen);
        }
        return alphaList;
    }

    public static List<BigInteger> calculateGList(List<BigInteger> alphaList, PublicAndPrivateParams params) {
        List<BigInteger> gList = new LinkedList<>();
        for (BigInteger alphaElem: alphaList) {
            gList.add(params.getG().modPow(alphaElem, params.getNsquare()));
            System.out.println("Generating g");
        }
        return gList;
    }

    private static BigInteger chooseAlpha(BigInteger addAlpha, BigInteger d, List<BigInteger> alphaList,PublicAndPrivateParams params) {

        BigInteger alpha;
        if (addAlpha.bitLength() < 256) {
            alpha = new BigInteger(256, 64, new Random());
        } else {
            alpha = addAlpha.nextProbablePrime();
        }
//        System.out.println("alpha: " + alpha);

        addAlpha = d.multiply(BigInteger.valueOf(params.getPartyNum())).multiply(alpha).add(addAlpha);

        alphaList.add(alpha);
        return addAlpha;
    }

    public static BigInteger proofHelper(List<BigInteger> alphaList, List<BigInteger> cleartextList, PublicAndPrivateParams params){
        BigInteger result = BigInteger.ZERO;
        for(int i=0; i<alphaList.size(); i++){
            result = alphaList.get(i).multiply(cleartextList.get(i)).mod(params.getNsquare()).add(result).mod(params.getNsquare());
        }
        return result;
    }

}
