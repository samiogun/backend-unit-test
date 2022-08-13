package com.example.backend.service;

import com.example.backend.dto.UserCreateDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.dto.UserViewDTO;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*****************************************************************************************************************************************
 @Mock ve @InjectMocks anotasyonlarının JUnit 4 te çalışması için bu(@RunWith(MockitoJUnitRunner.class)) anotasyonu eklememiz gerekiyor.
 JUnit 5 kullanıyorsak @ExtendWith(MockitoExtension.class) anotasyonunu sınıfımıza eklememiz gerekir.
 *****************************************************************************************************************************************/

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MockedStatic<UserViewDTO> userDTO;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Mock
    private Pageable pageable;

    private AutoCloseable closeable;

//    @Before
//    public void setUp() {
//        closeable = MockitoAnnotations.openMocks(this);
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        closeable.close();
//    }

    @Test
    public void Valid_Request_with_a_nonexisting_id_to_getUserById_method_should_throw_NotFoundException() {

        long id = -1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id : " + id);

        userDTO.verify(
                () -> UserViewDTO.of(any()),
                never()
        );

    }

    @Test
    public void Valid_Request_with_an_existing_id_to_getUserById_method_should_return_found_user() {

        long id = 1L;
        String firstName = "Sami Ogün";
        String lastName = "ERSUN";

        User user = new User("samiogun", firstName, lastName);
        user.setId(id);

        UserViewDTO expectedUserDto = new UserViewDTO(id, firstName, lastName);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        userDTO.when(() -> UserViewDTO.of(user)).thenReturn(expectedUserDto);

        UserViewDTO result = userService.getUserById(id);

//        verify(userService).getUserById(userViewDTOCaptor.capture().getId());

        assertUserDTOFields(expectedUserDto, result);
        assertThat(result).isEqualTo(expectedUserDto);

        verify(userRepository, times(1)).findById(id); // Burada times(1) olarak belirtmeyebilirdik, çünkü default olarak times(1) her zaman uygulanır.

    }

    @Test
    public void Valid_request_to_getUsers_method_should_return_a_list_of_all_users() {

        // given

        Random random = new Random();
        int size = random.nextInt(10);

        User user = new User();
        List<User> userList = new ArrayList<>();

        UserViewDTO userViewDTO = new UserViewDTO();
        List<UserViewDTO> userViewDTOList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            userList.add(user);
            userViewDTOList.add(userViewDTO);
        }

        // when

        when(userRepository.findAll()).thenReturn(userList);
        userDTO.when(() -> UserViewDTO.of(any(User.class))).thenReturn(userViewDTO);

        List<UserViewDTO> result = userService.getUsers();

        // then

        assertThat(result).isEqualTo(userViewDTOList);
        assertThat(result.size()).isEqualTo(userViewDTOList.size());

        verify(userRepository).findAll();// times(1) yazılmış gibi çalışır, default her verify metodu times(1) ' dir.
        userDTO.verify(
                () -> UserViewDTO.of(any(User.class)),
                times(size)
        );

    }

    @Test
    public void Valid_request_with_an_UserCreateDTO_object_to_createUser_method_should_return_UserViewDTO_object() {

        // given

        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .userName("Test-username")
                .firstName("Test-firstname")
                .lastName("Test-lastname")
                .build();

        User user = new User(userCreateDTO.getUserName(), userCreateDTO.getFirstName(), userCreateDTO.getLastName());
        user.setId(1L);

        UserViewDTO userViewDTO = new UserViewDTO(user.getId(), user.getFirstName(), user.getLastName());

        // when

        when(userRepository.save(any(User.class))).thenReturn(user);
        userDTO.when(() -> UserViewDTO.of(userArgumentCaptor.capture())).thenReturn(userViewDTO);

        UserViewDTO result = userService.createUser(userCreateDTO);

        // then

        assertThat(userArgumentCaptor.getValue()).isEqualTo(user);
        assertThat(result).isEqualTo(userViewDTO);
        userDTO.verify(
                () -> UserViewDTO.of(user)
        );

    }


    @Test
    public void Valid_request_with_an_existing_id_and_UserUpdateDTO_object_to_updateUser_method_should_return_UserViewDTO_object() {

        // given

        long id = 1L;

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .firstName("Test-firstname")
                .lastName("Test-lastname")
                .build();

        User user = new User("Test-username", userUpdateDTO.getFirstName(), userUpdateDTO.getLastName());
        user.setId(id);

        UserViewDTO userViewDTO = new UserViewDTO(user.getId(), user.getFirstName(), user.getLastName());

        // when

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        userDTO.when(() -> UserViewDTO.of(user)).thenReturn(userViewDTO);

        UserViewDTO result = userService.updateUser(id, userUpdateDTO);

        // then

        assertThat(result).isEqualTo(userViewDTO);

        verify(userRepository).findById(id);
        verify(userRepository).save(user);
        userDTO.verify(() -> UserViewDTO.of(user));

    }


    @Test
    public void Valid_request_with_a_nonexisting_id_and_UserUpdateDTO_object_to_updateUser_method_should_throw_UserNotFoundException() {

        // given

        long id = 1L;

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .firstName("Test-firstname")
                .lastName("Test-lastname")
                .build();

        // when

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // then

        assertThatThrownBy(() -> userService.updateUser(id, userUpdateDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id : " + id);

        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any(User.class));
        userDTO.verify(
                () -> UserViewDTO.of(any(User.class)),
                never()
        );

    }


    @Test
    public void Valid_request_with_an_existing_id_to_deleteUser_method_should_call_deleteById_method() {

        // given

        long id = 1L;

        User user = new User();
        user.setId(id);

        // when

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id);

        // then

        verify(userRepository).deleteById(id);

    }


    @Test
    public void Valid_request_with_a_nonexisting_id_to_deleteUser_method_should_throw_UserNotFoundException() {

        // given

        long id = 1L;

        // when

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // then

        assertThatThrownBy(() -> userService.deleteUser(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id : " + id);

        verify(userRepository, never()).deleteById(any());

    }

    @Test
    public void Valid_request_with_a_pageable_object_to_slice_method_should_return_list_of_UserViewDTO_objects() {

        // given

        Random random = new Random();
        int size = random.nextInt(10);

        User user = new User();
        List<User> userList = new ArrayList<>();

        UserViewDTO userViewDTO = new UserViewDTO();
        List<UserViewDTO> userViewDTOList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            userList.add(user);
            userViewDTOList.add(userViewDTO);
        }

        Page<User> userPage = new PageImpl<>(userList);

        // when

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        userDTO.when(() -> UserViewDTO.of(any(User.class))).thenReturn(userViewDTO);

        List<UserViewDTO> result = userService.slice(pageable);

        // then

        assertThat(result).isEqualTo(userViewDTOList);
        assertThat(result.size()).isEqualTo(userViewDTOList.size());

        verify(userRepository).findAll(pageable);
        userDTO.verify(
                () -> UserViewDTO.of(any(User.class)),
                times(size)
        );

    }


    @Test
    public void Valid_request_with_an_existing_username_to_isUsernameExists_method_should_return_true() {

        // given

        String userName = "Test-username";

        // when

        when(userRepository.existsUserByUserName(userName)).thenReturn(true);

        boolean result = userService.isUsernameExists(userName);

        // then

        assertThat(result).isTrue();

    }


    @Test
    public void Valid_request_with_a_nonexisting_username_to_isUsernameExists_method_should_return_false() {

        // given


        // when

        when(userRepository.existsUserByUserName(anyString())).thenReturn(false);

        boolean result = userService.isUsernameExists(anyString());

        // then

        assertThat(result).isFalse();

    }

    private void assertUserDTOFields(UserViewDTO expectedUserDto, UserViewDTO result) {
        assertThat(result.getFirstName()).isEqualTo(expectedUserDto.getFirstName());
        assertThat(result.getId()).isEqualTo(expectedUserDto.getId());
        assertThat(result.getLastName()).isEqualTo(expectedUserDto.getLastName());
    }


}