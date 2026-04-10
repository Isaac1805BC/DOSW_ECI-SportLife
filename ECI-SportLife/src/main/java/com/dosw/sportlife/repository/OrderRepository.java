package com.dosw.sportlife.repository;

import com.dosw.sportlife.model.Order;
import com.dosw.sportlife.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
