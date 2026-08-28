package ec.edu.uteq.appweb.biblioteca.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ============================================================================
 * TODO-U4-1 (Objetivo especifico 2 de la Guia): API REST DEL CATALOGO
 * ============================================================================
 *
 * Replique el patron de AutorController, que ya esta implementado y comentado.
 * LibroService y LibroMapper estan completos: usted solo expone, no reimplementa.
 *
 * Endpoints exigidos:
 *   GET    /api/v1/libros                 paginado, con meta; parametros opcionales
 *                                         titulo, categoriaId y anioDesde -> LibroService.buscar
 *   GET    /api/v1/libros/{id}            200 o 404 con ProblemDetail
 *   POST   /api/v1/libros                 201 + Location, rol ADMIN
 *   PUT    /api/v1/libros/{id}            200, rol ADMIN
 *   DELETE /api/v1/libros/{id}            204, rol ADMIN, borrado logico
 *   GET    /api/v1/libros/{id}/enriquecido combina el libro local con Open Library
 *                                         (depende del TODO-U4-4)
 *
 * Recuerde: exito en ApiResponse, error en ProblemDetail, nunca los dos mezclados.
 */
@RestController
@RequestMapping("/api/v1/libros")
public class LibroController {

    private final ec.edu.uteq.appweb.biblioteca.service.LibroService servicio;
    private final ec.edu.uteq.appweb.biblioteca.web.mapper.LibroMapper mapper;
    private final ec.edu.uteq.appweb.biblioteca.integration.OpenLibraryClient openLibraryClient;

    public LibroController(ec.edu.uteq.appweb.biblioteca.service.LibroService servicio,
                           ec.edu.uteq.appweb.biblioteca.web.mapper.LibroMapper mapper,
                           ec.edu.uteq.appweb.biblioteca.integration.OpenLibraryClient openLibraryClient) {
        this.servicio = servicio;
        this.mapper = mapper;
        this.openLibraryClient = openLibraryClient;
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse<java.util.List<ec.edu.uteq.appweb.biblioteca.web.dto.LibroResponse>> listar(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String titulo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long categoriaId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer anioDesde,
            @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable paginacion) {
        org.springframework.data.domain.Page<ec.edu.uteq.appweb.biblioteca.domain.Libro> pagina = servicio.buscar(titulo, categoriaId, anioDesde, paginacion);
        java.util.List<ec.edu.uteq.appweb.biblioteca.web.dto.LibroResponse> datos = pagina.getContent().stream().map(mapper::aRespuesta).toList();
        return ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse.ok(datos, "Libros listados", ec.edu.uteq.appweb.biblioteca.web.dto.PageMeta.de(pagina));
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse<ec.edu.uteq.appweb.biblioteca.web.dto.LibroResponse> buscar(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse.ok(mapper.aRespuesta(servicio.buscarPorId(id)), "Libro encontrado");
    }

    @org.springframework.web.bind.annotation.PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public org.springframework.http.ResponseEntity<ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse<ec.edu.uteq.appweb.biblioteca.web.dto.LibroResponse>> crear(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody ec.edu.uteq.appweb.biblioteca.web.dto.LibroRequest solicitud) {
        ec.edu.uteq.appweb.biblioteca.domain.Libro creado = servicio.crear(solicitud);
        ec.edu.uteq.appweb.biblioteca.web.dto.LibroResponse cuerpo = mapper.aRespuesta(creado);
        return org.springframework.http.ResponseEntity
                .created(java.net.URI.create("/api/v1/libros/" + creado.getId()))
                .body(ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse.ok(cuerpo, "Libro creado"));
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}/enriquecido")
    public ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse<ec.edu.uteq.appweb.biblioteca.web.dto.LibroEnriquecidoResponse> enriquecido(@org.springframework.web.bind.annotation.PathVariable Long id) {
        ec.edu.uteq.appweb.biblioteca.domain.Libro libro = servicio.buscarPorId(id);
        ec.edu.uteq.appweb.biblioteca.web.dto.OpenLibraryResponse openLibraryResponse = null;
        if (libro.getIsbn() != null) {
            openLibraryResponse = openLibraryClient.consultarPorIsbn(libro.getIsbn());
        }
        ec.edu.uteq.appweb.biblioteca.web.dto.LibroEnriquecidoResponse enriquecido = new ec.edu.uteq.appweb.biblioteca.web.dto.LibroEnriquecidoResponse(
                mapper.aRespuesta(libro),
                openLibraryResponse
        );
        return ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse.ok(enriquecido, "Libro enriquecido");
    }
}
