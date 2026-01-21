package com.xm.crypto_recommendation.repository;

import com.xm.crypto_recommendation.domain.Crypto;
import com.xm.crypto_recommendation.domain.CryptoPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {

    List<CryptoPrice> findByCrypto(Crypto crypto);

}
