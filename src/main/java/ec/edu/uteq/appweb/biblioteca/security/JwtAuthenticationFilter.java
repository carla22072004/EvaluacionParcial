package ec.edu.uteq.appweb.biblioteca.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * ============================================================================
 * TODO-U4-2: FILTRO QUE AUTENTICA CADA PETICION A PARTIR DEL JWT
 * ============================================================================
 *
 * Debe, en este orden:
 *   1. Leer el token de la cabecera Authorization: Bearer &lt;token&gt;
 *      (opcionalmente tambien de una cookie HttpOnly llamada access_token).
 *   2. Si no hay token, dejar pasar la peticion sin autenticar: el filtro NO
 *      rechaza, de eso se encarga la cadena de seguridad.
 *   3. Si hay token y es valido, construir un UsernamePasswordAuthenticationToken
 *      con las autoridades derivadas del claim rol, prefijadas con "ROLE_",
 *      y colocarlo en el SecurityContextHolder.
 *   4. Si el token es invalido o expiro, limpiar el contexto y continuar.
 *
 * Cuidado con un error frecuente: si escribe la respuesta de error aqui dentro,
 * se rompe el contrato de ProblemDetail que ya implementa GlobalExceptionHandler.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion,
                                    HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        String authHeader = peticion.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (peticion.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : peticion.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null && jwtService.esValido(token)) {
            String username = jwtService.extraerUsername(token);
            String rol = jwtService.extraerRol(token);
            
            java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> autoridades = 
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol));
                
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(username, null, autoridades);
                
            auth.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(peticion));
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
        
        cadena.doFilter(peticion, respuesta);
    }
}
