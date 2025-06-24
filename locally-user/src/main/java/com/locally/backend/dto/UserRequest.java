package com.locally.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String firstName;
    private String lastName;
    private String gender;
    private String state;
    private String country;
    private String address;
    private String email;
    private String phoneNumber;
    private String alternatePhoneNumber;
    private String password;
}