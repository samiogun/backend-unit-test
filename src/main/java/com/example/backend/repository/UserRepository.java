package com.example.backend.repository;

import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User , Long>{

//    boolean existsUserByuserName(String username);
    boolean existsUserByUserName(String username);

    //Yukarıdaki iki metodda database'e aynı sorguyu atar.

}
