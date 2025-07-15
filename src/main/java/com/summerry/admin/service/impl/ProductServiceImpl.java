package com.summerry.admin.service.impl;

import com.summerry.admin.controller.ProductController;
import com.summerry.admin.dto.product.*;
import com.summerry.admin.entity.Category;
import com.summerry.admin.entity.product.*;
import com.summerry.admin.repository.*;
import com.summerry.admin.repository.product.*;
import com.summerry.admin.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VariantValueRepository variantValueRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public void createProduct(ProductCreateRequest request) {
        // 1. 카테고리 조회
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 2. 상품 저장
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .category(category)
                .build();

        productRepository.save(product);

        // 3. 옵션 + 옵션값 저장
        // "옵션명:옵션값" 형태의 키로 정확하게 구분
        Map<String, ProductOptionValue> optionKeyToValueMap = new HashMap<>();

        for (ProductOptionDto optionDto : request.getOptions()) {
            ProductOption option = ProductOption.builder()
                    .optionName(optionDto.getOptionName())
                    .product(product)
                    .build();

            productOptionRepository.save(option);

            Set<String> uniqueValues = new HashSet<>();
            for(ProductOptionValueDto optionValueDto : optionDto.getValues()) {
                String valueName = optionValueDto.getValueName();
                if (!uniqueValues.add(valueName)) {
                    throw new IllegalArgumentException("옵션 '" + optionDto.getOptionName() + "' 안에 중복된 옵션값이 있습니다 : " + valueName);
                }

                ProductOptionValue optionValue = ProductOptionValue.builder()
                        .valueName(valueName)
                        .adjustPrice(optionValueDto.getAdjustPrice())
                        .option(option)
                        .build();

                productOptionValueRepository.save(optionValue);

                String compositeKey = option.getOptionName() + ":" + valueName;
                optionKeyToValueMap.put(compositeKey, optionValue);
            }
        }

        for (ProductVariantDto variantDto : request.getVariants()) {
            if (!productVariantRepository.existsBySku(variantDto.getSku())) {
                throw new IllegalArgumentException("이미 존재하는 SKU입니다 : " + variantDto.getSku());
            }
            ProductVariant productVariant = ProductVariant.builder()
                    .product(product)
                    .sku(variantDto.getSku())
                    .stock(variantDto.getStock())
                    .build();

            productVariantRepository.save(productVariant);

            for(String valueName : variantDto.getValueNames()) {
                if (!valueName.contains(":")) {
                    throw new IllegalArgumentException("옵션값은 반드시 '옵션명:옵션값' 형태로 전달되어야 합니다. 예: 사이즈:M");
                }

                ProductOptionValue optionValue = optionKeyToValueMap.get(valueName);
                if (optionValue == null) {
                    throw new IllegalArgumentException("존재하지 않는 옵션값입니다 : " + valueName);
                }

                VariantValue variantValue = VariantValue.builder()
                        .productVariant(productVariant)
                        .optionValue(optionValue)
                        .build();
                variantValueRepository.save(variantValue);
            }
        }

    }

    @Transactional
    @Override
    public void updateProduct(ProductUpdateRequest request, Long updatedBy) {
        // 1. 상품 정보 변경 감지 후 수정
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));

        boolean changed = false;

        if (!Objects.equals(product.getName(), request.getName())) {
            product.setName(request.getName());
            changed = true;
        }

        if (!Objects.equals(product.getDescription(), request.getDescription())) {
            product.setDescription(request.getDescription());
            changed = true;
        }

        if (!Objects.equals(product.getPrice(), request.getPrice())) {
            product.setPrice(request.getPrice());
            changed = true;
        }

        if (!Objects.equals(product.getThumbnailUrl(), request.getThumbnailUrl())) {
            product.setThumbnailUrl(request.getThumbnailUrl());
            changed = true;
        }

        if (product.getCategory() == null ||
            !Objects.equals(product.getCategory().getCategoryId(), request.getCategoryId())) {

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 카테고리를 찾을 수 없습니다."));

            product.setCategory(category);
            changed = true;
        }

        if (changed) {
            product.setUpdatedBy(updatedBy);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        }

        // 2. 옵션 및 옵션값 수정
        updateOptionsAndValues(product, request.getOptions());

        // 3. SKU 및 옵션값 조합 매핑 수정
        updateVariants(product, request.getVariants());

    }

    private void updateOptionsAndValues(Product product, List<ProductOptionDto> requestOptions) {

        // 1. 기존 옵션 불러오기
        List<ProductOption> existingOptions =  productOptionRepository.findByProductId(product.getProductId());

        Map<Long, ProductOption> existingOptionMap = existingOptions.stream()
                .collect(Collectors.toMap(ProductOption::getOptionId, o -> o));

        // 2. 요청에서 전달된 옵션 ID 수집
        Set<Long> requestOptionIds = requestOptions.stream()
                .map(ProductOptionDto::getOptionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 삭제 대상 옵션 처리
        for (ProductOption existingOption : existingOptions) {
            if(!requestOptionIds.contains(existingOption.getOptionId())) {
                productOptionRepository.deleteById(existingOption.getOptionId());
            }
        }

        // 4. 신규 또는 기존 옵션 저장/수정 처리
        for (ProductOptionDto dto : requestOptions) {
            if(dto.getOptionId() == null) {
                // id가 없을 경우 신규 등록
                ProductOption newOption = ProductOption.builder()
                        .product(product)
                        .optionName(dto.getOptionName())
                        .createdBy(1L) // 임시 하드코딩
                        .build();

                productOptionRepository.save(newOption);
                saveOptionValues(newOption, dto.getValues());
            } else {
                // id가 있을 경우 변경된 값이 있는지 비교하여 수정
                ProductOption option = existingOptionMap.get(dto.getOptionId());

                boolean changed = false;

                if (!Objects.equals(option.getOptionName(), dto.getOptionName())) {
                    option.setOptionName(dto.getOptionName());
                    changed = true;
                }

                if (changed) {
                    option.setUpdatedBy(1L); // 임시 하드코딩
                    productOptionRepository.save(option);
                }
                updateOptionValues(option, dto.getValues());
            }

        }
    }


    private void saveOptionValues(ProductOption option, List<ProductOptionValueDto> requestValues) {
        for (ProductOptionValueDto dto : requestValues) {
            ProductOptionValue optionValue = ProductOptionValue.builder()
                    .option(option)
                    .valueName(dto.getValueName())
                    .adjustPrice(dto.getAdjustPrice())
                    .createdBy(1L) // 임시 하드코딩
                    .build();
            productOptionValueRepository.save(optionValue);
        }
    }

    public void updateOptionValues(ProductOption option, List<ProductOptionValueDto> requestValues) {
        // 1. 기존 옵션값 불러오기
        List<ProductOptionValue> existingOptionValues = productOptionValueRepository.findByOptionIds(option.getOptionId());

        Map<Long, ProductOptionValue> existingOptionValueMap = existingOptionValues.stream()
                .collect(Collectors.toMap(ProductOptionValue::getValueId, v -> v));

        // 2. 요청에서 전달된 옵션값 ID 수집
        Set<Long> valueIds = requestValues.stream()
                .map(ProductOptionValueDto::getValueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 삭제 대상 옵션 처리 - 기존 옵션 값이 요청한 값에 포함되어 있지 않은 경우 삭제
        for(ProductOptionValue optionValue : existingOptionValues) {
            if(!valueIds.contains(optionValue.getValueId())) {
                productOptionValueRepository.deleteById(optionValue.getValueId());
            }
        }

        // 4. 신규 또는 기존 옵션값 저장/수정 처리
        for (ProductOptionValueDto dto : requestValues) {
            if(dto.getValueId() == null) {
                ProductOptionValue newOptionValue = ProductOptionValue.builder()
                        .option(option)
                        .valueName(dto.getValueName())
                        .adjustPrice(dto.getAdjustPrice())
                        .createdBy(1L) // 임시 하드코딩
                        .build();
                productOptionValueRepository.save(newOptionValue);
            } else {
                ProductOptionValue optionValue = existingOptionValueMap.get(dto.getValueId());

                boolean changed = false;

                if(Objects.equals(optionValue.getValueName(), dto.getValueName())) {
                    optionValue.setValueName(dto.getValueName());
                    changed = true;
                }

                if(Objects.equals(optionValue.getAdjustPrice(), dto.getAdjustPrice())) {
                    optionValue.setAdjustPrice(dto.getAdjustPrice());
                    changed = true;
                }

                if (changed) {
                    optionValue.setUpdatedBy(1L); // 임시 하드코딩
                    productOptionValueRepository.save(optionValue);
                }
            }
        }

    }

    public void updateVariants(Product product, List<ProductVariantDto> requestVariants) {
        // 1. 기존 variants 불러오기
        List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getProductId());

        Map<Long, ProductVariant> existingVariantsMap = existingVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getVariantId, v -> v));

        // 2. 요청에서 전달된 variants ID 수집
        Set<Long> variantIds = requestVariants.stream()
                .map(ProductVariantDto::getVariantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 삭제 대상 SKU 처리
        for (ProductVariant existingVariant : existingVariants) {
            if(!variantIds.contains(existingVariant.getVariantId())) {
                // 재고가 남아 있는 경우 삭제 불가
                if (existingVariant.getStock() > 0) {
                    throw new IllegalStateException("재고가 남아있는 SKU는 삭제할 수 없습니다 : " + existingVariant.getSku());
                }

                /*
                todo :
                1. 프론트에서 재고가 남아있을 시 삭제할 수 없도록 막기
                2. status 버튼은 재고가 남아있어도 비활성화로 변경할 수 있도록 세팅
                3. 재고가 0인 경우 삭제 버튼 활성화 + 소프트딜리트 진행
                */

                // 재고가 0인 경우 소프트딜리트 + INACTVE로 변경
                existingVariant.setStatus("INACTIVE");
                existingVariant.setUpdatedBy(1L); // 임시 하드코딩
                productVariantRepository.save(existingVariant);

                variantValueRepository.deleteByVariantId(existingVariant.getVariantId());
            }
        }

        // 4. 신규 또는 기존 variants 저장/수정 처리
        for (ProductVariantDto dto : requestVariants) {
            if(dto.getVariantId() == null) {
                ProductVariant productVariant = ProductVariant.builder()
                        .product(product)
                        .sku(dto.getSku())
                        .stock(dto.getStock())
                        .status("ACTIVE")
                        .deleteYn(false)
                        .createdBy(1L) // 임시 하드코딩
                        .build();
                productVariantRepository.save(productVariant);
            } else {
                ProductVariant productVariant = existingVariantsMap.get(dto.getVariantId());

                boolean changed = false;

                if(!Objects.equals(productVariant.getSku(), dto.getSku())) {
                    productVariant.setSku(dto.getSku());
                    changed = true;
                }

                if(!Objects.equals(productVariant.getStock(), dto.getStock())) {
                    productVariant.setStock(dto.getStock());
                    changed = true;
                }

                if(!Objects.equals(productVariant.getStatus(), dto.getStatus())) {
                    productVariant.setStatus(dto.getStatus());
                    changed = true;
                }

                if(!Objects.equals(productVariant.getDeleteYn(), dto.getDeleteYn())) {
                    productVariant.setDeleteYn(dto.getDeleteYn());
                    changed = true;
                }

                if (changed) {
                    productVariant.setUpdatedBy(1L);
                    productVariantRepository.save(productVariant);
                }

                updateVariantValues(productVariant, dto.getOptionValueIds());
            }
        }

    }


    private void updateVariantValues(ProductVariant productVariant, List<Long> requestOtionValueIds) {
        List<VariantValue> existingVariantValues = variantValueRepository.findByVariantId(productVariant.getVariantId());

        Set<Long> existingOptionValueIds = existingVariantValues.stream()
                .map(v -> v.getOptionValue().getValueId())
                .collect(Collectors.toSet());

        Set<Long> requestOptionValueIds = new HashSet<>(requestOtionValueIds);

        for(VariantValue vv : existingVariantValues) {
            if (!requestOptionValueIds.contains(existingOptionValueIds)) {
                variantValueRepository.deleteById(vv.getVariantValueId());
            }
        }

        for(Long optionValueId : requestOptionValueIds) {
            if(!existingOptionValueIds.contains(optionValueId)) {
                ProductOptionValue productOptionValue = productOptionValueRepository.findById(optionValueId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 옵션값입니다."));
                VariantValue variantValue = VariantValue.builder()
                        .optionValue(productOptionValue)
                        .createdBy(1L) // 임시 하드코딩
                        .build();
                variantValueRepository.save(variantValue);
            }
        }
    }
}
