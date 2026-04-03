package com.automarket.marketplace.car;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarMakeRepository extends JpaRepository<CarMake, UUID> {
    List<CarMake> findByActiveTrueOrderByNameAsc();
}

