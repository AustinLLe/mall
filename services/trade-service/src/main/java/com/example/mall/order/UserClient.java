package com.example.mall.order;

public interface UserClient {
    void requireActiveUserAndAddress(long userId, long addressId);
}
