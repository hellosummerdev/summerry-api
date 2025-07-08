package com.summerry.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ADDRESSES")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 외래키
    private User user;

    private String receiverName;
    private String phone;
    private String addressLine1; // 도로명
    private String addressLine2; // 상세주소
    private String postalCode;

    private Boolean isDefault;
    private Boolean deleteYn;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.deleteYn = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
