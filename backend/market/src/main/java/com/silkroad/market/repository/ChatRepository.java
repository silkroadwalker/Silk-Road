package com.silkroad.market.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.Chat;
import com.silkroad.market.entity.User;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByAdvertisementAndBuyerAndSeller(
            Advertisement advertisement,
            User buyer,
            User seller);

    List<Chat> findByBuyerOrSellerOrderByLastMessageAtDesc(
            User buyer,
            User seller);
}