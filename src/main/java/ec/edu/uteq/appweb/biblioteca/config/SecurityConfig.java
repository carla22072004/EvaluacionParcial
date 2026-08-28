package ec.edu.uteq.appweb.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================================
 * TODO-U4-2: CADENA DE SEGURIDAD
 * ============================================================================
 *
 * Tal como esta, la aplicacion arranca con TODO abierto para que usted pueda
 * probar los controladores antes de tener el JWT listo. Eso es deliberado y
 * temporal: no se entrega asi.
 *
 * Debe dejarla en este estado final:
 *   - csrf deshabilitado (la API es stateless y no usa formularios de sesion).
 *   - SessionCreationPolicy.STATELESS.
 *   - Publicos: POST /api/v1/auth/login, /swagger-ui/**, /v3/api-docs/**,
 *     /api/docs, /actuator/health.
 *   - El resto de /api/v1/** exige autenticacion.
 *   - Registrar JwtAuthenticationFilter antes de UsernamePasswordAuthenticationFilter.
 *   - Devolver 401 cuando no hay autenticacion y 403 cuando el rol no alcanza,
 *     ambos en formato ProblemDetail.
 *
 * La autorizacion fina por rol se declara con @PreAuthorize en los controladores,
 * habilitada por @EnableMethodSecurity, que ya esta puesto.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers("/api/v1/auth/login", "/swagger-ui/**", "/v3/api-docs/**", "/api/docs", "/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/problem+json");
                            response.getWriter().write(
                                    "{\"type\":\"https://uteq.edu.ec/errores/no-autenticado\",\"title\":\"No autenticado\",\"status\":401,\"detail\":\"Token invalido o ausente\",\"timestamp\":\"" + java.time.OffsetDateTime.now() + "\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessException) -> {
                            response.setStatus(403);
                            response.setContentType("application/problem+json");
                            response.getWriter().write(
                                    "{\"type\":\"https://uteq.edu.ec/errores/acceso-denegado\",\"title\":\"Acceso denegado\",\"status\":403,\"detail\":\"No tiene permisos suficientes para ejecutar esta operacion\",\"timestamp\":\"" + java.time.OffsetDateTime.now() + "\"}"
                            );
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
