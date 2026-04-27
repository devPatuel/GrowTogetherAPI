package com.jordipatuel.GrowTogetherAPI.model.enums;
/** Estados posibles de una solicitud de amistad entre dos usuarios. */
public enum EstadoSolicitud {
    /** Solicitud enviada y pendiente de respuesta del destinatario. */
    PENDIENTE,
    /** Solicitud aceptada por el destinatario. La amistad se crea al pasar a este estado. */
    ACEPTADA,
    /** Solicitud rechazada por el destinatario. */
    RECHAZADA
}
