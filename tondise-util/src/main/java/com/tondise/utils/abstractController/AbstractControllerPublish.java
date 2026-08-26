package com.tondise.utils.abstractController;

import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.abstractModel.AbstractDTO;
import com.tondise.utils.abstractModel.AbstractEntity;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
public abstract class AbstractControllerPublish<T extends AbstractEntity, D extends AbstractDTO, K> {


    private final AbstractService<T, D, K> service;


    protected AbstractControllerPublish(AbstractService<T, D, K> service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<D> create(@Valid  @RequestBody K dto) {
        return ResponseEntity.ok(service.create(dto));
    }


    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<D> getById(@PathVariable UUID id) {
        D entity = service.findById(id);
        return ResponseEntity.ok(entity);
    }


    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<D> update(@PathVariable UUID id, @Valid @RequestBody K dto) {
        D updatedEntity = service.update(dto, id);
        return ResponseEntity.ok(updatedEntity);
    }
}
