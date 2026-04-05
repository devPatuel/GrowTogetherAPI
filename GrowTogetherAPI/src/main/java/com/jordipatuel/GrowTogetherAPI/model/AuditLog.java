package com.jordipatuel.GrowTogetherAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String accion;

    @Column(nullable = false, length = 100)
    private String entidad;

    @Column
    private Long entidadId;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(length = 100)
    private String usuarioEmail;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(length = 50)
    private String ip;

    @Column(nullable = false)
    private Date fecha = new Date();
}
