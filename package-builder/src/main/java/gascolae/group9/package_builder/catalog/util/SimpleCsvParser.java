package gascolae.group9.package_builder.catalog.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SimpleCsvParser {

    public static List<Map<String, String>> parse(InputStream is) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> currentRow = new ArrayList<>();
            StringBuilder currentField = new StringBuilder();
            boolean inQuotes = false;
            int ch;

            reader.mark(1);
            int first = reader.read();
            if (first != 0xFEFF) {
                reader.reset();
            }

            while ((ch = reader.read()) != -1) {
                char c = (char) ch;
                if (inQuotes) {
                    if (c == '"') {
                        reader.mark(1);
                        int next = reader.read();
                        if (next == '"') {
                            currentField.append('"');
                        } else {
                            inQuotes = false;
                            reader.reset();
                        }
                    } else {
                        currentField.append(c);
                    }
                } else {
                    if (c == '"') {
                        inQuotes = true;
                    } else if (c == ',') {
                        currentRow.add(currentField.toString().trim());
                        currentField.setLength(0);
                    } else if (c == '\r') {
                        // skip
                    } else if (c == '\n') {
                        currentRow.add(currentField.toString().trim());
                        rows.add(currentRow);
                        currentRow = new ArrayList<>();
                        currentField.setLength(0);
                    } else {
                        currentField.append(c);
                    }
                }
            }
            if (currentField.length() > 0 || !currentRow.isEmpty()) {
                currentRow.add(currentField.toString().trim());
                rows.add(currentRow);
            }
        }

        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> headers = rows.getFirst();
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() == 1 && row.getFirst().isEmpty()) {
                continue;
            }
            Map<String, String> map = new LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                String header = headers.get(j).replace("\uFEFF", "").trim();
                String val = j < row.size() ? row.get(j) : "";
                map.put(header, val);
            }
            result.add(map);
        }
        return result;
    }
}
