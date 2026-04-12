package com.jordipatuel.GrowTogetherAPI.model.enums;
/** Estado de la participación de un usuario en un desafío. */
public enum EstadoProgreso {
    /** El usuario sigue participando en el desafío. */
    ACTIVO,
    /** El usuario completó el objetivo del desafío. */
    SUPERADO,
    /** El usuario abandonó el desafío. */
    ABANDONADO
}
