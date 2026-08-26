package com.tondise.ecommerce.controllers.publish;

import com.tondise.ecommerce.config.security.CurrentUserResolver;
import com.tondise.ecommerce.dao.dto.AuthResponseDto;
import com.tondise.ecommerce.dao.dto.UserDto;
import com.tondise.ecommerce.dao.request.ForgotPasswordRequest;
import com.tondise.ecommerce.dao.request.LoginRequest;
import com.tondise.ecommerce.dao.request.RegisterRequest;
import com.tondise.ecommerce.dao.request.ResetPasswordRequest;
import com.tondise.ecommerce.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion, déconnexion et réinitialisation de mot de passe. Routes publiques (sauf /me et /logout, qui nécessitent un token).")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserResolver currentUserResolver;

    @Operation(summary = "Se connecter",
            description = "Authentifie l'utilisateur par email/mot de passe (via Keycloak) et renvoie ses infos de profil avec un token d'accès et un refresh token.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Créer un compte",
            description = "Crée un compte Keycloak et la ligne utilisateur locale associée, puis connecte immédiatement l'utilisateur (renvoie ses tokens).")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Se déconnecter", description = "Invalide la session courante de l'utilisateur connecté côté Keycloak.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(currentUserResolver.resolveUserId(jwt));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer mon profil", description = "Renvoie le profil de l'utilisateur actuellement connecté.")
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.me(currentUserResolver.resolveUserId(jwt)));
    }

    @Operation(summary = "Demander une réinitialisation de mot de passe",
            description = "Si l'email existe, génère un jeton de réinitialisation et déclenche son envoi par email. Ne révèle jamais si l'email existe ou non (toujours 204).")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Réinitialiser le mot de passe",
            description = "Consomme le jeton reçu par email et définit le nouveau mot de passe (via Keycloak).")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
