Actividad - Consumo de APIs REST y Persistencia en Android

Objetivo

Desarrollar una aplicación móvil en Android (con Jetpack Compose) que consuma datos desde una API REST y los almacene localmente, permitiendo su consulta incluso sin conexión a internet.

Contexto del problema

Las aplicaciones modernas no solo consumen datos desde internet, sino que también deben ser capaces de almacenarlos localmente para mejorar el rendimiento y permitir el acceso offline.

En este taller se desarrollará una aplicación que:

Obtiene datos desde una API pública
Los muestra en pantalla
Permite filtrarlos
Permite ver el detalle de cada elemento
Guarda la información localmente

Requerimientos funcionales

Consumo de API

PokeAPI https://pokeapi.co/

Las peticiones deben realizarse usando Retrofit
Los datos deben mapearse a modelos en Kotlin

Interfaz de usuario (UI)

Se debe implementar lo siguiente:

Una pantalla que muestre una lista de elementos con la información básica.
Se debe permitir filtrar o buscar elementos usando al menos dos campos diferentes, para esto debe usar un endpoint de la API.
Se debe permitir navegar a una pantalla de detalle donde haya más información, por lo tanto debe usarse otro endpoint de la API para acceder a una información más completa.

Persistencia local

Se deben almacenar los datos en una base de datos local (SQLite usando Room). La aplicación debe poder:

Consultar datos almacenados
Mostrar datos si no hay conexión a internet

Lógica de funcionamiento esperada

La aplicación debe seguir este flujo:

Si hay conexión a internet:

Consumir la API.
Guardar los datos en la base de datos local, definiendo qué información persistir y estableciendo un mecanismo que evite la redundancia y garantice la consistencia de los datos.
Mostrar los datos en pantalla.

Si no hay conexión:

Cargar los datos desde la base de datos local
Mostrar los datos almacenados

Requerimientos obligatorios adicionales

La aplicación debe incluir obligatoriamente:

Indicador de estado de conexión (online/offline)
Paginación en la lista de resultados (con scroll infinito)

Arquitectura

Se recomienda utilizar una arquitectura en capas e implementar inyección de dependencias con Hilt. Las capas deben dividirse en:

UI
ViewModel
Repository
Domain
Fuentes de datos: API (Retrofit) y Base de datos (SQLite con Room).

Recomendaciones

Implementar primero el consumo de la API (mostrar datos en lista)
Posteriormente, integrar la persistencia local
Finalmente, unificar ambos (modo online/offline)
Manejar errores de red e informar al usuario.
