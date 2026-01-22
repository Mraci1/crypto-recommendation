package com.xm.crypto_recommendation.ingestion;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Parses CSV files containing cryptocurrency price data into strongly typed records.
 *
 * <p>
 * This component uses Jackson's CSV module to map CSV rows directly to
 * {@link CryptoPriceCsvRecord} instances based on header names.
 * </p>
 *
 * <p>
 * Parsing errors are treated as unrecoverable and result in an
 * {@link IllegalStateException}, as malformed input data would prevent
 * correct application behavior.
 * </p>
 */
@Component
public class CsvParser {

    private final CsvMapper csvMapper;

    public CsvParser() {
        this.csvMapper = new CsvMapper();
    }

    /**
     * Parses the provided CSV input stream into a list of price records.
     *
     * @param inputStream input stream of a CSV file with headers
     * @return list of parsed {@link CryptoPriceCsvRecord} instances
     *
     * @throws IllegalStateException if the CSV cannot be parsed
     */
    public List<CryptoPriceCsvRecord> parse(InputStream inputStream) {
        try {
            CsvSchema schema = CsvSchema.emptySchema()
                    .withHeader();

            MappingIterator<CryptoPriceCsvRecord> iterator =
                    csvMapper.readerFor(CryptoPriceCsvRecord.class)
                            .with(schema)
                            .readValues(inputStream);

            return iterator.readAll();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse CSV file", e);
        }
    }
}
