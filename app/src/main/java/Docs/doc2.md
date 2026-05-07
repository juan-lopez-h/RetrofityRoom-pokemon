# Retrofit y Comunicación con APIs REST



## Introducción

En el desarrollo de aplicaciones móviles, es común que necesitemos comunicarnos con servicios web para obtener o enviar datos. Una forma popular de hacerlo en Android es mediante el uso de **Retrofit**, una biblioteca que facilita la interacción con APIs RESTful.

---

## ¿Qué es Retrofit?

**Retrofit** es una biblioteca de cliente HTTP para Android y Java desarrollada por Square. Proporciona una forma sencilla y eficiente de consumir APIs RESTful al convertir las llamadas HTTP en interfaces de Java/Kotlin. Retrofit maneja automáticamente la serialización y deserialización de datos, lo que simplifica el proceso de comunicación con servicios web.

La documentación oficial de Retrofit se puede consultar en el siguiente enlace: [Retrofit Documentation](https://square.github.io/retrofit/).

---

## Comparación con otras bibliotecas

Aunque existen diversas bibliotecas para realizar solicitudes HTTP en Android, como **Volley** y **OkHttp**, Retrofit se distingue por su facilidad de uso y por su estrecha integración con bibliotecas de serialización como Gson y Moshi. Una de sus principales ventajas es la posibilidad de definir las solicitudes HTTP mediante anotaciones, lo que permite escribir un código más declarativo, legible y fácil de mantener. Estas características hacen de Retrofit una opción especialmente adecuada para el consumo de APIs REST en aplicaciones Android modernas.

En la siguiente tabla se comparan algunas características clave de Retrofit con otras bibliotecas populares:

| Característica | Retrofit | Volley | OkHttp |
|:---|:---|:---|:---|
| **Nivel de abstracción** | Alto | Medio | Bajo |
| **Propósito principal** | Consumo de APIs REST | Gestión de solicitudes HTTP y cache | Cliente HTTP |
| **Uso de anotaciones** | Sí | No | No |
| **Soporte para APIs REST** | Sí (orientado a REST) | Sí | Sí (manual) |
| **Serialización de datos** | Mediante conversores (Gson, Moshi, etc.) | Manual | Manual |
| **Integración directa con Gson** | Sí (con converter) | No | No |
| **Manejo de concurrencia** | Automático (con coroutines, RxJava, etc.) | Automático | Manual |
| **Manejo de cache** | No (requiere configuración adicional) | Sí (integrado) | Sí (configurable) |
| **Facilidad de uso** | Alta | Media | Baja |
| **Uso típico** | Apps modernas basadas en APIs REST | Apps con muchas peticiones pequeñas | Base para otras bibliotecas |

> **Nota:** Podemos afirmar que Retrofit abstrae a OkHttp para facilitar y estandarizar el consumo de servicios REST.

---

## Arquitectura de Retrofit

La arquitectura de Retrofit se estructura en capas que separan la lógica de la aplicación del cliente HTTP subyacente:

```
Aplicación Android (ViewModel + Corrutinas)
        │
        ▼
Llama a `suspend fun getUsers()`
        │
        ▼
Interface ApiService (@GET, @POST, etc.)
        │
        ▼
Cliente Retrofit (baseUrl + Converter Factory)
        │
        ▼
Gson Converter (Serializa/Deserializa JSON)
        │
        ▼
OkHttp (Cliente HTTP subyacente)
        │
        ▼
API REST (https://api.example.com)
        │
        ▼
Petición GET /users → Respuesta JSON [{ id, name, age }]
```

Retrofit se basa en la creación de interfaces que definen los endpoints de la API, utilizando anotaciones para especificar el tipo de solicitud HTTP y los parámetros. Posteriormente, genera automáticamente las implementaciones de estas interfaces, permitiendo realizar solicitudes de manera sencilla. Además, se integra con bibliotecas de serialización para convertir automáticamente los datos entre formatos JSON y objetos Java/Kotlin.

---

## Ejemplo Práctico

### 1. Agregar Dependencias

En el archivo `libs.versions.toml`:

```toml
[versions]
retrofit = "3.0.0"

[libraries]
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
converter-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "retrofit" }
```

En el archivo `build.gradle.kts` del módulo de la aplicación:

```kotlin
dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
}
```

> **⚠️ Importante:** Para que la aplicación pueda realizar solicitudes a través de la red, es necesario declarar el permiso de internet en el `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 2. Definir la Interfaz de la API

Creamos una interfaz que defina los endpoints utilizando anotaciones:

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
}
```
El uso de `suspend` indica que esta función es compatible con corrutinas, permitiendo operaciones asíncronas limpias. Retrofit también proporciona anotaciones como `@POST`, `@PUT`, `@DELETE`, entre otras.

### 3. Crear el Cliente Retrofit

Configuramos Retrofit para crear una instancia del cliente API:

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

### 4. Realizar Solicitudes a la API

Utilizamos la instancia de `apiService` para consumir la API:

```kotlin
// Se lanza una corrutina para realizar la solicitud de manera asíncrona
GlobalScope.launch {
    val users = apiService.getUsers()
    users.forEach { user ->
        Log.d("User", "Name: ${user.name}, Age: ${user.age}")
    }
}
```
> **Nota:** En un entorno de producción, es recomendable manejar errores robustamente y utilizar `viewModelScope` en lugar de `GlobalScope`, como se mostrará más adelante.

### 5. Definir el Modelo de Datos

Creamos una clase de datos que represente la estructura JSON recibida:

```kotlin
data class User(
    val id: Int,
    val name: String,
    val age: Int
)
```

---

## Integrar Retrofit con una arquitectura más completa

En una aplicación real, es recomendable integrar Retrofit con **Hilt** y un **patrón de repositorio**. De esta forma, los ViewModels no dependen directamente de Retrofit, lo que mejora la separación de responsabilidades y facilita las pruebas unitarias.

### 1. Agregar dependencias de Hilt

Si aún no lo ha hecho, agregue las dependencias de Hilt y KSP. Recuerde anotar la clase `Application` con `@HiltAndroidApp` y la actividad principal con `@AndroidEntryPoint`.

### 2. Configurar Hilt para proporcionar Retrofit

Creamos un módulo `NetworkModule.kt` en el paquete `di`:

```kotlin
package com.example.demoapp.di

import com.example.demoapp.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```
> **Nota:** Se utiliza `@Provides` porque Retrofit se construye mediante un Builder. Si requiere configurar `OkHttpClient` (interceptores, logging, timeouts), puede añadir un método `provideOkHttpClient` y pasarlo al builder mediante `.client()`.

### 3. Crear el Repositorio remoto

Definimos la interfaz en la capa de dominio (`domain/repository/UserRepository.kt`):

```kotlin
package com.example.demoapp.domain.repository
import com.example.demoapp.domain.model.User

interface UserRepository {
    suspend fun getUsers(): List<User>
}
```

Y su implementación en la capa de datos (`data/repository/UserRepositoryImpl.kt`):

```kotlin
package com.example.demoapp.data.repository
import com.example.demoapp.data.remote.ApiService
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun getUsers(): List<User> {
        return apiService.getUsers()
    }
}
```
Registre la vinculación en su `RepositoryModule`:
```kotlin
@Binds
@Singleton
abstract fun bindUserRepository(
    userRepositoryImpl: UserRepositoryImpl
): UserRepository
```

### 4. Usar el Repositorio en el ViewModel

Inyectamos el repositorio en el ViewModel y gestionamos la corrutina con `viewModelScope`:

```kotlin
package com.example.demoapp.features.user.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    init {
        loadUsers()
    }
    
    private fun loadUsers() {
        viewModelScope.launch {
            _users.value = repository.getUsers()
        }
    }
}
```
> **⚠️ Importante:** En un caso real, debe manejar los errores de red (mediante `try/catch` o exponiendo un `Result`/`Resource` en el ViewModel), ya que las peticiones HTTP pueden fallar por problemas de conectividad, errores del servidor o respuestas inválidas.

---

## Conclusión

**Retrofit** es una herramienta poderosa y fácil de usar para consumir APIs RESTful en aplicaciones Android. Su integración con bibliotecas de serialización como Gson facilita el manejo de datos, y su enfoque basado en anotaciones hace que el código sea limpio y mantenible. Al utilizar Retrofit, los desarrolladores pueden centrarse en la lógica de la aplicación sin preocuparse por los detalles complejos de las solicitudes HTTP. Además, al combinar Retrofit con Hilt y el patrón de repositorio, se obtiene una arquitectura escalable, mantenible y fácil de probar.