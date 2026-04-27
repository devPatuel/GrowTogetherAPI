package com.jordipatuel.GrowTogetherAPI.model;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoSolicitud;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
/**
 * Representa una solicitud de amistad entre dos usuarios.
 *
 * Flujo: el remitente envía la solicitud (estado PENDIENTE). El destinatario puede
 * aceptarla (pasa a ACEPTADA y se crea la relación bidireccional en usuario_amigos)
 * o rechazarla (queda como RECHAZADA). El remitente también puede cancelar su propia
 * solicitud mientras esté PENDIENTE (se elimina la fila).
 *
 * No se impone @UniqueConstraint a nivel de BD porque la unicidad aplica solo al estado
 * PENDIENTE: una vez respondida (ACEPTADA/RECHAZADA) puede volver a enviarse otra.
 * La regla se valida en {@link com.jordipatuel.GrowTogetherAPI.service.SolicitudAmistadService}.
 */
@Entity
@Table(name = "solicitudes_amistad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SolicitudAmistad {
    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Usuario que envía la solicitud. */
    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    /** Usuario que recibe la solicitud y puede aceptarla o rechazarla. */
    @ToString.Exclude
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    /** Estado actual de la solicitud. Por defecto PENDIENTE. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    /** Momento en que se envió la solicitud. */
    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    /** Momento en que el destinatario respondió. Null mientras la solicitud está PENDIENTE. */
    @Column
    private LocalDateTime fechaRespuesta;
}
