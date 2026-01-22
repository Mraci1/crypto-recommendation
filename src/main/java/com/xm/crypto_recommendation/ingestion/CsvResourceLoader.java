package com.xm.crypto_recommendation.ingestion;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Discovers CSV resources containing cryptocurrency price data.
 *
 * <p>
 * CSV files are loaded from the classpath using a glob pattern, allowing
 * new cryptocurrencies to be added simply by introducing new CSV files
 * without requiring any code changes.
 * </p>
 *
 * <p>
 * Expected file naming convention: {@code SYMBOL_values.csv}
 * <br>
 * Example: {@code BTC_values.csv}
 * </p>
 */
@Component
public class CsvResourceLoader {

    private static final String DATA_PATTERN = "classpath:data/*_values.csv";

    /**
     * Loads all CSV resources matching the configured pattern.
     *
     * @return list of CSV {@link Resource} objects
     *
     * @throws IllegalStateException if CSV resources cannot be resolved
     */
    public List<Resource> loadAllCsvs() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            return List.of(resolver.getResources(DATA_PATTERN));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load CSV resources", e);
        }
    }
}
