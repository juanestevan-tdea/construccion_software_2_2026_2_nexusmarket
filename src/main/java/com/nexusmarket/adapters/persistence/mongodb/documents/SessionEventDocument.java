package com.nexusmarket.adapters.persistence.mongodb.documents;

import com.nexusmarket.domain.valueobjects.SessionEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Documento de evento de sesión almacenado en la colección {@code session_events} de MongoDB.
 * Se utiliza para trazabilidad de seguridad (login, logout, cambio de contraseña,
 * rechazos de token, fallos de login).
 *
 * <p>Índices:</p>
 * <ul>
 *   <li>{{@code userEmail: 1, occurredAt: -1}} — consulta por usuario y rango de fechas.</li>
 *   <li>{{@code eventType: 1, occurredAt: -1}} — consulta por tipo de evento.</li>
 *   <li>{{@code occurredAt: 1}} con TTL de 90 días — expiración automática de eventos antiguos.</li>
 * </ul>
 *
 * <p><b>Decisión de TTL:</b> se incluye un índice TTL de 90 días ({@code 7776000} segundos)
 * sobre {@code occurredAt} para evitar crecimiento ilimitado de la colección.
 * Si se desea conservar los eventos indefinidamente, se puede eliminar el índice TTL.</p>
 */
@Document(collection = "session_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEventDocument {

    @Id
    private String id;

    @Indexed
    private String userEmail;

    @Indexed
    private SessionEventType eventType;

    private String tokenId;          // jti del token (nullable)

    private String reason;           // motivo del rechazo/fallo (nullable)

    @Indexed
    private String ipAddress;

    private String userAgent;

    private Integer tokenVersion;

    @Indexed(expireAfterSeconds = 7776000) // TTL: 90 días
    private Instant occurredAt;
}
