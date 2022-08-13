package com.example.backend.api;

import com.example.backend.dto.UserCreateDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.dto.UserViewDTO;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(value = UserAPI.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class UserAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void Valid_request_with_a_nonexisting_id_to_getUserById_method_should_throw_UserNotFoundException() throws Exception {

        // given

        long id = -1L;
        String url = "/api/v1/user/" + id;

        // when

        when(userService.getUserById(id)).thenThrow(new UserNotFoundException("User not found with id : " + id));

        RequestBuilder request = get(url).accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);
                /*.andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(UserNotFoundException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo(expectedExceptionMessage));*/

        // then

        verify(userService).getUserById(id);

        actions.andExpect(status().isNotFound());
        actions.andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(UserNotFoundException.class));
        actions.andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("User not found with id : " + id));

    }

    @Test
    public void Valid_request_with_an_existing_id_to_getUserById_method_should_return_ResponseEntitiy_of_UserViewDTO() throws Exception {

        // given

        long id = 1L;
        String uri = "/api/v1/user/" + id;
        String firstName = "Test-firstname";
        String lastName = "Test-lastname";

        UserViewDTO userViewDTO = new UserViewDTO(id, firstName, lastName);

        // when

        when(userService.getUserById(id)).thenReturn(userViewDTO);

        RequestBuilder request = get(uri).contentType(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        // then

        verify(userService).getUserById(id);

        actions.andExpect(status().isOk());
        actions.andExpect(content().contentType(APPLICATION_JSON));
        actions.andExpect(jsonPath("$.id").value(id));
        actions.andExpect(jsonPath("$.firstName").value(firstName));
        actions.andExpect(jsonPath("$.lastName").value(lastName));

    }


    @Test
    public void Valid_bad_request_with_id_as_String_to_getUserById_method_should_return_HTTPBadRequest() throws Exception {

        // given

        String id = "test";
        String uri = "/api/v1/user/" + id;

        // when

        RequestBuilder request = get(uri).contentType(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        // then

        actions.andExpect(status().isBadRequest());

    }

    @Test
    public void Valid_bad_request_with_id_as_Double_to_getUserById_method_should_return_HTTPBadRequest() throws Exception {

        // given

        double id = 1.1568;
        String uri = "/api/v1/user/" + id;

        // when

        ResultActions actions = mockMvc.perform(get(uri).contentType(APPLICATION_JSON));

        // then

        actions.andExpect(status().isBadRequest());

    }

    @Test
    public void Valid_request_with_UserCreateDTO_objet_to_createUser_method_should_success_and_return_GenericResponseMessage() throws Exception {

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

        when(userRepository.existsUserByUserName(username)).thenReturn(false);
        when(userService.createUser(userCreateDTO)).thenReturn(new UserViewDTO());

        String uri = "/api/v1/user";

        RequestBuilder request = post(uri)
                .content(objectMapper.writeValueAsString(userCreateDTO))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        // then

        verify(userService).createUser(userCreateDTO);
        actions.andExpect(result -> assertThat(jsonPath("$.message").value("User Created !")));

    }


    @Test
    public void Valid_request_with_UserCreateDTO_objet_which_has_existing_username_to_createUser_method_should_fail_and_return_bad_request() throws Exception {

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

        when(userRepository.existsUserByUserName(username)).thenReturn(true);
        when(userService.createUser(userCreateDTO)).thenReturn(new UserViewDTO());

        String uri = "/api/v1/user";

        RequestBuilder request = post(uri)
                .content(objectMapper.writeValueAsString(userCreateDTO))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        // then

        actions.andExpect(status().isBadRequest());

        String warningMessage = "User name must be unique";

        actions.andExpect(mvcResult -> assertThat(jsonPath("$.errors.defaultMessage").value(warningMessage)));

    }


    @Test
    public void Valid_Request_with_zero_record_in_DB_to_getUsers_method_should_return_empty_list_of_users() throws Exception {

        // given

        String uri = "/api/v1/user";

        // when

        when(userService.getUsers()).thenReturn(Collections.emptyList());

        RequestBuilder request = get(uri).accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        List<UserViewDTO> users = objectMapper.readValue(actions.andReturn().getResponse().getContentAsString(), new TypeReference<List<UserViewDTO>>() {
        });

        // then

        verify(userService).getUsers();
        actions.andExpect(status().isOk());
        assertThat(users).isEqualTo(Collections.emptyList());
        assertThat(users.size()).isEqualTo(Collections.emptyList().size());

    }


    @Test
    public void Valid_Request_to_getUsers_method_should_return_list_of_users() throws Exception {

        // given

        Random random = new Random();
        int records = random.nextInt(10) + 1;

        String uri = "/api/v1/user";

        List<UserViewDTO> userViewDTOList = new ArrayList<>();

        for (int i = 0; i < records; i++) {

            userViewDTOList.add(new UserViewDTO());

        }

        // when

        when(userService.getUsers()).thenReturn(userViewDTOList);

        RequestBuilder request = get(uri).accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        List<UserViewDTO> usersReturned = objectMapper.readValue(actions.andReturn().getResponse().getContentAsString(), new TypeReference<List<UserViewDTO>>() {
        });

        // then

        verify(userService).getUsers();
        actions.andExpect(status().isOk());
        assertThat(usersReturned).isEqualTo(userViewDTOList);
        assertThat(usersReturned.size()).isEqualTo(userViewDTOList.size());

    }

    @Test
    public void Valid_request_with_a_nonexisting_id_to_updateUser_method_should_throw_UserNotFoundException() throws Exception {

        // given

        long id = -1L;

        String uri = "/api/v1/user/" + id;

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder().build();

        // when

        /*********************************************************************************************************************************************
         * Aşağıdaki stubbing de updateUser methoduna bir tane raw değer ve bir tane matcher olarak girdi veremiyoruz. Hata alırız.
         * Stubbing de ya ikisi birden raw value olacak ya da ikisi birden matcher olacak. Bu nedenle captur.capture(), any(Some.class), any() gibi
         * matcherlar kullanmamız gerekirse hepsin matcher'a çevirmemiz gerekir.
         * ********************************************************************************************************************************************/
        when(userService.updateUser(id, userUpdateDTO)).thenThrow(new UserNotFoundException("User not found with id : " + id));

        RequestBuilder request = put(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userUpdateDTO))
                .accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        // then

        verify(userService).updateUser(id, userUpdateDTO);
        actions.andExpect(status().isNotFound());
        actions.andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("User not found with id : " + id));

    }


    @Test
    public void Valid_request_with_an_existing_id_to_updateUser_method_should_return_UserViewDTO_of_updated_user() throws Exception {

        // given

        long id = 1L;

        String uri = "/api/v1/user/" + id;

        String firstname = "Test-firstname";
        String lastname = "Test-lastname";

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .firstName(firstname)
                .lastName(lastname)
                .build();

        UserViewDTO userViewDTO = new UserViewDTO(id, firstname, lastname);

        // when

        /*********************************************************************************************************************************************
         * Aşağıdaki stubbing de updateUser methoduna bir tane raw değer ve bir tane matcher olarak girdi veremiyoruz. Hata alırız.
         * Stubbing de ya ikisi birden raw value olacak ya da ikisi birden matcher olacak. Bu nedenle captur.capture(), any(Some.class), any() gibi
         * matcherlar kullanmamız gerekirse hepsin matcher'a çevirmemiz gerekir.
         * ********************************************************************************************************************************************/
        when(userService.updateUser(id, userUpdateDTO)).thenReturn(userViewDTO);

        RequestBuilder request = put(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userUpdateDTO))
                .accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        // then

        verify(userService).updateUser(id, userUpdateDTO);
        actions.andExpect(status().isOk());
        actions.andExpect(mvcResult -> assertThat(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserViewDTO.class)).isEqualTo(userViewDTO));

    }


    @Test
    public void Valid_request_with_a_nonexisting_id_to_deleteUser_method_should_throw_UserNotFoundException() throws Exception {

        // given

        long id = -1L;

        String uri = "/api/v1/user/" + id;

        // when

        /*****************************************************************************************************************************************
         * userService.deleteUser() methodu void bir method olduğu için when().thenReturn() ya da when().thenThrow() kalıplarını kullanamıyoruz.
         * Bu kalıp yerine doThrow().when().somemethod(), doNothing().when(captor.capture()).somemethod() gibi kalıpları kullanmamız gerekir.
         *****************************************************************************************************************************************/
        doThrow(new UserNotFoundException("User not found with id : " + id)).when(userService).deleteUser(id);

        RequestBuilder request = delete(uri).accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        // then

        verify(userService).deleteUser(id);
        actions.andExpect(status().isNotFound());
        actions.andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("User not found with id : " + id));

    }


    @Test
    public void Valid_request_with_an_existing_id_to_deleteUser_method_should_return_GenericResponse() throws Exception {

        // given

        long id = 1L;

        String uri = "/api/v1/user/" + id;

        String genericResponse = "User deleted !";

        // when

        RequestBuilder request = delete(uri).accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        // then

        actions.andExpect(status().isOk());
        actions.andExpect(jsonPath(".message").value(genericResponse));

    }


    @Test
    public void Valid_request_with_pageable_object_to_slice_method_should_return_slice_of_UserViewDTOs() throws Exception {

        // given

        String uri = "/api/v1/user/slice";

        PageRequest page = PageRequest.of(1, 3);

        List<UserViewDTO> userViewDTOList = Collections.singletonList(new UserViewDTO());

        // when

        when(userService.slice(page)).thenReturn(userViewDTOList);

        RequestBuilder request = get(uri)
                .accept(APPLICATION_JSON)
                .queryParam("page", "1")
                .queryParam("size", "3");

        ResultActions actions = mockMvc.perform(request);

        // then

        String response = actions.andReturn().getResponse().getContentAsString();
        List<UserViewDTO> responseList = objectMapper.readValue(response, new TypeReference<List<UserViewDTO>>() {
        });

        verify(userService).slice(page);
        actions.andExpect(status().isOk());
        assertThat(responseList).isEqualTo(userViewDTOList);
        assertThat(responseList.size()).isEqualTo(userViewDTOList.size());

    }


    @Test
    public void Valid_request_with_a_nonexisting_username_to_isUsernameExists_method_should_return_false() throws Exception {

        // given

        String username = "non-existingusername";

        String uri = "/api/v1/user";

        // when

        when(userService.isUsernameExists(username)).thenReturn(false);

        RequestBuilder request = patch(uri).queryParam("username", username);

        ResultActions actions = mockMvc.perform(request);

        // then

        verify(userService).isUsernameExists(username);
        assertThat(actions.andReturn().getResponse().getContentAsString()).isEqualTo("false");

    }


    @Test
    public void Valid_request_with_an_existing_username_to_isUsernameExists_method_should_return_true() throws Exception {

        // given

        String username = "existing-username";

        String uri = "/api/v1/user";

        // when

        when(userService.isUsernameExists(username)).thenReturn(true);

        RequestBuilder request = patch(uri).queryParam("username", username);

        ResultActions actions = mockMvc.perform(request);

        // then

        verify(userService).isUsernameExists(username);
        assertThat(actions.andReturn().getResponse().getContentAsString()).isEqualTo("true");

    }


}