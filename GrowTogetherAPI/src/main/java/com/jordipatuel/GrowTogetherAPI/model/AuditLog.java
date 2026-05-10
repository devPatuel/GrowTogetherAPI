package com.jordipatuel.GrowTogetherAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * Entidad que registra las acciones sensibles realizadas por administradores.
 *
 * No tiene relaciones JPA: {@code usuarioId} y {@code usuarioEmail} se guardan
 * como valores directos para que el log sea inmutable e independiente del
 * ciclo de vida del usuario. Si el usuario se desactiva o cambia su email, el
 * log mantiene la información tal y como estaba en el momento del evento.
 *
 * Ver ADR-013 en {@code docs/DECISIONS.md} para el contexto completo.
 */
@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Acción realizada (ej: RESET_PASSWORD, DESACTIVAR_USUARIO). */
    @Column(nullable = false, length = 50)
    private String accion;

    /** Nombre de la entidad afectada (ej: Usuario, Desafio). */
    @Column(nullable = false, length = 100)
    private String entidad;

    /** ID de la entidad afectada. */
    @Column
    private Long entidadId;

    /** ID del admin que realizó la acción. Sin relación JPA. */
    @Column(nullable = false)
    private Long usuarioId;

    /** Email del admin que realizó la acción. Sin relación JPA. */
    @Column(length = 100)
    private String usuarioEmail;

    /** Información adicional sobre la acción realizada. */
    @Column(columnDefinition = "TEXT")
    private String detalle;

    /** IP desde la que se realizó la acción. */
    @Column(length = 50)
    private String ip;

    /** Fecha y hora en que ocurrió el evento. Se asigna automáticamente al crear. */
    @Column(nullable = false)
    private Date fecha = new Date();
}
