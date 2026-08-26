package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.AuthResponseDto;
import com.tondise.ecommerce.dao.dto.UserDto;
import com.tondise.ecommerce.dao.mappers.UserMapper;
import com.tondise.ecommerce.dao.models.Cart;
import com.tondise.ecommerce.dao.models.User;
import com.tondise.ecommerce.dao.repository.CartRepository;
import com.tondise.ecommerce.dao.repository.UserRepository;
import com.tondise.ecommerce.dao.request.ForgotPasswordRequest;
import com.tondise.ecommerce.dao.request.LoginRequest;
import com.tondise.ecommerce.dao.request.RegisterRequest;
import com.tondise.ecommerce.dao.request.ResetPasswordRequest;
import com.tondise.ecommerce.dao.request.UpdatePasswordRequest;
import com.tondise.ecommerce.dao.request.UpdateProfileRequest;
import com.tondise.ecommerce.dao.request.UpdateProfileRequest;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.exception.BadRequestException;
import com.tondise.utils.exception.ResourceNotFoundException;
import com.tondise.utils.security.keycloak_manager.KeycloakService;
import com.tondise.utils.security.keycloak_manager.data.CreateUserRequestData;
import com.tondise.utils.security.keycloak_manager.data.RoleData;
import com.tondise.utils.security.keycloak_manager.data.TokenData;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentification déléguée à Keycloak (via {@link KeycloakService},
 * tondise-util) : ce service ne stocke ni ne vérifie aucun mot de passe
 * lui-même. La table {@code users} ne fait que réconcilier l'identité
 * Keycloak ({@code keycloakUserId}) avec les données métier (panier,
 * commandes, adresses...).
 *
 * <p>Étend {@link AbstractService} pour {@code AccountController} — mais
 * {@code create}/{@code update} du contrat générique n'ont pas de sens ici
 * (inscription et mise à jour de profil passent par Keycloak et ont besoin de
 * bien plus qu'un simple DTO, voir {@link #register} et
 * {@link #updateProfile}) : ils ne sont donc jamais appelés en pratique,
 * {@code AccountController} les surcharge systématiquement.</p>
 */
@Slf4j
@Service
public class AuthService extends AbstractService<User, UserDto, UpdateProfileRequest> {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final KeycloakService keycloakService;
    private final TokenSessionService tokenSessionService;
    private final PasswordResetService passwordResetService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, CartRepository cartRepository,
                        KeycloakService keycloakService, TokenSessionService tokenSessionService,
                        PasswordResetService passwordResetService, UserMapper userMapper,
                        CacheManager cacheManager) {
        super(userRepository, cacheManager, User.class.getName());
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.keycloakService = keycloakService;
        this.tokenSessionService = tokenSessionService;
        this.passwordResetService = passwordResetService;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponseDto register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Cet email est déjà utilisé");
        }

        CreateUserRequestData data = CreateUserRequestData.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getEmail())
                .password(request.getPassword())
                .isActive(true)
                .roles(List.of(new RoleData("USER")))
                .build();

        String keycloakUserId;
        try {
            keycloakUserId = keycloakService.createKeycloakUser(data);
        } catch (Exception e) {
            throw new BadRequestException("Échec de la création du compte : " + e.getMessage());
        }

        User user = User.builder()
                .keycloakUserId(keycloakUserId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .build();
        user = userRepository.save(user);
        cartRepository.save(Cart.builder().user(user).build());

        TokenData tokenData = keycloakService.getAccessToken(request.getEmail(), request.getPassword());
        tokenSessionService.saveToken(user.getId(), tokenData);

        return toAuthResponse(user, tokenData);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email ou mot de passe incorrect"));

        TokenData tokenData = tokenSessionService.getValidToken(user.getId())
                .orElseGet(() -> {
                    TokenData fresh = keycloakService.getAccessToken(request.getEmail(), request.getPassword());
                    tokenSessionService.saveToken(user.getId(), fresh);
                    return fresh;
                });

        return toAuthResponse(user, tokenData);
    }

    public UserDto me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return userMapper.toDto(user);
    }

    @Transactional
    public void logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        tokenSessionService.invalidate(userId);
        keycloakService.logOut(user.getKeycloakUserId());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = passwordResetService.createToken(user.getId());
            // TODO: brancher l'envoi d'email réel (SMTP/service transactionnel) avec ce token
            log.info("Jeton de réinitialisation généré pour {} : {}", user.getEmail(), token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }

        UUID userId = passwordResetService.consumeToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Jeton de réinitialisation invalide ou expiré"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        keycloakService.resetPassword(user.getKeycloakUserId(), request.getPassword());
    }

    @Transactional
    public UserDto updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        UserDto updated = userMapper.toDto(userRepository.save(user));
        clearCache();
        return updated;
    }

    @Transactional
    public void updatePassword(UUID userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // Valide l'ancien mot de passe via un login Keycloak réel (aucun hash local à comparer) :
        // une erreur d'identifiants lève UsernameOrPasswordIsBadException -> 401 (GlobalExceptionHandler).
        keycloakService.getAccessToken(user.getEmail(), request.getCurrentPassword());

        keycloakService.resetPassword(user.getKeycloakUserId(), request.getNewPassword());
        tokenSessionService.invalidate(userId);
    }

    private AuthResponseDto toAuthResponse(User user, TokenData tokenData) {
        return AuthResponseDto.builder()
                .user(userMapper.toDto(user))
                .token(tokenData.token())
                .refreshToken(tokenData.refreshToken())
                .build();
    }

    @Override
    protected UserDto convertToDTO(User model) {
        return userMapper.toDto(model);
    }

    @Override
    protected User convertToModel(UserDto dto) {
        return userMapper.toModel(dto);
    }

    /** Jamais appelée : l'inscription passe par {@link #register(RegisterRequest)} (Keycloak), pas ce contrat générique. */
    @Override
    public UserDto create(UpdateProfileRequest dto) {
        throw new UnsupportedOperationException("Utiliser AuthService.register(request) : un compte se crée via Keycloak.");
    }

    /**
     * Jamais appelée : {@code AccountController.update} est surchargé pour
     * ignorer l'id de la route et passer par {@link #updateProfile(UUID, UpdateProfileRequest)}
     * avec l'utilisateur courant — un {@code PUT /auth/{id}} générique permettrait
     * sinon à n'importe quel utilisateur de modifier le profil de n'importe qui.
     */
    @Override
    public UserDto update(UpdateProfileRequest dto, UUID id) {
        throw new UnsupportedOperationException("Utiliser AuthService.updateProfile(userId, request).");
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }
}
