package com.tondise.ecommerce.dao.response;

import java.util.List;
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
public class PaginatedResponse<T> {
    private boolean success;
    private List<T> data;
    private PaginationDto pagination;

    public static <E, T> PaginatedResponse<T> of(Page<E> page, List<T> mappedContent) {
        return PaginatedResponse.<T>builder()
                .success(true)
                .data(mappedContent)
                .pagination(PaginationDto.from(page))
                .build();
    }
}
