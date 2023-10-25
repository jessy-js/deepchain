import com.paillier.algorithm.*;
import com.paillier.entity.Ciphertext;
import com.paillier.entity.ProofCDEntity;
import com.paillier.entity.PublicAndPrivateParams;
import com.paillier.utils.FileUtils;
import com.paillier.utils.PaillierUtils;
import com.paillier.utils.WashVector;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by jessy on 2019/6/4.
 */
public class Test {

    public static int alphaLen = 20;

    public static void main(String[] args) {
        int partyNum = 4;
        int iterationRound = 4;
        /*testWithoutAlpha();*/
        evaluateOnRealdata(partyNum,iterationRound);
//        evaluateEncryption(partyNum);
    }

    public static void evaluateOnRealdata(int partyNum,int iterationRound){

        List<List<List<BigDecimal>>> vectorList = new LinkedList<>();
        WashVector wv = new WashVector("C:\\Users\\lenovo\\Desktop\\deepchain-kotlin\\data\\worker 6\\worker 1.txt");
        vectorList.add(wv.wash(20));
        WashVector wv2 = new WashVector("C:\\Users\\lenovo\\Desktop\\deepchain-kotlin\\data\\worker 6\\worker 2.txt");
        vectorList.add(wv2.wash(20));
        WashVector wv3 = new WashVector("C:\\Users\\lenovo\\Desktop\\deepchain-kotlin\\data\\worker 6\\worker 3.txt");
        vectorList.add(wv3.wash(20));
        WashVector wv4 = new WashVector("C:\\Users\\lenovo\\Desktop\\deepchain-kotlin\\data\\worker 6\\worker 4.txt");
        vectorList.add(wv4.wash(20));

        for(int dataType = 0; dataType < vectorList.get(0).size(); dataType++){
            List<BigDecimal> bdsList = vectorList.get(0).get(dataType);
            System.out.println("cleartext size " + bdsList.size());
            List<List<BigDecimal>> mListsFromParties = new LinkedList<>();
            for (int i = 0; i < partyNum; i++) {
                // 测试用，为了避免每个 party 数据一样，打乱数组
//                Collections.shuffle(bdsList);
//                List<BigDecimal> tempList = new LinkedList<>(bdsList);
//                tempList.addAll(bdsList);
                mListsFromParties.add(vectorList.get(i).get(dataType));
            }

            List<BigDecimal> resultList = new LinkedList<>(
                    evaluate(mListsFromParties, partyNum, iterationRound, bdsList.size()));

            System.out.println("result size "+ resultList.size());
//            String resultString = wv.unwash(resultList, dataType);
//            wv.writeDataToFile(resultString, dataType);
        }
    }

    public static void evaluateEncryption(int partyNum) {
        WashVector wv = new WashVector("C:\\Users\\lenovo\\Desktop\\" +
                "deepchain-kotlin\\data\\worker 6\\worker 4.txt");
        String savePath = FileUtils.savedFileBasePath();
        List<List<BigDecimal>> vector = wv.wash(20);
        String identifier = "Old";
        long encryptTime = 0;
        long paraGenTime;

        int i = 1;
        for (List<BigDecimal> bdsList: vector) {
            System.out.println("加密轮次：" + i  + "/" + vector.size());
            i++;
            if (i > 10) break;
            //setup params
            PublicAndPrivateParams params;
            long paraGenStartTime = System.currentTimeMillis();
            if (!FileUtils.isFileExists(savePath, "publicParams", partyNum, alphaLen, identifier) &&
                    !FileUtils.isFileExists(savePath, "secretParams", partyNum, alphaLen, identifier)) {
                params = ParamsSetup.generateParams(1024, 64);
                params = ParamsSetup.splitSecretKey(params, partyNum);

                FileUtils.savePublicParams(params, savePath, alphaLen, identifier);
                FileUtils.saveSecretParams(params, savePath, alphaLen, identifier);
            } else {
                params = FileUtils.loadParams(savePath, partyNum, alphaLen, identifier);
            }
            paraGenTime = System.currentTimeMillis() - paraGenStartTime;
            System.out.println("生成/读取参数所用总时间：" + paraGenTime + "ms");

            //pre-process cleartext
            List<BigInteger> preCleartext = PaillierUtils.cleartextPreProcess(bdsList);

            //encrypt with alpha
            List<BigInteger> alphaSplitList;
            List<BigInteger> gSplitList;
            if (!FileUtils.isFileExists(savePath, "alphaSplitList", partyNum, alphaLen, identifier) &&
                    !FileUtils.isFileExists(savePath, "gSplitList", partyNum, alphaLen, identifier)) {

                alphaSplitList = PaillierUtils.generateAlphaList(params, alphaLen);
                gSplitList = PaillierUtils.calculateGList(alphaSplitList, params);
                FileUtils.saveAlphaParams(alphaSplitList, savePath, partyNum, alphaLen, identifier);
                FileUtils.saveGParams(gSplitList, savePath, partyNum, alphaLen, identifier);

            } else {
                alphaSplitList = FileUtils.loadAlphaList(savePath, partyNum, alphaLen, identifier);
                gSplitList = FileUtils.loadgSplitList(savePath, partyNum, alphaLen, identifier);
            }

            long encryptStartTiCCme = System.currentTimeMillis();
            Ciphertext ciphertext = Encrypt.verifiableEncryptWithAlpha(preCleartext, alphaSplitList, gSplitList, params);
            System.out.println(ciphertext.toString());
            encryptTime += (System.currentTimeMillis() - encryptStartTiCCme);
            long tempTime = System.currentTimeMillis() - encryptStartTiCCme;
            System.out.println("每组加密所用时间：" + tempTime + "ms");
        }
        System.out.println("加密所用总时间：" + encryptTime + "ms");

        try {
            BufferedWriter out;
            out = new BufferedWriter(new FileWriter(savePath + "/encrypt_time.txt", true));
            FileUtils.writeData(out, "加密所用总时间：", encryptTime + "ms");
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<BigDecimal> evaluate(List<List<BigDecimal>> mListsFromParties, int partyNum,
                                            int iterationRound, int dataLength) {
        String savePath = FileUtils.savedFileBasePath();
        String identifier = "Old";
        long encryptTime = 0;
        long mineTime = 0;
        long decryptTime = 0;
        long paraGenTime;
        int alphaLen = mListsFromParties.get(0).size();
        String fileName = savePath + "/result_" + iterationRound + "_" + dataLength + "_" + identifier;

        //setup params
        PublicAndPrivateParams params;
        long paraGenStartTime = System.currentTimeMillis();
        if (!FileUtils.isFileExists(savePath, "publicParams", partyNum, alphaLen, identifier) &&
                !FileUtils.isFileExists(savePath, "secretParams", partyNum, alphaLen, identifier)) {
            params = ParamsSetup.generateParams(1024, 64);
            params = ParamsSetup.splitSecretKey(params, partyNum);

            FileUtils.savePublicParams(params, savePath, alphaLen, identifier);
            FileUtils.saveSecretParams(params, savePath, alphaLen, identifier);
        } else {
            params = FileUtils.loadParams(savePath, partyNum, alphaLen, identifier);
        }
        paraGenTime = System.currentTimeMillis() - paraGenStartTime;


        List<List<BigInteger>> premListsFromParties = new LinkedList<>();
        for (int i = 0; i < partyNum; i++) {
            //pre-process cleartext
            List<BigInteger> preCleartext = PaillierUtils.cleartextPreProcess(mListsFromParties.get(i));
            premListsFromParties.add(preCleartext);
        }

        //generate alpha
        List<BigInteger> alphaSplitList;
        List<BigInteger> gSplitList;
        if (!FileUtils.isFileExists(savePath, "alphaSplitList", partyNum, alphaLen, identifier) &&
                !FileUtils.isFileExists(savePath, "gSplitList", partyNum, alphaLen, identifier)) {

            alphaSplitList = PaillierUtils.generateAlphaList(params, alphaLen);
            gSplitList = PaillierUtils.calculateGList(alphaSplitList, params);
            FileUtils.saveAlphaParams(alphaSplitList, savePath, partyNum, alphaLen, identifier);
            FileUtils.saveGParams(gSplitList, savePath, partyNum, alphaLen, identifier);

        } else {
            alphaSplitList = FileUtils.loadAlphaList(savePath, partyNum, alphaLen, identifier);
            gSplitList = FileUtils.loadgSplitList(savePath, partyNum, alphaLen, identifier);
        }

        ProofSystem proofSystem = new ProofSystem(params.getBitLength(), params.getN());
        List<Ciphertext> ciphertextList = new LinkedList<>();
        //encrypt with alpha
        for (int i=0; i< partyNum; i++) {
            long encryptStartTiCCme = System.currentTimeMillis();
            Ciphertext ciphertext = Encrypt.verifiableEncryptWithAlpha(premListsFromParties.get(i), alphaSplitList, gSplitList, params);
            ciphertextList.add(ciphertext);
            encryptTime += (System.currentTimeMillis() - encryptStartTiCCme);
            //verify
            boolean verifyPKResult = proofSystem.verifyPKProof(ciphertext.getProofPKEntity(), ciphertext.getCipher());
            System.out.println("verifyPKResult " + verifyPKResult);
        }

        long mineStartTime = System.currentTimeMillis();
        //mine
        Ciphertext ciphertext = Mine.mine(ciphertextList, params);
        mineTime += (System.currentTimeMillis() - mineStartTime);

        long decryptStartTime = System.currentTimeMillis();
        //decrypt
        Map<Integer,BigInteger> secretShareMap = params.getSecretShare();
        Map<Integer,BigInteger> cipherShareMap = new HashMap<>();
//                Decrypt.verifiableDecryptBySecretShareList(ciphertext, secretShareMap, params).getCipherShares();

        Map<Integer,BigInteger> cipherShareMap0 =
                Decrypt.verifiableDecryptBySecretShareList(ciphertext, secretShareMap, params).getCipherShares();

        for (int i = 0; i < partyNum; i++) {
            Ciphertext Ci = Decrypt.verifiableDecryptBySecretshare(ciphertext.getCipher(), i + 1, params.getSecretShare().get(i + 1), params);
            cipherShareMap.put(i + 1, Ci.getCipherShares().get(i + 1));
        }

        //verify CD
//        Map<Integer, ProofCDEntity> cipherShareProof = ciphertext.getCsharesProof();
//        for(int i =1; i<=ciphertextList.size(); i++){
//            boolean verifyCDResult = proofSystem.verifyCDProof(cipherShareProof.get(i));
//            System.out.println("verify CD result " + verifyCDResult);
//        }
        List<BigInteger> returnClearText = Decrypt.decryptWithAlphaByCipherShares(cipherShareMap,alphaSplitList, params);
        decryptTime += (System.currentTimeMillis() - decryptStartTime);

        //pro-process cleartext
        List<BigDecimal> decimalCombinedList = PaillierUtils.cleartextProProcess(returnClearText, params);
        /*System.out.println("cleartext: " );
        for(BigDecimal ct : decimalCombinedList){
            System.out.println(ct);
        }*/

        try {
            BufferedWriter out;
            out = new BufferedWriter(new FileWriter(fileName, true));
            FileUtils.writeData(out, "生成参数所用总时间：", paraGenTime + "ms");
            FileUtils.writeData(out, "加密所用总时间：", encryptTime + "ms");
            FileUtils.writeData(out, "密文加法所用总时间：", mineTime + "ms");
            FileUtils.writeData(out, "解密所用总时间：", decryptTime + "ms");
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return decimalCombinedList;
    }

    public static void testWithoutAlpha(){
        int partyNum = 10;
        ParamsSetup paramsSetup = new ParamsSetup();
        PublicAndPrivateParams params = paramsSetup.generateParams(512,64);
        params = paramsSetup.splitSecretKey(params, partyNum);

        /*cleartextList*/
        List<BigDecimal> cleartext = new LinkedList<>();
        cleartext.add(BigDecimal.valueOf(0.04680815));
        cleartext.add(BigDecimal.valueOf(0.02367513));
        cleartext.add(BigDecimal.valueOf(0.05825796));
        cleartext.add(BigDecimal.valueOf(0.03640306));
        cleartext.add(BigDecimal.valueOf(-0.02749515));
        cleartext.add(BigDecimal.valueOf(-0.03735155));
        cleartext.add(BigDecimal.valueOf(-0.02584232));
        cleartext.add(BigDecimal.valueOf(-0.03270825));
        cleartext.add(BigDecimal.valueOf(-0.00393088));
        //pre-process cleartext
        List<BigInteger> proCleartext = PaillierUtils.cleartextPreProcess(cleartext);
        //encrypt without alpha
        Encrypt encrypt = new Encrypt();
        /*List<Ciphertext> ciphertextList = encrypt.encryptWithoutAlpha(proCleartext, params);*/

        ProofSystem proofSystem = new ProofSystem(params.getBitLength(), params.getN());
        List<Ciphertext> ciphertextList;
        ciphertextList = encrypt.verifiableEncryptWithoutAlpha(proCleartext, params);
        for (Ciphertext c: ciphertextList){
            boolean verifyPKResult = proofSystem.verifyPKProof(c.getProofPKEntity(), c.getCipher());
        }

        //decrypt
        Decrypt decrypt = new Decrypt();
        Ciphertext temp_c = decrypt.verifiableDecryptBySecretShareList(ciphertextList.get(0), params.getSecretShare(), params);
        Map<Integer,BigInteger> cipherShares = temp_c.getCipherShares();
        Map<Integer, ProofCDEntity> cipherShareProof = temp_c.getCsharesProof();
        for(int i =1; i<=ciphertextList.size(); i++){
            boolean verifyCDResult = proofSystem.verifyCDProof(cipherShareProof.get(Integer.valueOf(i)));
            System.out.println("verify CD result " + verifyCDResult);
        }
        BigInteger returnCleartext = decrypt.decryptWithoutAlphaByCipherShares(cipherShares, params);

        List<BigInteger> returnCleartextList = new LinkedList<>();
        returnCleartextList.add(returnCleartext);

        List<BigDecimal> finalCleartext = PaillierUtils.cleartextProProcess(returnCleartextList, params);
        System.out.println("final cleartext: " );
        for(BigDecimal ct : finalCleartext){
            System.out.println(ct);
        }
    }

    public static void testWithAlpha(){
        ParamsSetup paramsSetup = new ParamsSetup();
        PublicAndPrivateParams params = paramsSetup.generateParams(1024,64);
        int partyNum = 4;
        params = paramsSetup.splitSecretKey(params, partyNum);

        /*cleartextVector: <>*/
        WashVector wv1 = new WashVector(4, 1);
        List<List<BigDecimal>> vector1 = wv1.wash();
        List<BigDecimal> cleartext = vector1.get(1);
        //pre-process cleartext
        List<BigInteger> preCleartext = PaillierUtils.cleartextPreProcess(cleartext);

        //used to encrypt and decrypt
        Encrypt encrypt = new Encrypt();
        List<BigInteger> alphaSplitList = PaillierUtils.generateAlphaList(params, 10);
        List<BigInteger> gSplitList = PaillierUtils.calculateGList(alphaSplitList, params);
        //mine
        List<Ciphertext> ciphertextList = new LinkedList<>();
        Mine mine = new Mine();
        //encrypt with alpha
        for (int i=0; i< partyNum; i++) {
            Ciphertext ciphertext = encrypt.encryptWithAlpha(preCleartext,gSplitList,params);
            ciphertextList.add(ciphertext);
            System.out.println("ciphertext : " + ciphertext.getCipher());
        }
        //mine
        Ciphertext ciphertext = mine.mine(ciphertextList, params);
        //decrypt
        Map<Integer,BigInteger> secretShareMap = params.getSecretShare();
        Decrypt decrypt = new Decrypt();
        Map<Integer,BigInteger> cipherShareMap = decrypt.verifiableDecryptBySecretShareList(ciphertext, secretShareMap, params).getCipherShares();
        List<BigInteger> returnClearText = decrypt.decryptWithAlphaByCipherShares(cipherShareMap,alphaSplitList, params);
        //pro-process cleartext
        List<BigDecimal> decimalCombinedList = PaillierUtils.cleartextProProcess(returnClearText, params);
        System.out.println("combined cleartext: " );
        for(BigDecimal ct : decimalCombinedList){
            System.out.println(ct);
        }
    }
}
