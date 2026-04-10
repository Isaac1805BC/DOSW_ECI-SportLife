package com.dosw.sportlife.service.impl;

import com.dosw.sportlife.model.Product;
import com.dosw.sportlife.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

    private final ProductRepository productRepository;

    public List<Product> getProducts(String category, String name) {
        // filtrar por categoria si la mandan
        if (category != null) {
            return productRepository.findByStatusAndCategoryIgnoreCase("ACTIVE", category);
        }
        // filtrar por nombre si lo mandan
        if (name != null) {
            return productRepository.findByStatusAndNameContainingIgnoreCase("ACTIVE", name);
        }
        // si no mandan filtro, devolver todos los activos
        return productRepository.findByStatus("ACTIVE");
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }
}
