package com.paillier.utils;

import java.io.*;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * version: 1.0
 * author: chengtianyang
 *
 * Process the data before and after every iteration
 *
 * 1. Read gradients from files and encapsulate every gradient
 *    into an one dimension vector before the iteration
 * 2. Restore the vector to the original format,
 *    and it could also be written to files after the iteration
 */
public class WashVector {

    private Integer partyNum;
    private Integer iterationRound = 1;
    private String paramPath = "";
    private static Integer[] dataLengthArray = {
            10 * 1 * 3 * 3,
            10 * 1,
            1960 * 128,
            128,
            128 * 10,
            10
    };

    public WashVector(Integer partyNum, Integer iterationRound) {
        this.partyNum = partyNum;
        this.iterationRound = iterationRound;
    }

    public WashVector(String paramPath) {
        this.paramPath = paramPath;
    }

    public List<List<BigDecimal>> wash() {
        return txt2VectorList();
    }

    public List<List<BigDecimal>> wash(int singleVectorLen) {
        List<List<BigDecimal>> vectorsList0 = txt2VectorList();
        List<BigDecimal> oneDimensionList = new LinkedList<>();

        for (List<BigDecimal> vector: vectorsList0) {
            oneDimensionList.addAll(vector);
        }

        List<List<BigDecimal>> vectorsList = new LinkedList<>();
        int i = 0;
        while ((i + 1) * singleVectorLen < oneDimensionList.size()) {
            vectorsList.add(oneDimensionList.subList(i * singleVectorLen, (i + 1) * singleVectorLen));
            i++;
        }
        List<BigDecimal> tempList = new LinkedList<>();
        tempList.addAll(oneDimensionList.subList(i * singleVectorLen, oneDimensionList.size()));
        int totalSize = tempList.size();
        for (int j = 0; j < singleVectorLen - totalSize; ++j) {
            tempList.add(BigDecimal.ZERO);
        }
        vectorsList.add(tempList);

        return vectorsList;
    }

    public List<List<BigDecimal>> txt2VectorList() {
        List<List<BigDecimal>> vectorsList = new LinkedList<>();
        System.out.println(System.getProperty("user.dir"));
        String filePath;
        if (!paramPath.equals("")) {
            filePath = paramPath;
        }
        else {
            filePath = "src/com/paillier/data/worker" + partyNum +
                    "/worker" + iterationRound + ".txt";
        }
        File file = new File(filePath);
        Scanner input;
        try {
            input = new Scanner(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return vectorsList;
        }

        while(input.hasNext()) {
            // flag means an input vector，false means that a new input vector is not ready
            boolean flag = false;
            int cnt = 0;
            Integer dataType = 1;
            // all vectors in the file
            List<BigDecimal> bdsList = new LinkedList<>();
            // if the loop stops, then a vector has been read and processed
            while (cnt < dataLengthArray[dataType - 1]) {
                if (!flag) {
                    String lineData;
                    try {
                        lineData = input.nextLine();
                    }
                    catch (NoSuchElementException e) {
                        break;
                    }
                    // System.out.println(lineData);
                    if (lineData.contains("W1")) {
                        flag = true;
                        dataType = 1;
                    }
                    else if (lineData.contains("b1")) {
                        flag = true;
                        dataType = 2;
                    }
                    else if (lineData.contains("W2")) {
                        flag = true;
                        dataType = 3;
                    }
                    else if (lineData.contains("b2")) {
                        flag = true;
                        dataType = 4;
                    }
                    if (lineData.contains("W3")) {
                        flag = true;
                        dataType = 5;
                    }
                    else if (lineData.contains("b3")) {
                        flag = true;
                        dataType = 6;
                    }
                    else {
                        continue;
                    }
                }

                String data = input.next();

                // if only bracket was read
                if (data.length() <= 5 && (data.startsWith("[") || data.startsWith("]"))) {
                    continue;
                }
                if (data.endsWith("]")) {
                    data = data.replace("]", "");
                }
                if (data.startsWith("[")) {
                    data = data.replace("[", "");
                }
                try {
                    BigDecimal tempData = new BigDecimal(data);
                    bdsList.add(tempData);
                    cnt++;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("DataType: " + dataType);
                    System.out.println("index" + cnt);
                    System.out.println("error: " + data);
                }
            }
            vectorsList.add(bdsList);
        }

        input.close();
        return vectorsList;
    }

    public void unwash(List<List<BigDecimal>> vectorList, String fileName) {
        List<BigDecimal> vectorListCombined = new LinkedList<>();
        for (List<BigDecimal>vectorListSingle: vectorList) {
            vectorListCombined.addAll(vectorListSingle);
        }

        int start = 0;
        int end;
        for (int i = 0; i < dataLengthArray.length; ++i) {
            end = dataLengthArray[i] + start;
            List<BigDecimal> vectorListSlice = vectorListCombined.subList(start, end);
            String vectorString = vectorListSlice2String(vectorListSlice, i + 1);
            writeDataToFile(vectorString, i + 1, fileName);
        }
    }

    /**
     * Restore an one dimensional vector to it original format
     *
     * @param resultList an one dimensional vector
     * @param dataType there are six types, belong to [1, 6]
     * @return return the string that was restored
     */
    public String vectorListSlice2String(List<BigDecimal> resultList, Integer dataType) {
        if (dataType == 1) {  // four dimension vector
            List<List<List<List<BigDecimal>>>> unwashedResult4D = new LinkedList<>();
            List<List<List<BigDecimal>>> unwashedResult3D = new LinkedList<>();
            for (int i = 1; i <= 10; ++i) {
                List<List<BigDecimal>> unwashedResult2D = new LinkedList<>();
                for (int j = 1; j <= 3; ++j) {
                    List<BigDecimal> unwashedResult1D = new LinkedList<>();
                    for (int k = 1; k <= 3; ++k) {
                        unwashedResult1D.add(resultList.get(9 * i + 3 * j + k - 13));
                    }
                    unwashedResult2D.add(unwashedResult1D);
                }
                unwashedResult3D.add(unwashedResult2D);
                unwashedResult4D.add(unwashedResult3D);
            }
            return unwashedResult4D.toString().replace(",", " ");
        }
        else if (dataType == 4 || dataType == 6) {  // one dimension vector that needn't processing
            return resultList.toString().replace(",", " ");
        }
        else {  // two dimension vector
            Integer dim1;
            Integer dim2;
            if (dataType == 2) {
                dim1 = 10;
                dim2 = 1;
            }
            else if (dataType == 3) {
                dim1 = 1960;
                dim2 = 128;
            }
            else if (dataType == 5) {
                dim1 = 128;
                dim2 = 10;
            }
            else {
                return "length error";
            }
            List<List<BigDecimal>> unwashedResult2D = new LinkedList<>();
            for (int j = 1; j <= dim1; ++j) {
                List<BigDecimal> unwashedResult1D = new LinkedList<>();
                for (int k = 1; k <= dim2; ++k) {
                    unwashedResult1D.add(resultList.get(dim2 * (j - 1) + k - 1));
                }
                unwashedResult2D.add(unwashedResult1D);
            }
            return unwashedResult2D.toString().replace(",", " ");
        }
    }

    /**
     * Export the vectors to file
     *
     * @param resultString the string of the vector
     * @param dataType there are six types, belong to [1, 6]
     */
    public void writeDataToFile(String resultString, Integer dataType, String filePath) {
        try {
            BufferedWriter out;
            if (dataType == 1) {
                out = new BufferedWriter(new FileWriter(filePath, false));
            }
            else {
                out = new BufferedWriter(new FileWriter(filePath, true));
                out.newLine();
            }
            switch (dataType) {
                case 1:
                    out.write("grad[1][W1]" + (iterationRound + 1) + ":");
                    break;
                case 2:
                    out.write("grad[1][b1]" + (iterationRound + 1) + ":");
                    break;
                case 3:
                    out.write("grad[1][W2]" + (iterationRound + 1) + ":");
                    break;
                case 4:
                    out.write("grad[1][b2]" + (iterationRound + 1) + ":");
                    break;
                case 5:
                    out.write("grad[1][W3]" + (iterationRound + 1) + ":");
                    break;
                case 6:
                    out.write("grad[1][b3]" + (iterationRound + 1) + ":");
                    break;
            }
            out.newLine();
            out.write(resultString);
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeStringToFile(String resultString, String filePath) {
        try {
            BufferedWriter out;
            out = new BufferedWriter(new FileWriter(filePath, false));
            out.write(resultString);
            out.newLine();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<List<BigDecimal>> generateVectors(int dataLength, int dataNum, int partyNum) {

        List<List<BigDecimal>> vectorsList = new LinkedList<>();
        File file = new File("src/com/paillier/data/worker1/worker1.txt");
        Scanner input;
        try {
            input = new Scanner(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return vectorsList;
        }
        int cnt = 0;

        List<BigDecimal> bdsList = new LinkedList<>();
        while (cnt < dataNum * dataLength * partyNum) {
            String data = input.next();

            if (data.equals("[[") || data.equals("[") || data.equals("]]") || data.equals("]")) {
                continue;
            }
            if (data.endsWith("]")) {
                data = data.replace("]", "");
            }
            if (data.startsWith("[")) {
                data = data.replace("[", "");
            }
            // System.out.println(data);
            try {
                BigDecimal tempData = new BigDecimal(data);
//                tempData = tempData.abs();
                bdsList.add(tempData);
            }
            catch (Exception e) {
                System.out.println("error: " + data);
                continue;
            }
            if (bdsList.size() >= dataLength) {
//                System.out.println(bdsList);
                vectorsList.add(bdsList);
                bdsList = new LinkedList<>();
            }
            cnt++;
        }

        input.close();
        return vectorsList;
    }
}
