package com.paillier.algorithm;

import com.paillier.proxyentity.ProxyParams;
import com.paillier.proxyentity.ProxyPublicParams;
import com.paillier.proxyentity.ProxySecretParams;
import com.paillier.entity.PublicAndPrivateParams;
import com.paillier.utils.PaillierUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by jessy on 2019/6/3.
 */
public class ParamsSetup {
    /**
     *
     * @param bitLength
     * @param certainty
     * @return
     */
    public static PublicAndPrivateParams generateParams(int bitLength, int certainty) {
        PublicAndPrivateParams params = new PublicAndPrivateParams();

        /*
         * Constructs two randomly generated positive BigIntegers that are
         * probably prime, with the specified bitLength and certainty.
         * BigInteger(int bitLength, int certainty, Random rnd)
         */
        BigInteger p = new BigInteger(bitLength, certainty, new Random());
        BigInteger q = new BigInteger(bitLength, certainty, new Random());
        BigInteger n = p.multiply(q);
        // p = 2p_ + 1, q = 2q_ + 1
        BigInteger p_ = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
        BigInteger q_ = q.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
        // fai_n = (p - 1)(q - 1)
        BigInteger fai_n = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // gcd(n, fai_n) == 1
        while (n.gcd(fai_n).intValue() != 1) {
            p = new BigInteger(bitLength, certainty, new Random());
            q = new BigInteger(bitLength, certainty, new Random());
            fai_n = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
            n = p.multiply(q);
        }
        BigInteger m = p_.multiply(q_);
        BigInteger nsquare = n.multiply(n);

        // g = (n + 1)^a * b^n mod n^2
        // gcd(g, n)=1
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.ONE;
        BigInteger g = (n.add(BigInteger.ONE)).modPow(a, nsquare).multiply(b.modPow(n,nsquare)).mod(nsquare);
        BigInteger beta = PaillierUtils.randomSelectZn(bitLength,n);
        BigInteger theta = a.multiply(m.multiply(beta)).mod(n);

        params.setBitLength(bitLength);
        params.setN(n);
        params.setG(g);
        params.setTheta(theta);
        params.setM(m);
        params.setNsquare(nsquare);

        BigInteger d = new BigDecimal(Double.toString(Math.pow(10, PaillierUtils.emax + 2))).toBigInteger();
        params.setD(d);

        BigInteger sk = m.multiply(beta);
        params.setSecretKey(sk);
        return params;
    }

    public static PublicAndPrivateParams generateParams() {
        int bitLength = 1024;
        int certainty = 64;
        PublicAndPrivateParams params = new PublicAndPrivateParams();

        /*
         * Constructs two randomly generated positive BigIntegers that are
         * probably prime, with the specified bitLength and certainty.
         * BigInteger(int bitLength, int certainty, Random rnd)
         */
        BigInteger p = new BigInteger(bitLength, certainty, new Random());
        BigInteger q = new BigInteger(bitLength, certainty, new Random());
        BigInteger n = p.multiply(q);
        // p = 2p_ + 1, q = 2q_ + 1
        BigInteger p_ = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
        BigInteger q_ = q.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
        // fai_n = (p - 1)(q - 1)
        BigInteger fai_n = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // gcd(n, fai_n) == 1
        while (n.gcd(fai_n).intValue() != 1) {
            p = new BigInteger(bitLength, certainty, new Random());
            q = new BigInteger(bitLength, certainty, new Random());
            fai_n = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
            n = p.multiply(q);
        }
        BigInteger m = p_.multiply(q_);
        BigInteger nsquare = n.multiply(n);

        // g = (n + 1)^a * b^n mod n^2
        // gcd(g, n)=1
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.ONE;
        BigInteger g = (n.add(BigInteger.ONE)).modPow(a, nsquare).multiply(b.modPow(n,nsquare)).mod(nsquare);
        BigInteger beta = PaillierUtils.randomSelectZn(bitLength,n);
        BigInteger theta = a.multiply(m.multiply(beta)).mod(n);

        params.setBitLength(bitLength);
        params.setN(n);
        params.setG(g);
        params.setTheta(theta);
        params.setM(m);
        params.setNsquare(nsquare);

        BigInteger d = new BigDecimal(Double.toString(Math.pow(10, PaillierUtils.emax + 2))).toBigInteger();
        params.setD(d);

        BigInteger sk = m.multiply(beta);
        params.setSecretKey(sk);
        return params;
    }

    /**
     *
     * @param bitLength
     * @param n
     * @return
     */
    public static ProxyParams generateProxyParams(int bitLength, BigInteger n) {
        ProxyPublicParams pp = new ProxyPublicParams();
        pp.setX1(PaillierUtils.randomSelectZn(bitLength, n));
        pp.setY1(PaillierUtils.randomSelectZn(bitLength, n));
        pp.setAx(PaillierUtils.randomSelectZn(bitLength, n));
        pp.setAy(PaillierUtils.randomSelectZn(bitLength, n));
        pp.setCx(PaillierUtils.randomSelectZn(bitLength, n));
        pp.setCy(PaillierUtils.randomSelectZn(bitLength, n));
        ProxySecretParams sp = new ProxySecretParams();
        sp.setRcx(PaillierUtils.randomSelectZn(bitLength, n));
        sp.setRcy(PaillierUtils.randomSelectZn(bitLength, n));
        ProxyParams params = new ProxyParams();
        params.setPp(pp);
        params.setSp(sp);
        return params;
    }

    /**
     *
     * @param params
     * @param partyNum
     * @return
     */
    public static PublicAndPrivateParams splitSecretKey(PublicAndPrivateParams params, int partyNum){
        params.setPartyNum(partyNum);
        // delta = N!
        BigInteger delta = BigInteger.valueOf(1);
        for (int i = partyNum; i >= 2; --i) {
            delta = delta.multiply(BigInteger.valueOf(i));
        }
        params.setDelta(delta);

        BigInteger range = params.getN().multiply(params.getM()).subtract(BigInteger.ONE);
        BigInteger secretKey = params.getSecretKey();
        int threshold = partyNum/3+1;
        List<BigInteger> coefficients = generateCoefficients(params.getBitLength(), secretKey, threshold, range);
        Map<Integer,BigInteger> secretShare = new HashMap<>();
        for (int i = 1; i <= partyNum; ++i) {
            BigInteger rst = BigInteger.ZERO;
            // result = a(0)x^0 + ... + a(t)x^t
            for (int j = 0; j < coefficients.size(); ++j) {
                BigInteger temp = BigInteger.valueOf(i).pow(j);
                rst = rst.add(coefficients.get(j).multiply(temp));
            }
            secretShare.put(i,rst.mod(range.add(BigInteger.ONE)));
        }
        params.setSecretShare(secretShare);
        return  params;
    }

    /**
     *
     * @param bitLength
     * @param secretKey
     * @param threshold
     * @param range
     * @return
     */
    private static List<BigInteger> generateCoefficients(int bitLength,
                              BigInteger secretKey, int threshold, BigInteger range){
        List<BigInteger> coefficients = new LinkedList<>();
        coefficients.add(secretKey);
        for (int i = 0; i < threshold; ++i) {
            BigInteger coff = new BigInteger(range.add(BigInteger.ONE).bitLength(), new Random());
            while (coff.compareTo(range.add(BigInteger.ONE)) >= 0) {
                coff = new BigInteger(bitLength, new Random());
            }
            coefficients.add(coff);
        }
        return coefficients;
    }

}
