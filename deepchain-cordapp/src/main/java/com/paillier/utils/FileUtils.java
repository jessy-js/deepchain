package com.paillier.utils;

import com.paillier.entity.PublicAndPrivateParams;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by jessy on 2019/6/4.
 */
public class FileUtils {
    public static String savedFileBasePath() {
        String folderPath;
        if (System.getProperty("user.dir").endsWith("kotlin")) {
            folderPath = System.getProperty("user.dir") + "/deepchain-cordapp/run";
        }
        else {
            folderPath = System.getProperty("user.dir") + "/run";
        }
        File folder = new File(folderPath);
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
            System.out.println("Create dir " + folderPath + " successfully.");
        }
        return folderPath;
    }

    public static void writeData(BufferedWriter out, String msg, String data) {
        try {
            out.newLine();
            out.write(msg);
            out.newLine();
            out.write(data);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeData(BufferedWriter out, String msg) {
        try {
            out.newLine();
            out.write(msg);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFileExists(String savedPath, String paramsFile,
                                       int partyNum, int alphaLen, String identifier) {
        String basicFileName = partyNum + "_" + alphaLen + "_" + identifier + ".txt";
        File file = new File(savedPath + "/" + paramsFile +"_" + basicFileName);
        System.out.println("Filename: " + savedPath + "/" + paramsFile +"_" + basicFileName);
        return file.exists();
    }


    public static void savePublicParams(PublicAndPrivateParams params, String savedPath,
                                        int alphaLen, String identifier) {
        String fileName;
        String basicFileName = params.getPartyNum() + "_" + alphaLen + "_" + identifier + ".txt";

        // save public params
        try {
            BufferedWriter out;

            fileName = savedPath + "/" + "publicParams_" + basicFileName;
            out = new BufferedWriter(new FileWriter(fileName, false));

            out.write("n");
            out.newLine();
            out.write(params.getN().toString());
            out.newLine();

            out.write("nsquare");
            out.newLine();
            out.write(params.getNsquare().toString());
            out.newLine();

            out.write("g");
            out.newLine();
            out.write(params.getG().toString());
            out.newLine();

            out.write("delta");
            out.newLine();
            out.write(String.valueOf(params.getDelta()));
            out.newLine();

            out.write("theta");
            out.newLine();
            out.write(params.getTheta().toString());
            out.newLine();

            out.write("bitLength");
            out.newLine();
            out.write(String.valueOf(params.getBitLength()));
            out.newLine();

            out.write("partyNum");
            out.newLine();
            out.write(String.valueOf(params.getPartyNum()));
            out.newLine();

            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSecretParams(PublicAndPrivateParams params, String savedPath,
                                        int alphaLen, String identifier){
        String fileName;
        String basicFileName = params.getPartyNum() + "_" + alphaLen + "_" + identifier + ".txt";
        // save secret keys
        try {
            BufferedWriter out;
            fileName = savedPath + "/" + "secretParams_" + basicFileName;
            out = new BufferedWriter(new FileWriter(fileName, false));

            out.write("secretKeys");
            out.newLine();
            List<BigInteger> secretShares = new LinkedList<>();
            for (Map.Entry<Integer, BigInteger> entry : params.getSecretShare().entrySet()){
                secretShares.add(entry.getValue());
            }
            out.write(secretShares.toString());
            out.newLine();

            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveAlphaParams(List<BigInteger> alphaSplitList, String savedPath,
                                       int partyNum, int alphaLen, String identifier){
        String fileName;
        String basicFileName = partyNum + "_" + alphaLen + "_" + identifier + ".txt";
        // save alpha list
        try {
            BufferedWriter out;
            fileName = savedPath + "/" + "alphaSplitList_" + basicFileName;
            out = new BufferedWriter(new FileWriter(fileName, false));
            out.write("alphaSplitList Start");
            out.newLine();
            out.write(alphaSplitList.toString());
            out.newLine();
            out.write("alphaSplitList End");
            out.newLine();
            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void saveGParams(List<BigInteger> gSplitList, String savedPath,
                                   int partyNum, int alphaLen, String identifier){
        String fileName;
        String basicFileName = partyNum + "_" + alphaLen + "_" + identifier + ".txt";
        // save g list
        try {
            BufferedWriter out;
            fileName = savedPath + "/" + "gSplitList_" + basicFileName;
            out = new BufferedWriter(new FileWriter(fileName, false));
            out.write("gSplitList Start");
            out.newLine();
            out.write(gSplitList.toString());
            out.newLine();
            out.write("gSplitList End");
            out.newLine();
            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PublicAndPrivateParams loadParams(String savedPath,
                                                    int partyNum, int alphaLen, String identifier) {
        PublicAndPrivateParams params = new PublicAndPrivateParams();
        String basicFileName = partyNum + "_" + alphaLen + "_" + identifier + ".txt";
        File file = new File(savedPath + "/" + "publicParams_" + basicFileName);
        Scanner input;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        // load n，nsquare，g，delta，theta, bitLength
        input.nextLine();
        BigInteger n = input.nextBigInteger();
        params.setN(n);

        input.nextLine();
        input.next();
        BigInteger nsquare = input.nextBigInteger();
        params.setNsquare(nsquare);

        input.nextLine();
        input.next();
        BigInteger g = input.nextBigInteger();
        params.setG(g);

        input.nextLine();
        input.next();
        BigInteger delta = input.nextBigInteger();
        params.setDelta(delta);

        input.nextLine();
        input.next();
        BigInteger theta = input.nextBigInteger();
        params.setTheta(theta);

        input.nextLine();
        input.next();
        int bitlength = input.nextInt();
        params.setBitLength(bitlength);

        input.nextLine();
        input.next();
        int pNum = input.nextInt();
        params.setPartyNum(pNum);
        // load SK
        input.close();

        file = new File(savedPath + "/" + "secretParams_" + basicFileName);
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        input.nextLine();
        Map<Integer,BigInteger> secretShare = new HashMap<>();
        String data = input.nextLine();
        String[] sks = data.split(",");
        for (int i = 0; i < sks.length; ++i) {
            String temp = sks[i];
            temp = temp.trim();
            if (temp.startsWith("[")) {
                temp = temp.substring(1);
            }
            if (temp.endsWith("]")) {
                temp = temp.substring(0, temp.length() - 1);
            }
            secretShare.put(i+1,new BigInteger(temp));
        }
        params.setSecretShare(secretShare);
        input.close();
        return params;
    }

    public static List<BigInteger> loadAlphaList(String savedPath,
                                                 int partyNum, int alphaLen, String identifier) {
        String basicFileName = partyNum + "_" + alphaLen + "_" + identifier + ".txt";
        List<BigInteger> alphaSplitList = new LinkedList<>();
        // load alpha list
        Boolean flag = false;
        Scanner input;
        File file = new File(savedPath + "/" + "alphaSplitList_" + basicFileName);
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        List<BigInteger> alphaList;
        while (input.hasNext()) {
            String lineData = input.nextLine();
            if (lineData.contains("Start")) {
                flag = true;
            } else if (lineData.contains("End") && !flag) {
                return null;  // 异常结束
            } else if (lineData.contains("End")) {
                break;
            } else {
                String[] tempArr = (lineData.substring(1, lineData.length() - 1)).split(",");
                alphaList = new LinkedList<>();
                for (String alpha : tempArr) {
                    alphaList.add(new BigInteger(alpha.trim()));
                }
                alphaSplitList = alphaList;
            }
        }
        return alphaSplitList;
    }

    public static List<BigInteger> loadgSplitList(String savedPath,
                                                  int partyNum, int alphaLen, String identifier) {
        String basicFileName = partyNum + "_" + alphaLen + "_" + identifier + ".txt";
        List<BigInteger> gSplitList = new LinkedList<>();
        // load alpha list
        Boolean flag = false;
        Scanner input;
        // load g list
        File file = new File(savedPath + "/" + "gSplitList_" + basicFileName);
        try {
            input = new Scanner(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        List<BigInteger> gList;
        while (input.hasNext()) {
            String lineData = input.nextLine();
            if (lineData.contains("Start")) {
                flag = true;
            }
            else if (lineData.contains("End") && !flag) {
                return null;  // 异常结束
            }
            else if (lineData.contains("End")) {
                break;  // 正常结束
            }
            else {
                String[] tempArr = (lineData.substring(1, lineData.length() - 1)).split(",");
                gList = new LinkedList<>();
                for(String alpha: tempArr) {
                    gList.add(new BigInteger(alpha.trim()));
                }
                gSplitList = gList;
            }
        }
        return gSplitList;
    }
}
