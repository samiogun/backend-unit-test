package com.example.backend.service;

import com.example.backend.dto.UserCreateDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.dto.UserViewDTO;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.repository.UserRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*************************************************************************************************************************************************
 Burada sadece @SpringBootTest anotasyonu yetmez.
 @Autowired anotasyonunun işlevini yerine getirmesi için  @RunWith(SpringRunner.class) anotasyonunu da kullanmalıyız.
 Çünkü JUnit 4 te @SpringBootTest anotasyonunun içerdiği @ExtendWith(SpringExtension.class) anotasyonu yerine
 @RunWith(SpringRunner.class) anotasyonunu kullanmamız gerekir. JUnit 5 te @ExtendWith(SpringExtension.class) anotasyonunu kullanabiliriz.
 **************************************************************************************************************************************************/


@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @After
    public void tearDown() throws Exception {
        userRepository.deleteAll();
    }

    @Test
    public void Valid_Request_with_a_nonexisting_id_to_getUserById_method_should_throw_NotFoundException() {

        long id = -1L;

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id : " + id);

    }


    @Test
    public void Valid_Request_with_an_existing_id_to_getUserById_method_should_return_UserViewDTO_object() {

        // given

        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .userName("Test-username")
                .firstName("Test-firstname")
                .lastName("Test-lastname")
                .build();

        UserViewDTO userViewDTO = userService.createUser(userCreateDTO);

        // when

        UserViewDTO userViewDTODb = userService.getUserById(userViewDTO.getId());

        // then

        assertThat(userViewDTO).isEqualTo(userViewDTODb);

    }

    @Test
    public void Valid_Request_to_getUsers_method_should_return_list_of_UserViewDTO_objects() {

        // given

        Random random = new Random();
        int size = random.nextInt(10);

        List<UserViewDTO> userViewDTOList = new ArrayList<>();

        for (int i = 0; i < size; i++) {

            String username = "Test-username" + i;

            UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                    .userName(username)
                    .firstName("Test-firstname")
                    .lastName("Test-lastname")
                    .build();

            UserViewDTO userViewDTO = userService.createUser(userCreateDTO);

            userViewDTOList.add(userViewDTO);

        }

        // when

        List<UserViewDTO> userViewDTOListDb = new ArrayList<>();

        userViewDTOListDb = userService.getUsers();

        // then

        assertThat(userViewDTOList.size()).isEqualTo(userViewDTOListDb.size());
        assertThat(userViewDTOList).isEqualTo(userViewDTOListDb);

    }

    @Test
    public void Valid_request_with_a_CreateUserDTO_object_to_createUser_method_should_return_UserViewDTO_object_of_saved_User() {

        // given

        String username = "Test-username";
        String firstname = "Test-firstname";
        String lastname = "Test-lastname";

        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .userName(username)
                .firstName(firstname)
                .lastName(lastname)
                .build();

        // when

        UserViewDTO userViewDTO = userService.createUser(userCreateDTO);

        // then

        assertThat(userViewDTO.getId() > 0).isTrue();
        assertThat(userViewDTO.getFirstName()).isEqualTo(firstname);
        assertThat(userViewDTO.getLastName()).isEqualTo(lastname);

    }


    @Test
    public void Valid_Request_with_a_nonexisting_id_to_updateUser_method_should_throw_NotFoundException() {

        long id = -1L;

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder().build();

        assertThatThrownBy(() -> userService.updateUser(id, userUpdateDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id : " + id);

    }


    @Test
    public void Valid_Request_with_an_existing_id_to_updateUser_method_should_return_UserViewDTO_object_of_updated_User() {

        // given

        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .userName("Test-username")
                .firstName("Test-firstname")
                .lastName("Test-lastname")
                .build();

        UserViewDTO userViewDTO = userService.createUser(userCreateDTO);

        String updatedFirstname = "Test-updatedusername";
        String updatedLastname = "Test-updatedlastname";

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .firstName(updatedFirstname)
                .lastName(updatedLastname)
                .build();

        // when

        UserViewDTO updatedUserViewDTO = userService.updateUser(userViewDTO.getId(), userUpdateDTO);

        // then

        assertThat(updatedUserViewDTO.getId()).isEqualTo(userViewDTO.getId());
        assertThat(updatedUserViewDTO.getFirstName()).isEqualTo(updatedFirstname);
        assertThat(updatedUserViewDTO.getLastName()).isEqualTo(updatedLastname);

    }

    @Test
    public void Valid_request_with_a_nonexisting_id_to_deleteUser_method_should_throw_UserNotFoundException() {

        long id = -1L;

        assertThatThrownBy(() -> userService.deleteUser(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id : " + id);

    }

    @Test
    public void Valid_request_with_an_existing_id_to_deleteUser_method_should_delete_User() {

        // given

        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .userName("Test-username")
                .firstName("Test-firstname")
                .lastName("Test-lastname")
                .build();

        UserViewDTO userViewDTO = userService.createUser(userCreateDTO);

        long id = userViewDTO.getId();

        // when

        userService.deleteUser(id);

        // then

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id : " + id);

    }

    @Test
    public void Valid_request_with_a_pageable_object_to_slice_method_should_return_page_of_objects() {

        // given

        Random random = new Random();
        int size = random.nextInt(10) + 10;

        List<UserViewDTO> userViewDTOList = new ArrayList<>();

        for (int i = 0; i < size; i++) {

            String username = "Test-username" + i;

            UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                    .userName(username)
                    .firstName("Test-firstname")
                    .lastName("Test-lastname")
                    .build();

            UserViewDTO userViewDTO = userService.createUser(userCreateDTO);

            userViewDTOList.add(userViewDTO);

        }

        int pageSize = 3;

        PageRequest pageRequest = PageRequest.of(1, pageSize);
        PageRequest pageRequest1 = PageRequest.of(0, pageSize);

        // when

        List<UserViewDTO> userViewDTOPageList = userService.slice(pageRequest);
        List<UserViewDTO> userViewDTOPageList1 = userService.slice(pageRequest1);

        // then

        assertThat(userViewDTOPageList.size()).isEqualTo(userViewDTOPageList1.size());

        for (int i = 0; i < pageSize; i++) {

            assertThat(userViewDTOList.get(i)).isEqualTo(userViewDTOPageList1.get(i));

        }

    }

    @Test
    public void Valid_request_with_a_nonexisting_username_to_isUsernameExists_method_should_return_false() {

        // given

        String username = "non-existingusername";

        // when

        boolean result = userService.isUsernameExists(username);

        // then

        assertThat(result).isFalse();

    }

    @Test
    public void Valid_request_with_an_existing_username_to_isUsernameExists_method_should_return_true() {

        // given

        String username = "Test-username";

        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .userName(username)
                .firstName("Test-firstname")
                .lastName("Test-lastname")
                .build();

        userService.createUser(userCreateDTO);

        // when

        boolean result = userService.isUsernameExists(username);

        // then

        assertThat(result).isTrue();

    }


}