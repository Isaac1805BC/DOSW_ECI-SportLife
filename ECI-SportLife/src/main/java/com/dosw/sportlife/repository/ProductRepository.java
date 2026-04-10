package com.dosw.sportlife.repository;

import com.dosw.sportlife.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatusAndCategoryIgnoreCase(String status, String category);
    List<Product> findByStatusAndNameContainingIgnoreCase(String status, String name);
    List<Product> findByStatus(String status);
}
