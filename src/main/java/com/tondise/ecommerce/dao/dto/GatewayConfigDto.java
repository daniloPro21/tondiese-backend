package com.tondise.ecommerce.dao.dto;

import java.util.List;
import java.util.Map;
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
public class GatewayConfigDto {
    private String gateway;
    private Map<String, Object> config;
    private List<String> methods;
}
