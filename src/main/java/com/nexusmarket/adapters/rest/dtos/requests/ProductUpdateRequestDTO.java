package com.nexusmarket.adapters.rest.dtos.requests;

import com.nexusmarket.domain.valueobjects.ProductType;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequestDTO {

    private String name;
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    private ProductType type;
    private Long categoryId;
}
