package com.GitScribe.Util;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriterUtil {

    public static void writeCSV(String fileName, List<String[]> rows) {
        String[] header = {
            "Index",
            "Method Name",
            "Method Signature",
            "Commit Count",
            "Change Types",
            "Commit Message",
            "Commit ID",
            "Commit Author"
        };
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
            writer.writeNext(header);
            writer.writeAll(rows);
            System.out.println("CSV generated: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
