package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.CategoryDto;
import com.tondise.ecommerce.dao.mappers.CategoryMapper;
import com.tondise.ecommerce.dao.models.Category;
import com.tondise.ecommerce.dao.repository.CategoryRepository;
import com.tondise.ecommerce.dao.request.CategoryRequest;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService extends AbstractService<Category, CategoryDto, CategoryRequest> {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper,
                            CacheManager cacheManager) {
        super(categoryRepository, cacheManager, Category.class.getName());
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryDto> getCategories() {
        return categoryMapper.toDtoList(categoryRepository.findByParentIsNull());
    }

    public CategoryDto getBySlug(String slug) {
        return categoryMapper.toDto(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable: " + slug)));
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .image(request.getImage())
                .parent(request.getParentId() != null ? findOrThrow(request.getParentId()) : null)
                .build();
        CategoryDto saved = categoryMapper.toDto(categoryRepository.save(category));
        clearCache();
        return saved;
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryRequest request, UUID id) {
        Category category = findOrThrow(id);
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setImage(request.getImage());
        category.setParent(request.getParentId() != null ? findOrThrow(request.getParentId()) : null);
        CategoryDto updated = categoryMapper.toDto(categoryRepository.save(category));
        clearCache();
        return updated;
    }

    @Override
    protected CategoryDto convertToDTO(Category model) {
        return categoryMapper.toDto(model);
    }

    @Override
    protected Category convertToModel(CategoryDto dto) {
        return categoryMapper.toModel(dto);
    }

    @Override
    protected Class<Category> getEntityClass() {
        return Category.class;
    }

    private Category findOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable: " + id));
    }
}
