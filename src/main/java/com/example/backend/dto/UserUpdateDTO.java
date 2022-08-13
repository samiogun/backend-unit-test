package com.example.backend.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UserUpdateDTO {

    private String firstName;
    private String lastName;

}
