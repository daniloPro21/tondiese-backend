package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.Address;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AddressRepository extends BaseRepository<Address> {

    List<Address> findByUserId(UUID userId);

    Page<Address> findByUserId(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    Optional<Address> findByIdAndUserId(UUID id, UUID userId);
}
