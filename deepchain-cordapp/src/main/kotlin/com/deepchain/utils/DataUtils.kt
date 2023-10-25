package com.deepchain.utils

import com.deepchain.contracts.PublicAndPaivateParamsContract
import com.paillier.entity.Ciphertext
import com.paillier.entity.ProofCDEntity
import com.paillier.entity.ProofPKEntity
import com.paillier.entity.PublicAndPrivateParams
import java.math.BigInteger

object DataUtils {
    /**
     * 将压缩成 list 的参数还原成 Param 对象
     */
    fun restorePaillierParam(stateDataP: PublicAndPaivateParamsContract.State): PublicAndPrivateParams {
        val params = PublicAndPrivateParams()
        val paramList = stateDataP.paramList
        params.n = paramList[0].toBigInteger()
        params.nsquare = paramList[1].toBigInteger()
        params.g = paramList[2].toBigInteger()
        params.delta = paramList[3].toBigInteger()
        params.theta = paramList[4].toBigInteger()
        params.bitLength = paramList[5].toInt()
        params.partyNum = paramList[6].toInt()

        params.secretKey = stateDataP.privateKey.toBigInteger()

        return params
    }

    /**
     * 将 ciphertext 压缩成 list
     */
    fun cipherText2BigIntegerList(ciphertext: Ciphertext): List<BigInteger> {
        val param = mutableListOf<BigInteger>()
        param.add(ciphertext.cipher)
        if (ciphertext.proofPKEntity != null) {
            param.add(ciphertext.proofPKEntity.proof_N)
            param.add(ciphertext.proofPKEntity.proof_a)
            param.add(ciphertext.proofPKEntity.proof_u)
            param.add(ciphertext.proofPKEntity.proof_d)
            param.add(ciphertext.proofPKEntity.proof_e)
            param.add(ciphertext.proofPKEntity.proof_c)
            param.add(ciphertext.proofPKEntity.proof_upA)
            param.add(ciphertext.proofPKEntity.getgElem())
        }
        else {
            for (i in 0 until 8) {
                param.add(BigInteger.ZERO)
            }
        }

        return param
    }

    /**
     * 将 ciphertext 从 list 还原
     */
    fun bigIntegerList2cipherText(bigIntegerList: List<BigInteger>): Ciphertext {
        val ciphertext = Ciphertext()
        ciphertext.cipher = bigIntegerList[0]

        val proofPKEntity = ProofPKEntity()
        proofPKEntity.proof_N = bigIntegerList[1]
        proofPKEntity.proof_a = bigIntegerList[2]
        proofPKEntity.proof_u = bigIntegerList[3]
        proofPKEntity.proof_d = bigIntegerList[4]
        proofPKEntity.proof_e = bigIntegerList[5]
        proofPKEntity.proof_c = bigIntegerList[6]
        proofPKEntity.proof_upA = bigIntegerList[7]
        proofPKEntity.setgElem(bigIntegerList[8])
        ciphertext.proofPKEntity = proofPKEntity

        return ciphertext
    }

    /**
     * 将 share 压缩到 list
     */
    fun shareCipher2BigIntegerList(ciphertext: Ciphertext): List<BigInteger> {
        val param = mutableListOf<BigInteger>()
        param.add(ciphertext.cipherShares[1]!!)
        if (ciphertext.csharesProof[1] != null) {
            param.add(ciphertext.csharesProof[1]!!.proof_v)
            param.add(ciphertext.csharesProof[1]!!.proof_vi)
            param.add(ciphertext.csharesProof[1]!!.proof_a)
            param.add(ciphertext.csharesProof[1]!!.proof_b)
            param.add(ciphertext.csharesProof[1]!!.proof_c)
            param.add(ciphertext.csharesProof[1]!!.proof_r)
            param.add(ciphertext.csharesProof[1]!!.fake)
        }
        else {
            for (i in 0 until 8) {
                param.add(BigInteger.ZERO)
            }
        }

        return param
    }

    /**
     * 将 share 从 list 还原
     */
    fun bigIntegerList2cipherShare(bigIntegerList: List<BigInteger>): Ciphertext {
        val ciphertext = Ciphertext()
        ciphertext.cipherShares = hashMapOf(Pair(1, bigIntegerList[0]))

        val csharesProof = mutableMapOf(Pair(1, ProofCDEntity()))
        csharesProof[1]!!.proof_v = bigIntegerList[1]
        csharesProof[1]!!.proof_vi = bigIntegerList[2]
        csharesProof[1]!!.proof_a = bigIntegerList[3]
        csharesProof[1]!!.proof_b = bigIntegerList[4]
        csharesProof[1]!!.proof_c = bigIntegerList[5]
        csharesProof[1]!!.proof_r = bigIntegerList[6]
        csharesProof[1]!!.fake = bigIntegerList[7]
        ciphertext.csharesProof = csharesProof

        return ciphertext
    }
}