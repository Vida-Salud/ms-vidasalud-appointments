# VidaSalud - Microservicio de Atenciones

Microservicio encargado de administrar el ciclo de vida de las **atenciones médicas** de la plataforma VidaSalud.

Permite crear solicitudes de atención, consultar atenciones, modificar su información y controlar las transiciones entre sus distintos estados.

## Funcionalidades

El microservicio permite:

- Crear nuevas atenciones médicas.
- Consultar todas las atenciones registradas.
- Consultar una atención por ID.
- Modificar servicio, box y fecha de una atención.
- Administrar el estado de una atención.
- Validar transiciones de estado.
- Validar que las atenciones sean programadas para fechas futuras.
- Persistir la información en Oracle Database.

## Tecnologías

- Java 17+
- Spring Boot 4.0.8
- Spring Web MVC
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server
- Microsoft Entra ID / Azure AD
- Jakarta Validation
- Oracle Database
- Maven
- Docker

> La imagen Docker utiliza Eclipse Temurin JDK/JRE 21.

## Arquitectura

```text
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Oracle Database
```

Estructura principal:

```text
src/main/java/cl/duoc/ms_vidasalud_appointments/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AtencionController.java
│   └── ManejadorErrores.java
├── dto/
├── model/
│   ├── Atencion.java
│   └── EstadoAtencion.java
├── repository/
│   └── AtencionRepository.java
└── service/
    └── AtencionService.java
```

## Puerto

El servicio se ejecuta por defecto en:

```text
8081
```

Base URL:

```text
http://localhost:8081
```

## Estados de una atención

Las atenciones utilizan los siguientes estados:

```text
SOLICITADA
CONFIRMADA
EN_ESPERA
EN_ATENCION
CERRADA
CANCELADA
```

Las transiciones permitidas son:

```text
SOLICITADA
 ├── CONFIRMADA
 └── CANCELADA

CONFIRMADA
 ├── EN_ESPERA
 └── CANCELADA

EN_ESPERA
 ├── EN_ATENCION
 └── CANCELADA

EN_ATENCION
 └── CERRADA

CERRADA
 └── Estado final

CANCELADA
 └── Estado final
```

Las atenciones solamente pueden modificar sus datos mientras se encuentren en:

```text
SOLICITADA
CONFIRMADA
EN_ESPERA
```

## Seguridad

Todos los endpoints requieren autenticación mediante JWT de Microsoft Entra ID.

```http
Authorization: Bearer <access_token>
```

La autorización específica según roles de VidaSalud se aplica principalmente desde `ms-vidasalud-bff`.

## Variables de entorno

| Variable | Descripción | Valor local por defecto |
|---|---|---|
| `AZURE_TENANT_ID` | Tenant de Microsoft Entra ID | Configuración local |
| `AZURE_CLIENT_ID` | Client ID de la API | Configuración local |
| `AZURE_APP_ID_URI` | Identificador de la API | `api://<client-id>` |
| `DB_HOST` | Host Oracle | `localhost` |
| `DB_PORT` | Puerto Oracle | `1521` |
| `DB_SERVICE` | Oracle Service Name | `XEPDB1` |
| `DB_USERNAME` | Usuario Oracle | `vidasalud` |
| `DB_PASSWORD` | Contraseña Oracle | Configuración local |

## API

Base:

```text
/api/atenciones
```

### Crear atención

```http
POST /api/atenciones
```

Ejemplo:

```json
{
  "paciente": "Juan Pérez",
  "servicio": "Medicina General",
  "box": "Box 01",
  "fechaHora": "2026-09-25T10:30:00"
}
```

El box es opcional al momento de solicitar una atención.

La fecha y hora deben corresponder a una fecha futura.

### Listar atenciones

```http
GET /api/atenciones
```

### Obtener atención

```http
GET /api/atenciones/{id}
```

### Actualizar atención

```http
PUT /api/atenciones/{id}
```

Ejemplo:

```json
{
  "servicio": "Medicina General",
  "box": "Box 02",
  "fechaHora": "2026-09-25T11:30:00"
}
```

### Cambiar estado

```http
PUT /api/atenciones/{id}/estado
```

Ejemplo:

```json
{
  "nuevoEstado": "CONFIRMADA"
}
```

El servicio validará que la transición desde el estado actual sea válida.

## Ejecución local

### Windows

```bash
mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

También puede ejecutarse con Maven instalado:

```bash
mvn spring-boot:run
```

## Compilar

```bash
./mvnw clean package
```

El JAR resultante se genera en:

```text
target/
```

## Docker

Construir:

```bash
docker build -t vidasalud-appointments .
```

Ejecutar:

```bash
docker run -p 8081:8081 vidasalud-appointments
```

## Base de datos

Se utiliza Oracle Database mediante Spring Data JPA.

Hibernate se encuentra configurado actualmente con:

```yaml
ddl-auto: update
```

La entidad principal es:

```text
Atencion
```

La aplicación registra información como:

```text
Paciente
Servicio
Box
Fecha y hora
Estado
Fecha de creación
```

## Integración

El flujo normal dentro de VidaSalud es:

```text
Frontend
   │
   ▼
BFF
   │
   ▼
ms-vidasalud-appointments
   │
   ▼
Oracle Database
```

El BFF transforma:

```text
/api/appointments
```

en solicitudes internas hacia:

```text
/api/atenciones
```

manteniendo el token de autorización del usuario.

## Proyecto VidaSalud

Este microservicio forma parte de la arquitectura distribuida de VidaSalud y concentra la lógica de negocio relacionada con el proceso y ciclo de vida de las atenciones médicas.
