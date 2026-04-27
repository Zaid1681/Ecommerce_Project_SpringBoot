package com.platform.ecommerce.payment.serviceImpl;

import com.platform.ecommerce.cart.service.CartService;
import com.platform.ecommerce.common.enums.OrderStatus;
import com.platform.ecommerce.common.enums.PaymentStatus;
import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.inventory.service.InventoryService;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.order.entity.OrderItem;
import com.platform.ecommerce.order.repository.OrderRepository;
import com.platform.ecommerce.payment.dto.PaymentResDto;
import com.platform.ecommerce.payment.entity.Payment;
import com.platform.ecommerce.payment.mapper.PaymentMapper;
import com.platform.ecommerce.payment.repository.PaymentRepository;
import com.platform.ecommerce.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CartService cartService;

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    PaymentMapper paymentMapper;

    @Override
    public void markSuccess(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Details not found"));

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // checking payment status
        if(!payment.getStatus().equals(PaymentStatus.PENDING)){
            throw new RuntimeException("Failed to create payment");
        }

        // checking order status
        if(!order.getStatus().equals(OrderStatus.CREATED)){
            throw new RuntimeException("Failed to create payment");
        }

        order.setStatus(OrderStatus.PAID);
        payment.setStatus(PaymentStatus.SUCCESS);

        cartService.clearCart(order.getUserId());

        // saving changes
        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    @Override
    public void markFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Details not found"));
        payment.setStatus(PaymentStatus.FAILED);

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if(!payment.getStatus().equals(PaymentStatus.PENDING)){
            throw new RuntimeException("Payment process failed");
        }
        order.setStatus(OrderStatus.CANCELLED);

        // 🔥 Restore stock
        for (OrderItem item : order.getItems()) {
            inventoryService.restoreStock(item.getProductId(), item.getQuantity());
        }
        paymentRepository.save(payment);
        orderRepository.save(order);

    }

    @Override
    public PaymentResDto getPaymentDetailsBId(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Details not found"));

        return paymentMapper.toDto(payment);
    }
}
