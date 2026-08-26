package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.User;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;

public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakUserId(String keycloakUserId);

    boolean existsByEmail(String email);
}
