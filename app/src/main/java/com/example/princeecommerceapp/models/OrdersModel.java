package com.example.princeecommerceapp.models;

public class OrdersModel {
    private String userOrder; // Field to store order details
    private String orderId;
    private String orderStatus;// Field to store the document ID

    public OrdersModel() {
        // Default constructor required for calls to DataSnapshot.getValue(OrdersModel.class)
    }

    public String getUserOrder() {
        return userOrder;
    }

    public void setUserOrder(String userOrder) {
        this.userOrder = userOrder;
    }

    public String getOrderId() { // Getter for orderId
        return orderId;
    }

    public void setOrderId(String orderId) { // Setter for orderId
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}