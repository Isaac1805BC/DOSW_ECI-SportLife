package com.dosw.sportlife.controller;

import com.dosw.sportlife.dto.request.AddToCartRequest;
import com.dosw.sportlife.model.Cart;
import com.dosw.sportlife.service.impl.CartServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartServiceImpl cartService;

    @GetMapping
    public ResponseEntity<Cart> verCarrito(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.getCart(email));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> agregarItem(@AuthenticationPrincipal UserDetails userDetails,
                                            @Valid @RequestBody AddToCartRequest datos) {
        String email = userDetails.getUsername();
        Cart carritoActualizado = cartService.addItem(email, datos);
        return ResponseEntity.ok(carritoActualizado);
    }
}
