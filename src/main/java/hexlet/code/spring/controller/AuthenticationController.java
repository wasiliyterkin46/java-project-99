package hexlet.code.spring.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hexlet.code.spring.dto.AuthRequest;
import hexlet.code.spring.util.JWTUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public final class AuthenticationController {

    @NonNull private final JWTUtils jwtUtils;
    @NonNull private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public String create(@RequestBody final AuthRequest authRequest) {
        var authentication = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword());

        authenticationManager.authenticate(authentication);

        var token = jwtUtils.generateToken(authRequest.getUsername());
        return token;
    }
}
