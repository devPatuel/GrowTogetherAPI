package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import java.sql.Time;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * Entidad que representa un recordatorio asociado a un hábito.
 *
 * Almacena la hora a la que el usuario quiere ser recordado. La frecuencia
 * con la que el recordatorio se dispara la decide el cliente derivándola del
 * hábito asociado: si el hábito es DIARIO la noti suena cada día, si es
 * PERSONALIZADO suena solo los {@code diasSemana} del hábito. Por eso no
 * existe campo {@code frecuencia} aquí.
 *
 * La entrega real de la notificación al dispositivo se hace en cliente con
 * {@code flutter_local_notifications}. El backend solo persiste la
 * configuración del recordatorio.
 */
@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notificacion {

    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /** Mensaje del recordatorio que verá el usuario. */
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Column(nullable = false)
    private String mensaje;

    /** Hora a la que se enviará el recordatorio. Solo hora, sin fecha. */
    @NotNull(message = "La hora programada no puede ser nula")
    @Column(nullable = false)
    private Time horaProgramada;

    /** Indica si la notificación está activa o pausada. */
    @Column(nullable = false)
    private boolean activa;

    /** Hábito al que pertenece esta notificación. */
    @NotNull(message = "La notificación debe estar asociada a un hábito")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "habito_id", nullable = false)
    private Habito habito;
}
