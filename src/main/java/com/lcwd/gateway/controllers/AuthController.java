package com.lcwd.gateway.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lcwd.gateway.models.AuthResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RegisteredOAuth2AuthorizedClient("okta") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OidcUser user,
            Model model) {

        logger.info("User email ID is: {}", user.getEmail());

        // Creating auth response object
        AuthResponse authResponse = new AuthResponse();

        // Setting user email to authResponse
        authResponse.setUserId(user.getEmail());

        // Setting access token to authResponse
        authResponse.setAccessToken(client.getAccessToken().getTokenValue());

        // Setting refresh token to authResponse
        if (client.getRefreshToken() != null) {
            authResponse.setRefreshToken(client.getRefreshToken().getTokenValue());
        }

        // Setting expire time to authResponse
        authResponse.setExpireAt(client.getAccessToken().getExpiresAt().getEpochSecond());

        // Collecting authorities from the user and setting it to authResponse
        List<String> authorities = user.getAuthorities().stream()
                .map(grantedAuthority -> {
                	return grantedAuthority.getAuthority();
                })
                .collect(Collectors.toList());

        authResponse.setAuthorities(authorities);

        // Returning the authResponse wrapped in ResponseEntity
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }
}
