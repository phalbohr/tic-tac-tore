package com.tictactore.security;

import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final String redirectUri;

    public CustomOAuth2SuccessHandler(
            JwtService jwtService,
            UserRepository userRepository,
            @Value("${application.security.oauth2.redirect-uri}") String redirectUri
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getName(); // Usually the Google internal ID

        // Persist or update user in database
        userRepository.findByEmail(email).ifPresentOrElse(
            user -> {
                user.setName(name);
                user.setProviderId(providerId);
                userRepository.save(user);
            },
            () -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setProviderId(providerId);
                userRepository.save(newUser);
            }
        );
        
        String token = jwtService.generateToken(email);
        
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
