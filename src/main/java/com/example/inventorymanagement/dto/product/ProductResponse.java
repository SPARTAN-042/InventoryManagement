package com.example.inventorymanagement.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
     Long id,
     String name,
     String sku,
     String description,
     BigDecimal price,
     Integer quantity,
     String categoryName,
     LocalDateTime createdAt,
     LocalDateTime updatedAt
) {}
