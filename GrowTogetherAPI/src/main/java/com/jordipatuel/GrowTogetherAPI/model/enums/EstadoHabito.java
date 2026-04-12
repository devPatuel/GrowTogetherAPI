package com.jordipatuel.GrowTogetherAPI.model.enums;
/** Estado de un hábito en un día concreto. */
public enum EstadoHabito {
    /** El hábito fue completado ese día. */
    COMPLETADO,
    /** El hábito aún no ha sido completado ni descartado ese día. */
    PENDIENTE,
    /** El día pasó sin completar el hábito. */
    NO_COMPLETADO
}
