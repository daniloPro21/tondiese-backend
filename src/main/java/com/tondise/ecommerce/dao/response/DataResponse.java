package com.tondise.ecommerce.dao.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse<T> {
    private boolean success;
    private T data;

    public static <T> DataResponse<T> of(T data) {
        return DataResponse.<T>builder().success(true).data(data).build();
    }
}
