package ec.edu.uteq.appweb.biblioteca.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO-U4-2: autenticacion.
 *
 *   POST /api/v1/auth/login   recibe LoginRequest, valida con BCrypt contra
 *                             UsuarioRepository.findByUsernameAndActivoTrue,
 *                             y devuelve LoginResponse dentro de ApiResponse.
 *                             El token va en la cabecera Authorization de las
 *                             siguientes peticiones o en una cookie HttpOnly.
 *   POST /api/v1/auth/logout  invalida el token por su jti (opcional, suma en la rubrica).
 *
 * Credenciales sembradas por Flyway en V3__usuarios.sql:
 *   admin / Admin123!          rol ADMIN
 *   bibliotecario / Biblio123! rol BIBLIOTECARIO
 *   lector / Lector123!        rol LECTOR
 *
 * Un login fallido debe devolver 401, no 200 con success=false.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final ec.edu.uteq.appweb.biblioteca.repository.UsuarioRepository usuarioRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final ec.edu.uteq.appweb.biblioteca.security.JwtService jwtService;

    public AuthController(ec.edu.uteq.appweb.biblioteca.repository.UsuarioRepository usuarioRepository,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                          ec.edu.uteq.appweb.biblioteca.security.JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @org.springframework.web.bind.annotation.PostMapping("/login")
    public org.springframework.http.ResponseEntity<ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse<ec.edu.uteq.appweb.biblioteca.web.dto.LoginResponse>> login(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody ec.edu.uteq.appweb.biblioteca.web.dto.LoginRequest solicitud) {
        ec.edu.uteq.appweb.biblioteca.domain.Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(solicitud.username())
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(solicitud.password(), usuario.getPassword())) {
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        String token = jwtService.generar(usuario);
        ec.edu.uteq.appweb.biblioteca.web.dto.LoginResponse respuesta = new ec.edu.uteq.appweb.biblioteca.web.dto.LoginResponse(token);
        
        return org.springframework.http.ResponseEntity.ok(
                ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse.ok(respuesta, "Login exitoso")
        );
    }
}
