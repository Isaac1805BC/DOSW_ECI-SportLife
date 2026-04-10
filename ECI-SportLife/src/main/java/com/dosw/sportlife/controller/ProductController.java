package com.dosw.sportlife.controller;

import com.dosw.sportlife.model.Product;
import com.dosw.sportlife.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceImpl productService;

    @GetMapping
    public ResponseEntity<List<Product>> listarProductos(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {

        List<Product> productos = productService.getProducts(category, name);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Product> crearProducto(@RequestBody Product product) {
        Product guardado = productService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }
}
