package com.example.backend.api;

import com.example.backend.exception.UserNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)                //https://stackoverflow.com/questions/58901288/springrunner-vs-springboottest
public class UserAPIIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void Valid_request_with_a_nonexisting_id_to_getUserById_method_should_throw_UserNotFoundException() throws Exception {

        long id = -1L;

        String uri = "/api/v1/user/" + id;
        String expectedExceptionMessage = "User not found with id : " + id;

        RequestBuilder request = get(uri).accept(APPLICATION_JSON);

        ResultActions actions = mockMvc.perform(request);

        actions.andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo(expectedExceptionMessage));
        actions.andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(UserNotFoundException.class));

        actions.andExpect(status().isNotFound());

    }

}
