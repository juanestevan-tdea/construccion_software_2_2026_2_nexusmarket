# 🛒 NexusMarket

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-94%20passed-brightgreen?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

Backend de marketplace multiproveedor construido con **Arquitectura Hexagonal (Ports & Adapters)** sobre **Spring Boot 3.4** y **Java 17**.

---

## 📦 Descripción

**NexusMarket** es una plataforma backend para comercio electrónico que gestiona el ciclo completo de un marketplace: autenticación JWT, catálogo de productos, bodegas e inventario, órdenes, facturación, reembolsos, envíos, devoluciones y auditoría centralizada en MongoDB.

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **Ports & Adapters** con tres capas concéntricas:
[ Clientes REST / HTTP ]
│
▼
┌──────────────────────────────────────────────────────────────┐
│ ADAPTERS: REST (Controllers) │
└──────────────────────────────┬───────────────────────────────┘
│ Invoca
▼
┌──────────────────────────────────────────────────────────────┐
│ DOMAIN: Ports In (Use Cases) │
│ adapters/useCases/ │
└──────────────────────────────┬───────────────────────────────┘
│ Orquesta
▼
┌──────────────────────────────────────────────────────────────┐
│ DOMAIN CORE │
│ - Models (POJOs) - Value Objects (Enums) │
│ - Domain Services - Business Exceptions │
│ - Ports Out (Interfaces) │
└──────────────────────────────┬───────────────────────────────┘
│ Satisface contrato
▼
┌──────────────────────────────────────────────────────────────┐
│ ADAPTERS: PERSISTENCE (JPA & MongoDB) │
│ - MySQL: Entities, Repositories, Mappers │
│ - MongoDB: Documents, Repositories, Mappers │
└──────────────────────────────────────────────────────────────┘

text

---

## ⚙️ Stack Tecnológico

| Capa / Rol | Tecnología | Detalle |
|---|---|---|
| **Lenguaje** | Java 17 | OpenJDK / Eclipse Temurin LTS |
| **Framework** | Spring Boot 3.4 | Spring MVC, IoC e inyección de dependencias |
| **Seguridad** | Spring Security + JWT | Tokens HMAC-SHA256 stateless (JJWT 0.12.6) + BCrypt |
| **Persistencia Relacional** | MySQL 8.x + Spring Data JPA | Hibernate ORM, transacciones ACID |
| **Persistencia Documental** | MongoDB 6.x/7.x + Spring Data Mongo | Logs y trazabilidad de auditoría |
| **Build** | Maven 3.9+ | Gestión de dependencias |
| **Testing** | JUnit 5 + Mockito + Spring Test | 94 tests verdes |
| **Docker** | Dockerfile multi-stage + docker-compose | MySQL + MongoDB + App |

---

## 📂 Estructura del Repositorio
construccion_software_2_2026_2_nexusmarket/
├── domain/ # Núcleo de Dominio Puro
│ ├── exceptions/ # 17 Excepciones de negocio
│ ├── models/ # 14 POJOs (Order, User, Product...)
│ ├── ports/
│ │ ├── in/ # 15 Interfaces de Casos de Uso
│ │ └── out/ # 13 Interfaces de Persistencia
│ ├── services/ # 20 Servicios con reglas de negocio
│ └── valueobjects/ # 10 Enums (OrderStatus, UserRole...)
├── adapters/ # Adaptadores Externos
│ ├── persistence/
│ │ ├── jpa/ # Adaptador relacional
│ │ │ ├── adapters/ # 12 Implementaciones de Ports Out
│ │ │ ├── entities/ # 13 Entidades JPA
│ │ │ ├── mappers/ # 13 Mappers Entity ↔ Domain
│ │ │ └── repositories/ # 13 Spring Data JpaRepository
│ │ └── mongodb/ # Adaptador NoSQL (auditoría)
│ ├── rest/ # Adaptador API REST
│ │ ├── controllers/ # 15 Controladores @RestController
│ │ ├── dtos/ # DTOs requests + responses
│ │ ├── exception/ # GlobalExceptionHandler, ErrorResponse
│ │ └── mappers/ # Mappers DTO ↔ Domain
│ └── useCases/ # 15 Implementaciones de ports/in
├── infrastructure/ # Infraestructura Transversal
│ ├── config/ # CorsConfig, JacksonConfig
│ ├── notification/ # NotificationPort, NoOpNotificationAdapter
│ └── security/ # JwtService, filtros, SecurityConfig
├── SDD/ # Software Design Document
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── DOCUMENTATION.md
├── SETUP.md
└── LICENSE

text

---

## 🚀 Cómo Ejecutar

Consulta **[SETUP.md](SETUP.md)** para la guía paso a paso completa.

### Resumen rápido

1. **Crear base de datos en MySQL:**
   ```sql
   CREATE DATABASE nexusmarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
Compilar e instalar dependencias:

bash
mvn clean install -DskipTests
Ejecutar la aplicación:

bash
mvn spring-boot:run
La API queda disponible en http://localhost:8080.

🔐 Seguridad
Autenticación stateless mediante tokens Bearer JWT firmados con HMAC-SHA256.

Control de acceso basado en roles (RBAC) con 5 roles:

ADMIN: Control total del marketplace y auditoría.

BUYER: Compras, órdenes, historial y devoluciones.

SELLER: Publicación de productos y gestión de catálogos.

SUPERVISOR: Aprobación y seguimiento de reembolsos.

WAREHOUSE_OPERATOR: Gestión de bodegas, recepciones y despachos.

Almacenamiento seguro de credenciales con BCrypt.

🧪 Testing
Suite completa de pruebas unitarias y de integración con aislamiento mediante mocks.

Total: 94 tests verdes (0 fallos, 0 errores, 0 omitidos).

Herramientas: JUnit 5, Mockito, Spring Test.

Comando:

bash
mvn clean test
🐳 Docker
Incluye Dockerfile multi-stage y docker-compose.yml para levantar MySQL + MongoDB + App:

bash
docker-compose up -d
📚 Documentación
DOCUMENTATION.md — Documentación técnica completa.

SETUP.md — Guía de instalación, variables de entorno y troubleshooting.

SDD/ — Software Design Document y especificaciones funcionales.

✅ Estado del Proyecto
✅ Arquitectura hexagonal completa (8 módulos de negocio)

✅ 94 tests verdes

✅ Autenticación JWT con roles

✅ Persistencia híbrida (MySQL + MongoDB)

✅ Docker + docker-compose

📄 Licencia
Este proyecto está bajo la Licencia MIT. Consulta el archivo LICENSE para más detalles.

text

**Guarda con `Ctrl+S`.**

---

## 📄 Paso 2 — Crear `SDD/README.md`

### Paso 2a — Crear la carpeta y mover el PDF

En PowerShell:

```powershell
cd "C:\Users\juane\Desktop\proyectos\construccion_software_2_2026_2_nexusmarket"

# Crear carpeta SDD
New-Item -ItemType Directory -Force -Path "SDD"

# Ver el nombre exacto del PDF
Get-ChildItem "Recursos_de_Clase"