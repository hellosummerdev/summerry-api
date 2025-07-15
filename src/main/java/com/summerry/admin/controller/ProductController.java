package com.summerry.admin.controller;

import com.summerry.admin.dto.product.ProductCreateRequest;
import com.summerry.admin.dto.product.ProductUpdateRequest;
import com.summerry.admin.service.ProductService;
import com.summerry.admin.service.impl.ProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceImpl productServiceImpl;

    @Operation(
            summary = "상품 등록 API",
            description = "상품, 옵션, 옵션값, SKU를 한 번에 등록합니다. SKU는 전체 쇼핑몰에서 유니크해야 합니다."
    )
    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody @Valid ProductCreateRequest request) {
        productServiceImpl.createProduct(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "상품 수정 API",
            description = "상품, 옵션, 옵션값, SKU를 한 번에 수정합니다."
    )
    @PutMapping
    public ResponseEntity<Void> updateProduct(@RequestBody @Valid ProductUpdateRequest request) {
        Long mockUserId = 1L; // 임시 관리자 계정
        productServiceImpl.updateProduct(request, mockUserId);
        return ResponseEntity.ok().build();
    }
}
