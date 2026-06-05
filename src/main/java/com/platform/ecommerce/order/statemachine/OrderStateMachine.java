package com.platform.ecommerce.order.statemachine;

import com.platform.ecommerce.common.enums.OrderStatus;
import com.platform.ecommerce.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStateMachine {

        private static final Logger log = LoggerFactory.getLogger(OrderStateMachine.class);

    private static final Map<OrderStatus, Set<OrderStatus>> transitions =
            new HashMap<>();

    static {

        transitions.put(
                OrderStatus.PLACED,
                Set.of(
                        OrderStatus.CONFIRMED,
                        OrderStatus.CANCELLED
                )
        );

        transitions.put(
                OrderStatus.CONFIRMED,
                Set.of(
                        OrderStatus.PAYMENT_PENDING,
                        OrderStatus.CANCELLED
                )
        );

        transitions.put(
                OrderStatus.PAYMENT_PENDING,
                Set.of(
                        OrderStatus.PAID,
                        OrderStatus.PAYMENT_FAILED,
                        OrderStatus.CANCELLED
                )
        );

        transitions.put(
                OrderStatus.PAID,
                Set.of(
                        OrderStatus.PROCESSING,
                        OrderStatus.CANCELLED
                )
        );

        transitions.put(
                OrderStatus.PROCESSING,
                Set.of(
                        OrderStatus.SHIPPED,
                        OrderStatus.CANCELLED
                )
        );

        transitions.put(
                OrderStatus.SHIPPED,
                Set.of(
                        OrderStatus.DELIVERED
                )
        );

        transitions.put(
                OrderStatus.PAYMENT_FAILED,
                Set.of(
                        OrderStatus.CANCELLED
                )
        );
    }

    public void transition(Order order,
                           OrderStatus newStatus) {

        OrderStatus currentStatus = order.getStatus();

        log.info("Attempting transition for orderId={} from {} to {}",
                order.getId(), currentStatus, newStatus);

        // idempotent: if already in desired status, do nothing
        if (currentStatus == newStatus) {
            log.debug("No-op transition: orderId={} already in {}", order.getId(), newStatus);
            return;
        }

        Set<OrderStatus> allowedTransitions =
                transitions.getOrDefault(currentStatus, Set.of());

        if (!allowedTransitions.contains(newStatus)) {
            log.error("Invalid transition attempted for orderId={} from {} to {}",
                    order.getId(), currentStatus, newStatus);
            throw new RuntimeException(
                    "Invalid transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        order.setStatus(newStatus);
        log.info("Transitioned orderId={} to {}", order.getId(), newStatus);
    }
}
