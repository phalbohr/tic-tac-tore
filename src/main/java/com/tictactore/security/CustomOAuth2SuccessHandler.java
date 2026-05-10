package com.tictactore.security;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.service.JwtService;
import com.tictactore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String AUTH_COOKIE_NAME = "TTT_TOKEN";
    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_SUB = "sub";
    private static final String ERROR_MISSING_ATTRIBUTES = "Required attributes missing from OAuth2 provider";
    private static final String COOKIE_PATH = "/";
    private static final String COOKIE_SAME_SITE = "Lax";
    private static final int COOKIE_MAX_AGE_HOURS = 24;

    private final UserService userService;
    private final JwtService jwtService;
    private final ApplicationProperties properties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        var token = (OAuth2AuthenticationToken) authentication;
        var attributes = token.getPrincipal().getAttributes();

        var email = (String) attributes.get(ATTR_EMAIL);
        var name = (String) attributes.get(ATTR_NAME);
        var providerId = (String) attributes.get(ATTR_SUB);

        if (email == null || providerId == null) {
            throw new OAuth2AuthenticationException(ERROR_MISSING_ATTRIBUTES);
        }

        var user = userService.findOrCreate(email, name, providerId);
        var jwt = jwtService.generateToken(user);

        var responseCookie = ResponseCookie.from(AUTH_COOKIE_NAME, jwt)
                .httpOnly(true)
                .secure(true)
                .path(COOKIE_PATH)
                .maxAge(Duration.ofMillis(properties.getJwt().getExpiration()))
                .sameSite(COOKIE_SAME_SITE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, properties.getOauth2().getRedirectUri());
    }
}
