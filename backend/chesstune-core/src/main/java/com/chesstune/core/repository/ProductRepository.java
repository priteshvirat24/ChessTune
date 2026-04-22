package com.chesstune.core.repository;

import com.chesstune.core.entity.Product;
import com.chesstune.core.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByIdDesc();
    List<Product> findByProductTypeAndActiveTrueOrderByIdDesc(ProductType type);
}
