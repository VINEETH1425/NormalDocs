package com.storeconnect.inventory.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDTO {
    private String id;
    private String name;
    private String code;
    private String address;
    private String city;
    private String phone;
    private Boolean isActive;

    public String getStoreId() {
        return id;
    }
}
