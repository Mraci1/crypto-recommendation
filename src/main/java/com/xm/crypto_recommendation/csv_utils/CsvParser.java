package com.xm.crypto_recommendation.csv_utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class CsvParser {

    private final CsvMapper csvMapper;

    public CsvParser() {
        this.csvMapper = new CsvMapper();
    }

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
