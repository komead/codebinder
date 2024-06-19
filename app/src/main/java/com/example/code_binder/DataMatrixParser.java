package com.example.code_binder;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMatrixParser {
    private final String gtinPattern = "(\\d{14})";
    private final String batchPattern = "10([\\x21-\\x7E]+?)($|\\d{2}|01|17|21)";
    private final String expirationPattern = "17(\\d{6})";
    private final String serialPattern = "([!%-?A-Z_a-z\\x22]{1,20})(\\x1D)";
    private final String countPattern = "37(\\d{1,8})";
    private final String tallPattern = "([!%-?A-Z_a-z\\x22]{1,90})";

    public enum AI {
        GTIN("01"),
        SERIAL_NUMBER("21"),
        COUNT_OF_ITEMS("37"),
        BATCH("10");

        private final String code;

        AI (String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public HashMap<String, String> parseDataMatrix(String dataMatrix) {
        if (dataMatrix == null || dataMatrix.isEmpty())
            return null;

        HashMap<String, String> resultMap = new HashMap<>();

        // Extract GTIN
        extractAndPut(dataMatrix, gtinPattern, AI.GTIN.getCode(), resultMap);
        // Extract Serial Number
        extractAndPut(dataMatrix, serialPattern, AI.SERIAL_NUMBER.getCode(), resultMap);

        return resultMap;
    }

    private static void extractAndPut(String dataMatrix, String pattern, String ai, HashMap<String, String> resultMap) {
        Pattern p = Pattern.compile(ai + pattern);
        Matcher m = p.matcher(dataMatrix);
        if (m.find()) {
            resultMap.put(ai, m.group(1));
        }
    }
}