package com.stegnography.Repository;

import com.stegnography.domain.UserDetails;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
//import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends CrudRepository<UserDetails, BigInteger> {
    UserDetails findByEmail(String email);
    UserDetails findPasswordByEmail(String email);

//    String findEmailByPhoneNumber(BigInteger phone_no);
}
