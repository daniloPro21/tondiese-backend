package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tondise.utils.abstractModel.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends BaseRepository<Product> {

    Optional<Product> findBySlug(String slug);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    Page<Product> findByFeaturedTrue(Pageable pageable);

    @Query("""
            select p from Product p
            where lower(p.name) like lower(concat('%', :query, '%'))
               or lower(p.shortDescription) like lower(concat('%', :query, '%'))
            """)
    Page<Product> search(@Param("query") String query, Pageable pageable);
}
