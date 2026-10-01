package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.models.Product;

import java.util.List;

public interface CatalogUseCasePort {

    List<Product> getActiveProducts();

    List<Category> getRootCategories();

    List<Product> searchProducts(String query);

    Product getProductDetail(Long id);
}
