# 🚀 NexusMarket - Centralized Digital Marketplace

## 📖 Descripción del Proyecto
**NexusMarket** es una plataforma digital centralizada que actúa como intermediario comercial entre compradores y vendedores. El sistema administra integralmente la operación, desde el registro de usuarios y publicación de productos hasta la logística, facturación y posventa, garantizando trazabilidad y coordinación entre todos los participantes.

## 🎯 Objetivos Funcionales (OBJ-01 a OBJ-12)
- **OBJ-01**: Administrar la información de todos los usuarios del Marketplace.
- **OBJ-02**: Gestionar el registro y administración de vendedores.
- **OBJ-03**: Administrar compradores registrados.
- **OBJ-04**: Controlar la información de las bodegas.
- **OBJ-05**: Gestionar el catálogo de productos.
- **OBJ-06**: Administrar el inventario distribuido.
- **OBJ-07**: Gestionar el carrito de compras.
- **OBJ-08**: Controlar el ciclo completo de los pedidos.
- **OBJ-09**: Administrar la facturación de las compras.
- **OBJ-10**: Gestionar los procesos logísticos.
- **OBJ-11**: Administrar devoluciones y reembolsos.
- **OBJ-12**: Consolidar información administrativa para consulta.

## 🏗️ Tecnologías Utilizadas
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje de programación |
| Spring Boot | 3.4.0 | Framework principal |
| MySQL | 8.x | Base de datos relacional |
| MongoDB | 6.x | Base de datos documental |
| Maven | 3.9.16 | Gestión de dependencias y construcción |
| Git | Última | Control de versiones |

## 📂 Estructura del Proyecto (Modelo de Dominio)
El modelo de dominio se organiza en los siguientes módulos:
- **`users`**: Gestión de usuarios, compradores, vendedores y roles.
- **`catalog`**: Catálogo de productos, categorías y bodegas.
- **`inventory`**: Control de inventario y estados (disponible, dañado, reservado).
- **`orders`**: Ciclo de vida de pedidos (CART → FINISHED).
- **`logistics`**: Gestión de envíos y devoluciones.
- **`billing`**: Facturación y reembolsos.
- **`audit`**: Auditoría de acciones con MongoDB.

## 🧪 Cómo Ejecutar el Proyecto
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/juanestevan-tdea/construccion_software_2_2026_2_nexusmarket.git