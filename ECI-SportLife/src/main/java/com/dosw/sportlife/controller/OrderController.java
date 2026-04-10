package com.dosw.sportlife.controller;

import com.dosw.sportlife.model.Order;
import com.dosw.sportlife.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> realizarCompra(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Order orden = orderService.checkout(email);
        return ResponseEntity.ok(orden);
    }
}
