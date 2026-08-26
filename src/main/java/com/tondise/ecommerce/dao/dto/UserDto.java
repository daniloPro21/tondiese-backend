package com.tondise.ecommerce.dao.dto;

import com.tondise.utils.abstractModel.AbstractDTO;
import java.time.Instant;
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
public class UserDto extends AbstractDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String avatar;
    private Instant emailVerifiedAt;
}
