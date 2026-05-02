package com.tictactore.security;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.model.User;
import com.tictactore.service.JwtService;
import com.tictactore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final ApplicationProperties properties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");

        User user = userService.findOrCreate(email, name, providerId);
        String jwt = jwtService.generateToken(user);

        // Security: JWT Leaked in URL - Use HttpOnly cookies instead of URL parameters.
        ResponseCookie responseCookie = ResponseCookie.from("TTT_TOKEN", jwt)
                .httpOnly(true)
                .secure(request.isSecure())
                .path("/")
                .maxAge(Duration.ofHours(24))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, properties.getOauth2().getRedirectUri());
    }
}
