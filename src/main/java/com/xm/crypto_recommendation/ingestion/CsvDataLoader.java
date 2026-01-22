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

/**
 * Loads historical cryptocurrency price data from CSV files into the database
 * on application startup.
 *
 * <p>
 * This component is implemented as an {@link ApplicationRunner} so that CSV
 * ingestion happens once during application initialization, before any API
 * requests are served.
 * </p>
 *
 * <p>
 * For the purpose of this exercise, the loader assumes an empty database on
 * startup (e.g. in-memory H2). In a production environment with a persistent
 * database, CSV ingestion would typically be handled using a versioned
 * migration or ingestion mechanism (such as Flyway, Liquibase, or a dedicated
 * ingestion history table) to ensure idempotency and safe re-deployments.
 * </p>
 *
 * <p>
 * Each CSV file is expected to represent a single cryptocurrency and follow
 * a naming convention of {@code SYMBOL_*.csv} (e.g. {@code BTC_values.csv}).
 * </p>
 */
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

    /**
     * Executes CSV ingestion on application startup.
     *
     * <p>
     * For each discovered CSV file:
     * <ul>
     *     <li>The crypto symbol is derived from the file name</li>
     *     <li>The crypto entity is created if not already present</li>
     *     <li>All price records are parsed and persisted in bulk</li>
     * </ul>
     * </p>
     *
     * <p>
     * The method is transactional to ensure consistency in case of parsing
     * or persistence errors.
     * </p>
     */
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

    /**
     * Extracts the crypto symbol from the CSV file name.
     *
     * <p>
     * Example: {@code BTC_values.csv} → {@code BTC}
     * </p>
     */
    private String extractSymbol(Resource resource) {
        String filename = resource.getFilename();
        return filename.substring(0, filename.indexOf("_"));
    }
}
