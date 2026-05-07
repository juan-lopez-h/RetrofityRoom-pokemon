# Persistencia de Datos en Android con Room

## Introducción

En clases anteriores, estudiamos **DataStore**, una solución moderna para la persistencia de datos en Android. Pero ¿qué pasa si necesitamos almacenar datos más complejos, como objetos o listas de objetos? Aquí es donde entra en juego **Room**, una biblioteca de persistencia que proporciona una capa de abstracción sobre SQLite para permitir un acceso más robusto a la base de datos.

---

## SQLite

**SQLite** es una base de datos relacional ligera que viene integrada con Android. Permite almacenar datos estructurados en tablas y realizar consultas SQL para manipular esos datos. Sin embargo, trabajar directamente con SQLite puede ser tedioso y propenso a errores, especialmente cuando se trata de manejar esquemas de bases de datos y migraciones.

Internamente, SQLite utiliza un archivo de base de datos para almacenar los datos de la aplicación. Este archivo se encuentra en el sistema de archivos del dispositivo y es gestionado por el sistema operativo Android. Las aplicaciones pueden acceder a este archivo utilizando la API de SQLite proporcionada por Android.

> **Nota:** Una aplicación móvil no tiene la responsabilidad de administrar datos complejos, ya que esta responsabilidad recae en el backend. Sin embargo, existen casos en los que es necesario almacenar datos localmente, como cuando se desea ofrecer funcionalidad offline o mejorar el rendimiento al reducir las llamadas a la red.

Para más información sobre SQLite en Android, puede consultar la documentación oficial: [SQLite Documentation](https://developer.android.com/reference/android/database/sqlite/package-summary).

---

## Room

**Room** es una biblioteca de persistencia que simplifica el acceso a la base de datos SQLite en Android. Proporciona una capa de abstracción que permite definir entidades, DAOs (Data Access Objects) y bases de datos de manera más sencilla y segura. Gracias a Room, se simplifica la gestión de esquemas de tablas, consultas SQL y migraciones de bases de datos, ya que Room se encarga de generar el código necesario para interactuar con SQLite.

### Componentes principales de Room

La arquitectura de Room se basa en tres componentes principales:

1. **Entidades**: Son clases de datos que representan tablas en la base de datos. Cada entidad se anota con `@Entity` y define las columnas de la tabla mediante propiedades de la clase.
2. **DAOs (Data Access Objects)**: Son interfaces que definen los métodos para acceder a la base de datos. Los DAOs se anotan con `@Dao` y contienen métodos para insertar, actualizar, eliminar y consultar datos.
3. **Base de Datos**: Es una clase abstracta que extiende `RoomDatabase` y define la base de datos. Esta clase se anota con `@Database` y especifica las entidades y la versión de la base de datos.

Se recomienda utilizar Room para la persistencia de datos en Android debido a su facilidad de uso, seguridad y eficiencia. Room proporciona una forma más estructurada y segura de interactuar con SQLite, lo que reduce la probabilidad de errores y mejora la calidad del código.

La documentación oficial de Room se puede encontrar en el siguiente enlace: [Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room).

---

## Arquitectura de Room

```
UserViewModel
   │
   ▼
Llama al repositorio en una corrutina
   │
   ▼
UserRepository
   │  Expone insert() y getAll() suspend
   ▼
UserDao (@Dao)
   │  Opera sobre UserEntity
   ▼
AppDatabase (@Database)
   │  Expone los DAOs
   ▼
SQLite
   │  Archivo "app-database" en el dispositivo
   ▼
Hilt · DatabaseModule
   │  @Provides AppDatabase y DAO
   ▼
Inyección de dependencias
```

> **Recomendación:** La arquitectura de Room se basa en la separación de responsabilidades entre las entidades, los DAOs y la base de datos. Por encima de esta arquitectura, se recomienda integrar Room con un patrón de repositorio y utilizar **Hilt** para la inyección de dependencias en los ViewModels donde se necesite acceder a los datos.

---

## Ejemplo Práctico

### 1. Agregar dependencias

Primero, debemos agregar las dependencias de Room. En el archivo `libs.versions.toml`:

```toml
[versions]
room = "2.8.4"

[libraries]
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
```

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación:

```kotlin
dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
```

---

### 2. Definir la Entidad

Creamos una clase de datos que represente una tabla en la base de datos. Por ejemplo, una entidad `User` con campos `id`, `name` y `age`.

```kotlin
// Archivo: data/local/entity/UserEntity.kt
package com.example.demoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val age: Int
    // Otros campos que se deseen agregar...
)
```

---

### 3. Crear el DAO (Data Access Object)

Definimos una interfaz DAO que contenga las funciones para acceder a la base de datos. Estas funciones son `suspend` para permitir su uso con corrutinas.

```kotlin
// Archivo: data/local/dao/UserDao.kt
package com.example.demoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.demoapp.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity)
    
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>
}
```

---

### 4. Definir la Base de Datos

Creamos una clase abstracta que extienda `RoomDatabase` y defina la base de datos.

```kotlin
// Archivo: data/local/AppDatabase.kt
package com.example.demoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.demoapp.data.local.dao.UserDao
import com.example.demoapp.data.local.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

---

### 5. Usar Room en la Aplicación

Inicializamos la base de datos utilizando Room:

```kotlin
val db = Room.databaseBuilder(
    applicationContext,
    AppDatabase::class.java,
    "app-database"
).build()
```

---

### 6. Usar el DAO

Finalmente, podemos usar el DAO para insertar y recuperar datos:

```kotlin
val userDao = db.userDao()
val newUser = UserEntity(id = 1, name = "John Doe", age = 30)
userDao.insert(newUser)

val users = userDao.getAll()
```

> **Importante:** Estas operaciones deben realizarse en un hilo de fondo, por lo que es recomendable utilizar corrutinas o algún otro mecanismo de concurrencia.

---

## Integrar Room con una arquitectura más completa

Para una mejor arquitectura, es recomendable integrar Room con un patrón de repositorio y utilizar **Hilt** para la inyección de dependencias.

### 1. Crear el Repositorio

```kotlin
// Archivo: data/repository/UserRepository.kt
package com.example.demoapp.data.repository

import jakarta.inject.Singleton
import javax.inject.Inject

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao // Se inyecta el DAO aquí
) {
    // Función para insertar un usuario
    suspend fun insert(user: UserEntity) {
        userDao.insert(user)
    }
    
    // Función para obtener todos los usuarios
    suspend fun getAll(): List<UserEntity> {
        return userDao.getAll()
    }
}
```

> **Notas importantes:**
> - Si ya cuentas con una capa de modelos de dominio, es recomendable mapear entre las entidades de Room y los modelos de dominio dentro del repositorio.
> - Este repositorio puede ser la implementación de una interfaz que defina las operaciones disponibles, permitiendo cambiar la fuente de datos en el futuro sin afectar las capas superiores.
> - ⚠ Si el repositorio realiza operaciones suspendidas, las funciones que lo llamen también deben ser suspendidas o ejecutarse en un contexto adecuado.

---

### 2. Configurar Hilt para la Inyección de Dependencias

Creamos un módulo de Hilt para proporcionar la instancia de la base de datos y el DAO:

```kotlin
// Archivo: di/DatabaseModule.kt
package com.example.demoapp.di

import android.content.Context
import androidx.room.Room
import com.example.demoapp.data.local.AppDatabase
import com.example.demoapp.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app-database"
        ).build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
```

---

### 3. Usar el Repositorio en ViewModel

Finalmente, inyectamos el repositorio en nuestro ViewModel:

```kotlin
// Archivo: features/user/list/UserViewModel.kt
package com.example.demoapp.features.user.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.local.entity.UserEntity
import com.example.demoapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository // Se inyecta el repositorio
) : ViewModel() {
    
    fun insert(user: UserEntity) {
        viewModelScope.launch {
            userRepository.insert(user)
        }
    }
    
    fun getAll(): LiveData<List<UserEntity>> {
        val usersLiveData = MutableLiveData<List<UserEntity>>()
        viewModelScope.launch {
            val users = userRepository.getAll()
            usersLiveData.postValue(users)
        }
        return usersLiveData
    }
}
```

> **Recomendación de arquitectura limpia:** En una arquitectura más limpia, es recomendable utilizar modelos de dominio separados para evitar acoplar la capa de presentación con la capa de datos. El DAO debe estar encapsulado dentro del repositorio, y el ViewModel debe interactuar únicamente con el repositorio.

---

## Conclusión

**Room** es una herramienta poderosa para la persistencia de datos en Android, que facilita el manejo de bases de datos SQLite. Al utilizar Room, podemos:

- ✅ Definir entidades, DAOs y bases de datos de manera más sencilla y segura.
- ✅ Mejorar la calidad del código y reducir la probabilidad de errores.
- ✅ Simplificar la gestión de migraciones y esquemas de base de datos.

Aunque la persistencia local no siempre es necesaria en aplicaciones móviles, existen casos donde es fundamental para ofrecer una mejor experiencia al usuario, como:

- Funcionalidad offline.
- Mejora del rendimiento al reducir llamadas a la red.
- Almacenamiento temporal de datos para caché.

---

> **Recursos adicionales:**
> - [Documentación oficial de Room](https://developer.android.com/topic/libraries/architecture/room)
> - [Documentación oficial de SQLite en Android](https://developer.android.com/reference/android/database/sqlite/package-summary)

