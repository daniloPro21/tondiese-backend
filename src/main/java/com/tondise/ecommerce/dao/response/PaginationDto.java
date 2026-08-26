package com.tondise.ecommerce.dao.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDto {
    private int page;
    private int limit;
    private long total;
    private int totalPages;

    public static PaginationDto from(Page<?> page) {
        return PaginationDto.builder()
                .page(page.getNumber())
                .limit(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
