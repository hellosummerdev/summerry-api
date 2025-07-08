package com.summerry.user.service;

import com.summerry.user.dto.AddressRequest;
import com.summerry.user.dto.AddressResponse;
import com.summerry.user.entity.Address;
import com.summerry.user.entity.User;
import com.summerry.user.mapper.AddressMapper;
import com.summerry.user.repository.AddressRepository;
import com.summerry.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Transactional
    public void createAddress(User user, AddressRequest addressRequest) {
        // 기본 배송지 설정 시, 기존 기본 배송지 해제
        if (Boolean.TRUE.equals(addressRequest.getIsDefault())) {
            addressMapper.clearDefaultForUser(user.getUserId());
        }

        Address address = Address.builder()
                .user(user)
                .receiverName(addressRequest.getReceiverName())
                .phone(addressRequest.getPhone())
                .addressLine1(addressRequest.getAddressLine1())
                .addressLine2(addressRequest.getAddressLine2())
                .postalCode(addressRequest.getPostalCode())
                .isDefault(addressRequest.getIsDefault())
                .deleteYn(false)
                .createdBy(user.getUserId())
                .updatedBy(user.getUserId())
                .build();

        addressRepository.save(address);
    }

    public List<AddressResponse> getAddresses(User user) {
        return addressMapper.findAllByUserId(user.getUserId());
    }

    public void updateAddress(User user, Long addressId, AddressRequest addressRequest) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소가 존재하지 않습니다."));

        if(!address.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("해당 주소에 대한 수정 권한이 없습니다.");
        }

        if (Boolean.TRUE.equals(addressRequest.getIsDefault())) {
            addressMapper.clearDefaultForUser(user.getUserId());
        }

        addressMapper.updateAddress(
                addressId,
                user.getUserId(),
                addressRequest.getReceiverName(),
                addressRequest.getPhone(),
                addressRequest.getAddressLine1(),
                addressRequest.getAddressLine2(),
                addressRequest.getPostalCode(),
                addressRequest.getIsDefault(),
                user.getUserId()
        );
    }

    @Transactional
    public void deleteAddress(User user, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소가 존재하지 않습니다."));

        if(!address.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("해당 주소에 대한 수정 권한이 없습니다.");
        }

        address.setDeleteYn(true);
        address.setUpdatedBy(user.getUserId());
    }
}
