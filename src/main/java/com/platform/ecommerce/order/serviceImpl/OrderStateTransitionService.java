package com.platform.ecommerce.order.serviceImpl;

import com.platform.ecommerce.common.enums.OrderStatus;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.order.statemachine.OrderStateMachine;
import org.springframework.stereotype.Service;

@Service
public class OrderStateTransitionService {

    private final OrderStateMachine orderStateMachine;

    public OrderStateTransitionService(OrderStateMachine orderStateMachine) {
        this.orderStateMachine = orderStateMachine;
    }

    public void confirmOrder(Order order) {
        orderStateMachine.transition(order, OrderStatus.CONFIRMED);
    }

    public void transitionTo(Order order, OrderStatus status) {
        orderStateMachine.transition(order, status);
    }
}
