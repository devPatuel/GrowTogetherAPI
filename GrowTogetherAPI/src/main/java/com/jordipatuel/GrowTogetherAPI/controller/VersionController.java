package com.jordipatuel.GrowTogetherAPI.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint trivial publicado bajo /api/v1/auth/version para no tener que
 * añadir nuevas rutas a la lista permitAll de SecurityConfig (todo lo que
 * cuelga de /api/v1/auth/** es publico).
 *
 * Sirve como prueba de vida del despliegue CI/CD: cada vez que se cambia el
 * mensaje y se pushea a main, el workflow GitHub Actions debe redesplegar
 * la API y este endpoint debe reflejar el cambio.
 */
@RestController
@RequestMapping("/api/v1/auth/version")
public class VersionController {

    @GetMapping
    public Map<String, String> version() {
        return Map.of(
            "service", "GrowTogetherAPI",
            "deploy", "1"
        );
    }
}
