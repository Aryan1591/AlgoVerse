package com.algoverse.platform.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.algoverse.platform.entity.UserDetails;

public interface UserDetailsRepository  extends MongoRepository<UserDetails, String> {
    
}
