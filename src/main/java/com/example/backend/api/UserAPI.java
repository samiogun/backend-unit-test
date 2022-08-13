package com.example.backend.api;

import com.example.backend.dto.UserCreateDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.dto.UserViewDTO;
import com.example.backend.service.UserService;
import com.example.backend.shared.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class UserAPI {

    private final UserService userService;

    @GetMapping("v1/user/{id}")
    public ResponseEntity<UserViewDTO> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


//    @PostMapping("v1/user")
//    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO){
//
//        userService.createUser(userCreateDTO);
//        return ResponseEntity.ok(new GenericResponse("User Created."));
//
//    }

    /******************************************************************************************************************************************
     @ResponseBody - Geri döndürülecek objeyi otomatikman JSON'a çevirir. RestController anatosyonu ile bu özelliği sağlamış oluyoruz.
     Bu nedenle @ResponseBody zorunlu değil, koymasakta olur.
     ********************************************************************************************************************************************/

    @PostMapping(path = "v1/user")
    public GenericResponse createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {

        userService.createUser(userCreateDTO);
        return new GenericResponse("User Created !");

    }

    @GetMapping("v1/user")
    public List<UserViewDTO> getUsers() {

//        final List<UserViewDTO> users = userService.getUsers();
//        return ResponseEntity.ok(users);
//        return new ResponseEntity<>(users, new HttpHeaders(), HttpStatus.OK);
        return userService.getUsers();

    }

    @PutMapping("v1/user/{id}")
    public ResponseEntity<UserViewDTO> updateUser(@PathVariable("id") Long userId, @RequestBody UserUpdateDTO userUpdateDTO) {

        final UserViewDTO user = userService.updateUser(userId, userUpdateDTO);
        return ResponseEntity.ok(user);

    }


    @DeleteMapping("v1/user/{id}")
    public ResponseEntity<GenericResponse> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);
        return ResponseEntity.ok(new GenericResponse("User deleted !"));

    }


    @GetMapping("v1/user/slice")
    public ResponseEntity<List<UserViewDTO>> slice(Pageable pageable) {

        final List<UserViewDTO> users = userService.slice(pageable);
        return ResponseEntity.ok(users);

    }

    @RequestMapping(path = "v1/user", method = RequestMethod.PATCH)
    public boolean isUsernameExists(@RequestParam(value = "username") String username) {

        return userService.isUsernameExists(username);

    }

}