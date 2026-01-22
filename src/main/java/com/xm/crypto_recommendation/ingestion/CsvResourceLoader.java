package com.xm.crypto_recommendation.ingestion;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CsvResourceLoader {

    private static final String DATA_PATTERN = "classpath:data/*_values.csv";

    public List<Resource> loadAllCsvs() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            return List.of(resolver.getResources(DATA_PATTERN));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load CSV resources", e);
        }
    }
}
