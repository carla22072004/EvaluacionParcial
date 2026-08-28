package ec.edu.uteq.appweb.biblioteca.security;

import ec.edu.uteq.appweb.biblioteca.domain.Usuario;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * TODO-U4-2 (Objetivo especifico 2 de la Guia): AUTENTICACION JWT STATELESS
 * ============================================================================
 *
 * Implemente esta clase con la libreria jjwt 0.13.0, ya declarada en el pom.
 *
 * Lo que debe emitir el token:
 *   - sub  : el username
 *   - rol  : el rol del usuario (ADMIN, BIBLIOTECARIO o LECTOR)
 *   - jti  : identificador unico del token (UUID), necesario para revocarlo
 *   - iat  : fecha de emision
 *   - exp  : fecha de expiracion, tomada de app.jwt.expiracion-minutos
 *
 * Firma: HMAC-SHA256 o superior, con la clave de app.jwt.secreto.
 * La clave NO se escribe en el codigo ni en application.yml con valor real:
 * se inyecta por variable de entorno. Un secreto versionado en Git invalida
 * el esquema completo.
 *
 * Metodos sugeridos:
 *   String generar(Usuario usuario)
 *   String extraerUsername(String token)
 *   String extraerRol(String token)
 *   String extraerJti(String token)
 *   boolean esValido(String token)
 *   long expiracionEnSegundos()
 *
 * Pista de arranque con jjwt 0.13.x:
 *   SecretKey clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
 *   String jwt = Jwts.builder()
 *           .subject(usuario.getUsername())
 *           .claim("rol", usuario.getRol().name())
 *           .id(UUID.randomUUID().toString())
 *           .issuedAt(Date.from(ahora))
 *           .expiration(Date.from(ahora.plus(duracion)))
 *           .signWith(clave)
 *           .compact();
 */
@Service
public class JwtService {

    @org.springframework.beans.factory.annotation.Value("${app.jwt.secreto}")
    private String secretoBase64;

    @org.springframework.beans.factory.annotation.Value("${app.jwt.expiracion-minutos}")
    private long expiracionMinutos;

    public String generar(Usuario usuario) {
        javax.crypto.SecretKey clave = io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secretoBase64));
        java.util.Date ahora = new java.util.Date();
        java.util.Date expiracion = new java.util.Date(ahora.getTime() + expiracionMinutos * 60000);
        
        return io.jsonwebtoken.Jwts.builder()
                .setSubject(usuario.getUsername())
                .claim("rol", usuario.getRol().name())
                .setId(java.util.UUID.randomUUID().toString())
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(clave)
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public boolean esValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private io.jsonwebtoken.Claims extraerClaims(String token) {
        javax.crypto.SecretKey clave = io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secretoBase64));
        return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(clave)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
