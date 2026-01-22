package com.xm.crypto_recommendation.ingestion;

import com.xm.crypto_recommendation.domain.entity.Crypto;
import com.xm.crypto_recommendation.domain.entity.CryptoPrice;
import com.xm.crypto_recommendation.repository.CryptoPriceRepository;
import com.xm.crypto_recommendation.repository.CryptoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvDataLoader implements ApplicationRunner {

    private final CryptoRepository cryptoRepository;
    private final CryptoPriceRepository priceRepository;
    private final CsvResourceLoader resourceLoader;
    private final CsvParser csvParser;


    @Autowired
    public CsvDataLoader(
            CryptoRepository cryptoRepository, CryptoPriceRepository priceRepository,
            CsvResourceLoader resourceLoader, CsvParser csvParser
    ) {
        this.cryptoRepository = cryptoRepository;
        this.priceRepository = priceRepository;
        this.resourceLoader = resourceLoader;
        this.csvParser = csvParser;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        resourceLoader.loadAllCsvs().forEach(resource -> {
            try (InputStream csvInputStream = resource.getInputStream()) {
                List<CryptoPriceCsvRecord> records = csvParser.parse(csvInputStream);

                String symbol = extractSymbol(resource);
                Crypto crypto = cryptoRepository.findBySymbol(symbol).orElseGet(() -> cryptoRepository.save(new Crypto(symbol)));

                List<CryptoPrice> cryptoPrices = records.stream()
                        .map(cryptoPriceCsvRecord ->
                                new CryptoPrice(crypto, Instant.ofEpochMilli(cryptoPriceCsvRecord.getTimestamp()), cryptoPriceCsvRecord.getPrice()))
                        .collect(Collectors.toList());

                priceRepository.saveAll(cryptoPrices);

            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to read CSV: " + resource.getFilename(), e
                );
            }
        });
    }

    private String extractSymbol(Resource resource) {
        String filename = resource.getFilename(); // BTC_values.csv
        return filename.substring(0, filename.indexOf("_"));
    }
}
