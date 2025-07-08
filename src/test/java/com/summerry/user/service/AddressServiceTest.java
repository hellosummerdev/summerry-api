package com.summerry.user.service;

import com.summerry.user.dto.AddressRequest;
import com.summerry.user.entity.Address;
import com.summerry.user.entity.User;
import com.summerry.user.mapper.AddressMapper;
import com.summerry.user.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .username("testuser")
                .build();
    }

    @Test
    @DisplayName("배송지 전체 조회")
    void getAddresses_success() {
        // when
        addressService.getAddresses(user);

        // then
        verify(addressMapper).findAllByUserId(user.getUserId());
    }

    @Test
    @DisplayName("기본 배송지 등록 시 기존 기본 해제")
    void createAddress_withDefault_shouldClearOldDefault() {
        // given
        AddressRequest request = new AddressRequest();
        request.setReceiverName("홍길동");
        request.setPhone("01012345678");
        request.setAddressLine1("서울 성동구");
        request.setPostalCode("12345");
        request.setIsDefault(true);

        // when
        addressService.createAddress(user, request);

        // then
        verify(addressMapper).clearDefaultForUser(user.getUserId());
        verify(addressRepository).save(any());
    }

    @Test
    @DisplayName("배송지 수정")
    void updateAddress_success() {
        // given
        AddressRequest request = new AddressRequest();
        request.setReceiverName("김철수");
        request.setPhone("01098765432");
        request.setAddressLine1("서울 마포구");
        request.setPostalCode("54321");
        request.setIsDefault(false);

        Address existingAddress = Address.builder()
                .addressId(1L)
                .user(user)
                .build();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(existingAddress));

        // when
        addressService.updateAddress(user, 1L, request);

        // then
        verify(addressMapper).updateAddress(
                eq(1L), eq(1L), eq("김철수"), eq("01098765432"), eq("서울 마포구"),
                eq(null), eq("54321"), eq(false), any()
        );
    }

    @Test
    @DisplayName("수정 시 존재하지 않는 주소이면 예외 발생")
    void updateAddress_addressNotFound_throwsException() {
        // given
        AddressRequest request = new AddressRequest();

        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            addressService.updateAddress(user, 1L, request);
        });

        // then
        assertEquals("해당 주소가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("수정 시 주소가 본인의 것이 아니면 예외 발생")
    void updateAddress_otherUsersAddress_throwsException() {
        // given
        User otherUser = User.builder().userId(99L).build();
        AddressRequest request = new AddressRequest();

        Address address = Address.builder()
                .addressId(1L)
                .user(otherUser)
                .build();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // when
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            addressService.updateAddress(user, 1L, request);
        });

        // then
        assertEquals("해당 주소에 대한 수정 권한이 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("배송지 삭제")
    void deleteAddress_success() {
        // given
        Address address = Address.builder()
                .addressId(1L)
                .user(user)
                .build();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // when
        addressService.deleteAddress(user, 1L);

        // then
        verify(addressRepository).findById(1L);
    }
}
