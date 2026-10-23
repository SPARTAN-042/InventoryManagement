package com.example.inventorymanagement.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        String name,

        @NotBlank
        String sku,

        String description,

        @NotNull
        @PositiveOrZero
        BigDecimal price,

        @NotNull
        @PositiveOrZero
        Integer quantity,

        @NotNull
        Long categoryId

) {}
