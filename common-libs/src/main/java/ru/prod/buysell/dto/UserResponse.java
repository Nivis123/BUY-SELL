package ru.prod.buysell.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String phoneNumber;
    private String name;
    private boolean active;
    private LocalDateTime dateofCreated;
    private List<ProductResponse> products;
}