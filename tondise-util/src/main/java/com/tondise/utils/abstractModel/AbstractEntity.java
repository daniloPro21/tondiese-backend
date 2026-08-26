package com.tondise.utils.abstractModel;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
//@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "deleted", type = Boolean.class))
//@Filter(name = "deletedFilter", condition = "deleted = :deleted")
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    @Builder.Default
    public boolean deleted = false;

    @Column(name = "deleted_on", columnDefinition = "timestamptz")
    public Instant deletedOn;

    @Column(nullable = false, name = "created_at", columnDefinition = "timestamptz")
    public Instant created = Instant.now();

    @Column(name = "updated_at", columnDefinition = "timestamptz")
    public Instant updated;

    @PrePersist
    public void onCreate() {
        this.created = Instant.now();
        this.updated = created;
    }

    @PreUpdate
    public void onUpdate() {
        this.updated = Instant.now();
    }

    @PreDestroy
    public void onDestroy() {
        this.deleted = true;
        this.deletedOn = Instant.now();
    }

}