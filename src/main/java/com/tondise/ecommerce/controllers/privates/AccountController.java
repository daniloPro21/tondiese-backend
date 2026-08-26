package com.tondise.ecommerce.controllers.privates;

import com.tondise.ecommerce.config.security.CurrentUserResolver;
import com.tondise.ecommerce.dao.dto.UserDto;
import com.tondise.ecommerce.dao.models.User;
import com.tondise.ecommerce.dao.request.UpdatePasswordRequest;
import com.tondise.ecommerce.dao.request.UpdateProfileRequest;
import com.tondise.ecommerce.services.AuthService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Étend {@link AbstractController} sur {@link User} — l'entité la plus
 * sensible du système. {@code CREATE} (inscription via Keycloak, voir
 * {@code AuthController.register}), {@code SOFT_DELETE}/{@code DELETE}
 * (suppression de compte : pas de flux générique), et {@code FILTER}/
 * {@code SEARCH} (énumération de comptes/PII) restent désactivées — seules
 * {@code READ} et {@code UPDATE} sont ouvertes, <b>et chaque méthode ignore
 * délibérément l'{@code id} reçu dans l'URL</b> : elle opère toujours sur
 * l'utilisateur courant (résolu via le JWT), jamais sur l'id fourni par le
 * client. C'est la garde essentielle ici — sans elle, {@code GET/PUT /auth/{id}}
 * laisserait n'importe quel utilisateur authentifié lire ou modifier le
 * compte de n'importe qui d'autre.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Compte", description = "Consultation et mise à jour du profil de l'utilisateur connecté (jamais celui d'un autre utilisateur, même en forçant un id dans l'URL).")
public class AccountController extends AbstractController<User, UserDto, UpdateProfileRequest> {

    private final AuthService authService;
    private final CurrentUserResolver currentUserResolver;

    public AccountController(AuthService service, CurrentUserResolver currentUserResolver) {
        super(service);
        this.authService = service;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    protected Set<CrudOperation> allowedOperations() {
        return EnumSet.of(CrudOperation.READ, CrudOperation.UPDATE);
    }

    @Override
    @Operation(summary = "Récupérer mon profil",
            description = "Renvoie le profil de l'utilisateur actuellement connecté. L'id dans l'URL est ignoré : impossible de consulter le profil d'un autre utilisateur.")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(authService.me(currentUserId()));
    }

    @Override
    @Operation(summary = "Récupérer mon profil (liste à un élément)",
            description = "Forme héritée du CRUD générique : renvoie une liste contenant uniquement le profil de l'utilisateur connecté. Préférer GET /auth/profile.")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(List.of(authService.me(currentUserId())));
    }

    @Override
    @Operation(summary = "Récupérer mon profil (page à un élément)",
            description = "Forme paginée héritée du CRUD générique — il n'y a jamais qu'un seul profil pour l'utilisateur connecté.")
    @GetMapping("/all")
    public Page<UserDto> getAll(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(List.of(authService.me(currentUserId())), pageable, 1);
    }

    @Override
    @Operation(summary = "Compter mes comptes", description = "Renvoie toujours 1 : un utilisateur connecté a exactement un compte.")
    @GetMapping("/count")
    public long count() {
        return 1L;
    }

    @Override
    @Operation(summary = "Mettre à jour mon profil (forme héritée)",
            description = "Équivalent à PUT /auth/profile ; l'id dans l'URL est ignoré, seul le profil de l'utilisateur connecté est modifié.")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@Valid @PathVariable UUID id, @RequestBody UpdateProfileRequest dto) {
        return ResponseEntity.ok(authService.updateProfile(currentUserId(), dto));
    }

    @Operation(summary = "Mettre à jour mon profil",
            description = "Modifie prénom, nom et téléphone de l'utilisateur connecté.")
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(currentUserId(), request));
    }

    @Operation(summary = "Changer mon mot de passe",
            description = "Vérifie le mot de passe actuel puis le remplace par le nouveau (via Keycloak) et invalide la session en cours.")
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(currentUserId(), request);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUserResolver.resolveUserId(jwt);
    }
}
