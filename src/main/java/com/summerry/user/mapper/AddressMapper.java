package com.summerry.user.mapper;
import com.summerry.user.dto.AddressResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {
    void clearDefaultForUser(Long userId);
    List<AddressResponse> findAllByUserId(Long userId);

    void updateAddress(
            @Param("addressId") Long addressId,
            @Param("userId") Long userId,
            @Param("receiverName") String receiverName,
            @Param("phone") String phone,
            @Param("addressLine1") String addressLine1,
            @Param("addressLine2") String addressLine2,
            @Param("postalCode") String postalCode,
            @Param("isDefault") Boolean isDefault,
            @Param("updatedBy") Long updatedBy
    );
}
