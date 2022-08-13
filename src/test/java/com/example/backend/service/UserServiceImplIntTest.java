package com.example.backend.service;

import com.example.backend.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


/****************************************************************************************************************************
 Burada sadece @SpringBootTest anotasyonu ile çalıştırabildik(Bu classta JUnit 5 kullanıyoruz).
 Çünkü bu anotasyon JUnit 5 ile entegre olan @ExtendWith({SpringExtension.class}) anotasyonunu içerir.
 Diğer Integration Test classımızda JUnit 4 kullandığımız için sadece @SpringBootTest anotasyonu yetmeyecektir.
 Bir de @RunWith(SpringRunner.class) anotasyonunu eklememiz gerekir ki @Autowired anotasyonu gerekli işlemi yapabilsin.
 *****************************************************************************************************************************/

@SpringBootTest
@RunWith(SpringRunner.class)
class UserServiceImplIntTest {

    @Autowired
    private UserService userService;

    @Test
    public void Valid_Request_with_a_nonexisting_id_to_getUserById_method_should_throw_NotFoundException() {

        long id = -1L;

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id : " + id);

    }

}