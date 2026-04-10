package com.dosw.sportlife.service.impl;

import com.dosw.sportlife.dto.request.AddToCartRequest;
import com.dosw.sportlife.model.Cart;
import com.dosw.sportlife.model.CartItem;
import com.dosw.sportlife.model.Product;
import com.dosw.sportlife.model.User;
import com.dosw.sportlife.repository.CartRepository;
import com.dosw.sportlife.repository.ProductRepository;
import com.dosw.sportlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Cart getCart(String email) {
        User usuario = userRepository.findByEmail(email).orElseThrow();
        return obtenerOCrearCarrito(usuario);
    }

    public Cart addItem(String email, AddToCartRequest datos) {
        User usuario = userRepository.findByEmail(email).orElseThrow();

        Product producto = productRepository.findById(datos.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (!producto.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Producto no disponible");
        }
        if (producto.getStock() < datos.getQuantity()) {
            throw new RuntimeException("Stock insuficiente");
        }

        Cart carrito = obtenerOCrearCarrito(usuario);

        // revisar si el producto ya esta en el carrito para solo sumarle cantidad
        CartItem itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(producto.getId()))
                .findFirst()
                .orElse(null);

        if (itemExistente != null) {
            itemExistente.setQuantity(itemExistente.getQuantity() + datos.getQuantity());
        } else {
            CartItem nuevoItem = new CartItem();
            nuevoItem.setCart(carrito);
            nuevoItem.setProduct(producto);
            nuevoItem.setQuantity(datos.getQuantity());
            carrito.getItems().add(nuevoItem);
        }

        return cartRepository.save(carrito);
    }

    // busca el carrito del usuario, si no tiene uno le crea uno vacio
    private Cart obtenerOCrearCarrito(User usuario) {
        return cartRepository.findByUser(usuario).orElseGet(() -> {
            Cart nuevoCarrito = new Cart();
            nuevoCarrito.setUser(usuario);
            return cartRepository.save(nuevoCarrito);
        });
    }
}
