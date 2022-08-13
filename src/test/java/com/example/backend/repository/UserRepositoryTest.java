package com.example.backend.repository;

import com.example.backend.model.User;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @DataJpaTest anotasyonu H2 veri tabanını aktif eder ve sadece test için gerekli konfigürasyonları yapar.
 * userRepository objesini @Autowired anotasyonu ile inject etmek için
 * bu anotasyonu kullandık(@RunWith(SpringRunner.class)). Junit 4 te bu anotasyonu kullanmamız gerekiyor. JUnit 5 kullanmış olsaydık,
 * @DataJpaTest anotasyonunun içinde mevcut olan @ExtendWith(SpringExtension.class) anotasyonu ile @Autowired anotasyonunun
 * çalışmasını sağlayacaktık.
 */


@DataJpaTest
@RunWith(SpringRunner.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @After
    public void setUp() throws Exception {

        userRepository.deleteAll();

    }

    @Test
    public void Valid_request_with_an_existing_username_to_getUserById_method_should_return_true() {

        User user = new User("samiogun", "Sami Ogün", "ERSUN");

        userRepository.save(user);

        boolean actual = userRepository.existsUserByUserName(user.getUserName());

        assertThat(actual).isTrue();


    }

    @Test
    public void Valid_request_with_a_nonexisting_username_to_getUserById_method_should_return_false() {

        String username = "doesntexist";

        boolean actual = userRepository.existsUserByUserName(username);

        assertThat(actual).isFalse();


    }

}