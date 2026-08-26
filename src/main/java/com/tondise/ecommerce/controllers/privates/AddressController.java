package com.tondise.ecommerce.controllers.privates;

import com.tondise.ecommerce.config.security.CurrentUserResolver;
import com.tondise.ecommerce.dao.dto.AddressDto;
import com.tondise.ecommerce.dao.models.Address;
import com.tondise.ecommerce.dao.request.AddressRequest;
import com.tondise.ecommerce.services.AddressService;
import com.tondise.utils.abstractController.AbstractController;
import com.tondise.utils.abstractController.CrudOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Étend {@link AbstractController}, mais {@code Address} appartient à un
 * utilisateur — contrairement au CRUD admin (Category/Product), les routes
 * {@code /{id}} héritées n'ont aucune notion de propriétaire. Chaque route
 * qui prend un id ou renvoie une liste est donc <b>redéfinie ici</b> pour
 * vérifier/filtrer par l'utilisateur courant avant de déléguer à
 * {@link AddressService}. {@code findWithFilter}/{@code search} restent
 * désactivées ({@link #allowedOperations()}) : leur filtrage par colonne
 * arbitraire n'a pas de garde-fou par propriétaire.
 */
@RestController
@RequestMapping("/addresses")
@Tag(name = "Adresses", description = "CRUD des adresses (livraison/facturation) de l'utilisateur connecté. Toutes les routes sont limitées à ses propres adresses.")
public class AddressController extends AbstractController<Address, AddressDto, AddressRequest> {

    private final AddressService addressService;
    private final CurrentUserResolver currentUserResolver;

    public AddressController(AddressService service, CurrentUserResolver currentUserResolver) {
        super(service);
        this.addressService = service;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    protected Set<CrudOperation> allowedOperations() {
        return EnumSet.complementOf(EnumSet.of(CrudOperation.FILTER, CrudOperation.SEARCH));
    }

    @Override
    @Operation(summary = "Ajouter une adresse",
            description = "Crée une nouvelle adresse pour l'utilisateur connecté. Si isDefault=true, désactive l'adresse par défaut précédente.")
    @PostMapping
    public ResponseEntity<AddressDto> create(@Valid @RequestBody AddressRequest dto) {
        return ResponseEntity.ok(addressService.createAddress(currentUserId(), dto));
    }

    @Override
    @Operation(summary = "Récupérer une adresse",
            description = "Renvoie une adresse par son id, uniquement si elle appartient à l'utilisateur connecté (404 sinon).")
    @GetMapping("/{id}")
    public ResponseEntity<AddressDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(addressService.getAddress(currentUserId(), id));
    }

    @Override
    @Operation(summary = "Lister mes adresses", description = "Renvoie toutes les adresses de l'utilisateur connecté, sans pagination.")
    @GetMapping
    public ResponseEntity<List<AddressDto>> getAll() {
        return ResponseEntity.ok(addressService.getAddresses(currentUserId()));
    }

    @Override
    @Operation(summary = "Lister mes adresses (paginé)", description = "Renvoie les adresses de l'utilisateur connecté, page par page.")
    @GetMapping("/all")
    public Page<AddressDto> getAll(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return addressService.getAddresses(currentUserId(), PageRequest.of(page, size));
    }

    @Override
    @Operation(summary = "Compter mes adresses", description = "Renvoie le nombre d'adresses enregistrées par l'utilisateur connecté.")
    @GetMapping("/count")
    public long count() {
        return addressService.countAddresses(currentUserId());
    }

    @Override
    @Operation(summary = "Modifier une adresse",
            description = "Met à jour une adresse existante appartenant à l'utilisateur connecté (404 si elle appartient à quelqu'un d'autre ou n'existe pas).")
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> update(@Valid @PathVariable UUID id, @RequestBody AddressRequest dto) {
        return ResponseEntity.ok(addressService.updateAddress(currentUserId(), id, dto));
    }

    @Override
    @Operation(summary = "Supprimer une adresse (logique)",
            description = "Marque l'adresse comme supprimée sans effacer la ligne en base (réversible côté données).")
    @PutMapping("softdelete/{id}")
    public ResponseEntity<AddressDto> softdelete(@PathVariable UUID id) {
        return ResponseEntity.ok(addressService.softDeleteAddress(currentUserId(), id));
    }

    @Override
    @Operation(summary = "Supprimer une adresse (définitive)",
            description = "Supprime définitivement l'adresse de la base de données.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        addressService.deleteAddress(currentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUserResolver.resolveUserId(jwt);
    }
}
