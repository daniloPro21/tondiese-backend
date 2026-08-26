package com.tondise.utils.abstractModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@SuperBuilder
@AllArgsConstructor
@Data
@NoArgsConstructor
public abstract class AbstractDTO implements Serializable {

    private UUID id;

    protected boolean deleted;

    protected Instant deletedOn;

    protected Instant created;

    protected Instant updated;

}
