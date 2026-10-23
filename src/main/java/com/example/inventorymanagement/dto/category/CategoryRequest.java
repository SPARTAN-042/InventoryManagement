package com.example.inventorymanagement.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank
        Long name,

        String description
)
{}
