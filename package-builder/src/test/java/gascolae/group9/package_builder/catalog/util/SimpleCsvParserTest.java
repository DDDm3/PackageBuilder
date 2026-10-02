package gascolae.group9.package_builder.catalog.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

class SimpleCsvParserTest {

    private int countCsv(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            List<Map<String, String>> records = SimpleCsvParser.parse(is);
            return records.size();
        }
    }

    @Test
    void testParseAllSeedCsvFiles() throws Exception {
        Assertions.assertEquals(91, countCsv("seed/data_items.csv"));
        Assertions.assertEquals(194, countCsv("seed/tags.csv"));
        Assertions.assertEquals(12, countCsv("seed/services.csv"));
        Assertions.assertEquals(36, countCsv("seed/service_levels.csv"));
        Assertions.assertEquals(94, countCsv("seed/service_inputs.csv"));
        Assertions.assertEquals(94, countCsv("seed/service_outputs.csv"));
        Assertions.assertEquals(86, countCsv("seed/service_deliverables.csv"));
        Assertions.assertEquals(94, countCsv("seed/service_deliverable_items.csv"));
        Assertions.assertEquals(298, countCsv("seed/service_tags.csv"));
        Assertions.assertEquals(2, countCsv("seed/service_relations.csv"));
    }
}
