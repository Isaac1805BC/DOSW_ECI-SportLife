package com.dosw.sportlife.service.impl;

import com.dosw.sportlife.model.Cart;
import com.dosw.sportlife.model.CartItem;
import com.dosw.sportlife.model.Order;
import com.dosw.sportlife.model.OrderItem;
import com.dosw.sportlife.model.Product;
import com.dosw.sportlife.model.User;
import com.dosw.sportlife.repository.CartRepository;
import com.dosw.sportlife.repository.OrderRepository;
import com.dosw.sportlife.repository.ProductRepository;
import com.dosw.sportlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Order checkout(String email) {
        User usuario = userRepository.findByEmail(email).orElseThrow();

        Cart carrito = cartRepository.findByUser(usuario)
                .orElseThrow(() -> new RuntimeException("El carrito está vacío"));

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        validarStockDeItems(carrito);

        Order orden = crearOrdenDesdeCarrito(usuario, carrito);

        // por ahora simulamos que el pago siempre se aprueba
        boolean pagoAprobado = true;

        if (pagoAprobado) {
            orden.setStatus("PAID");
            orden.setTransactionId(UUID.randomUUID().toString());
            descontarStock(carrito);
            vaciarCarrito(carrito);
        } else {
            orden.setStatus("REJECTED");
        }

        return orderRepository.save(orden);
    }

    private void validarStockDeItems(Cart carrito) {
        for (CartItem item : carrito.getItems()) {
            Product producto = item.getProduct();
            if (producto.getStock() < item.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getName());
            }
        }
    }

    private Order crearOrdenDesdeCarrito(User usuario, Cart carrito) {
        Order orden = new Order();
        orden.setUser(usuario);
        orden.setStatus("PENDING");

        List<OrderItem> itemsOrden = new ArrayList<>();
        double total = 0;

        for (CartItem itemCarrito : carrito.getItems()) {
            OrderItem itemOrden = new OrderItem();
            itemOrden.setOrder(orden);
            itemOrden.setProduct(itemCarrito.getProduct());
            itemOrden.setQuantity(itemCarrito.getQuantity());
            itemOrden.setUnitPrice(itemCarrito.getProduct().getPrice());

            total += itemCarrito.getProduct().getPrice() * itemCarrito.getQuantity();
            itemsOrden.add(itemOrden);
        }

        orden.setItems(itemsOrden);
        orden.setTotal(total);
        return orden;
    }

    private void descontarStock(Cart carrito) {
        for (CartItem item : carrito.getItems()) {
            Product producto = item.getProduct();
            producto.setStock(producto.getStock() - item.getQuantity());
            productRepository.save(producto);
        }
    }

    private void vaciarCarrito(Cart carrito) {
        carrito.getItems().clear();
        cartRepository.save(carrito);
    }
}
