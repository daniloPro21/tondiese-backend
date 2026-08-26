package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;

public interface CategoryRepository extends BaseRepository<Category> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNull();

    List<Category> findByParentId(UUID parentId);
}
