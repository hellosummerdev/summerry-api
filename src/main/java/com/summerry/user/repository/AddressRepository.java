package com.summerry.user.repository;

import com.summerry.user.entity.Address;
import com.summerry.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findByUserAndIsDefaultTrue(User user);
}
