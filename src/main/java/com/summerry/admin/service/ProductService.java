package com.summerry.admin.service;

import com.summerry.admin.dto.product.ProductCreateRequest;
import com.summerry.admin.dto.product.ProductUpdateRequest;

public interface ProductService {

    void createProduct(ProductCreateRequest request);

    void updateProduct(ProductUpdateRequest request, Long updatedBy);
}
