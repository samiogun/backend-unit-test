package com.example.backend.dto;

import com.example.backend.model.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public final class UserViewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String firstName;
    private String lastName;

    public UserViewDTO(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static UserViewDTO of(User user) {
        return new UserViewDTO(user.getId(), user.getFirstName(), user.getLastName());
    }

}
