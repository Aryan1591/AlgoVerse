package com.algoverse.platform.repository_security;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.algoverse.platform.entity_security.UserDetails;

public interface UserDetailsRepository  extends MongoRepository<UserDetails, String> {
    
}
