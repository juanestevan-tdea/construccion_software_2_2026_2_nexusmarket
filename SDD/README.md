# 📐 Software Design Document (SDD)

Esta carpeta contiene la documentación de diseño del sistema NexusMarket.

## Contenido

- **Especificacion-Funcional.pdf** — Documento de especificación funcional del negocio con los objetivos OBJ-01 a OBJ-12.

## Documentación relacionada

- [DOCUMENTATION.md](../DOCUMENTATION.md) — Arquitectura técnica completa
- [SETUP.md](../SETUP.md) — Guía de instalación
- [README.md](../README.md) — Overview del proyecto

## Arquitectura

NexusMarket implementa **Arquitectura Hexagonal (Ports & Adapters)**:

- **domain/**: Núcleo puro (modelos, value objects, excepciones, puertos, servicios de dominio).
- **adapters/**: Implementaciones REST, persistencia JPA/MongoDB y casos de uso.
- **infrastructure/**: Configuración de seguridad (JWT), config general y notificaciones.

Toda comunicación con el exterior se realiza a través de **puertos** (`ports/in` y `ports/out`), implementados por adaptadores intercambiables.

## Referencias

- Repositorio de referencia del profesor: [construccion_de_software_2_2026_2](https://github.com/andfsanchezag/construccion_de_software_2_2026_2)