package com.tondise.ecommerce.dao.dto;

import com.tondise.utils.abstractModel.AbstractDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto extends AbstractDTO {
    private String name;
    private String slug;
    private String description;
    private String image;
    private List<CategoryDto> children;
}
