# Passworld Android 🔐

**Passworld** es una aplicación de gestión de contraseñas segura y moderna para Android, diseñada para mantener tus credenciales protegidas con cifrado de extremo a extremo.

## 📱 Características Principales

### 🔐 Seguridad Avanzada
- **Cifrado AES de extremo a extremo** para todas las contraseñas almacenadas
- **Contraseña maestra** como única clave de acceso a tu bóveda
- **Evaluación de seguridad** automática de contraseñas (débiles, comprometidas, duplicadas)
- **Almacenamiento seguro** en Firebase con datos cifrados localmente

### 🛡️ Autenticación Flexible
- **Registro con email y contraseña**
- **Inicio de sesión con Google** integrado
- **Verificación de contraseña maestra** antes de acceder a las credenciales

### 🎯 Gestión Inteligente de Contraseñas
- **Generador de contraseñas seguras** con configuración personalizable
  - Longitud ajustable (8-32 caracteres)
  - Mayúsculas/minúsculas, números y caracteres especiales
  - Indicador de fortaleza en tiempo real
- **Búsqueda rápida** por descripción, usuario o URL
- **Filtros inteligentes** (todas las contraseñas, comprometidas)
- **Ordenamiento múltiple** (nombre A-Z/Z-A, fecha de modificación)

### 📊 Análisis de Seguridad
- **Dashboard de estadísticas** con contadores de contraseñas
- **Detección automática** de contraseñas:
  - Débiles o inseguras
  - Duplicadas
  - Comprometidas en brechas de seguridad
  - URLs potencialmente inseguras
- **Indicadores visuales** de alerta en la lista de contraseñas

### 🎨 Experiencia de Usuario
- **Interfaz moderna** con Material Design 3
- **Modo oscuro** disponible
- **Soporte multiidioma** (Español, Inglés, Alemán)
- **Responsive design** optimizado para tablets y móviles
- **Animaciones fluidas** y transiciones suaves

## 🏗️ Arquitectura Técnica

### 📚 Stack Tecnológico
- **Lenguaje**: Java
- **Plataforma**: Android (API 29+)
- **Base de datos**: Firebase Realtime Database
- **Autenticación**: Firebase Authentication
- **UI Framework**: Material Design Components
- **Cifrado**: AES-256 con derivación de claves PBKDF2

### 🏛️ Estructura del Proyecto
```
app/src/main/java/mobile/passworld/
├── activity/              # Actividades principales
│   ├── MainActivity.java           # Punto de entrada
│   ├── SignInActivity.java         # Inicio de sesión
│   ├── SignUpActivity.java         # Registro de usuario
│   ├── VaultUnlockActivity.java    # Desbloqueo con contraseña maestra
│   ├── PasswordListActivity.java   # Lista principal de contraseñas
│   ├── GeneratePasswordActivity.java # Generador de contraseñas
│   ├── adapter/                    # Adaptadores de RecyclerView
│   └── fragment/                   # Fragmentos de diálogo
├── data/                  # Modelos y repositorios
│   ├── PasswordDTO.java            # Modelo de contraseña
│   ├── PasswordRepository.java     # Repositorio de contraseñas
│   ├── UserRepository.java         # Repositorio de usuarios
│   └── session/                    # Gestión de sesión
└── utils/                 # Utilidades
    ├── EncryptionUtil.java         # Cifrado y seguridad
    ├── PasswordEvaluator.java      # Análisis de contraseñas
    ├── PasswordGenerator.java      # Generación de contraseñas
    └── SecurityFilterUtils.java    # Filtros de seguridad
```

### 🔄 Flujo de la Aplicación
1. **Splash/Main** → Verificación de autenticación
2. **SignIn/SignUp** → Registro o inicio de sesión
3. **VaultUnlock** → Ingreso de contraseña maestra
4. **PasswordList** → Dashboard principal con todas las funcionalidades
5. **GeneratePassword** → Creación de nuevas contraseñas seguras

## 🚀 Instalación y Configuración

### 📋 Requisitos Previos
- Android Studio Arctic Fox o superior
- SDK Android 29 (API level 29) o superior
- Cuenta de Firebase configurada
- Java 11+

### ⚙️ Configuración del Proyecto
1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/passworld-android.git
   cd passworld-android
   ```

2. **Configurar Firebase**
   - Crear proyecto en [Firebase Console](https://console.firebase.google.com)
   - Agregar aplicación Android con package `mobile.passworld`
   - Descargar `google-services.json` y colocarlo en `app/`
   - Habilitar Authentication (Email/Password y Google)
   - Configurar Realtime Database

3. **Google Sign-In**
   - Generar SHA-1 del certificado de debug
   - Agregar SHA-1 en configuración de Firebase
   - Actualizar `google-services.json`

4. **Compilar y ejecutar**
   ```bash
   ./gradlew assembleDebug
   ```

### 🔧 Variables de Configuración
- **Kotlin DSL**: `build.gradle.kts`
- **Versión mínima**: Android 10 (API 29)
- **Versión objetivo**: Android 14 (API 35)
- **Namespace**: `mobile.passworld`

## 🧪 Testing

### 🔬 Tipos de Pruebas
- **Unit Tests**: Pruebas de utilidades de cifrado y evaluación
- **Integration Tests**: Pruebas de repositorios y Firebase
- **UI Tests**: Pruebas de flujo de usuario con Espresso

### ▶️ Ejecutar Pruebas
```bash
# Pruebas unitarias
./gradlew test

# Pruebas de instrumentación
./gradlew connectedAndroidTest
```

## 🛡️ Seguridad y Privacidad

### 🔐 Medidas de Seguridad
- **Cifrado local**: Todas las contraseñas se cifran antes de almacenarse
- **Zero-knowledge**: El servidor nunca ve las contraseñas en texto plano
- **Contraseña maestra**: No se almacena, solo se deriva la clave de cifrado
- **Validación robusta**: Verificación de fortaleza y seguridad automática

### 🛡️ Privacidad
- **Datos mínimos**: Solo se almacena información esencial
- **Cumplimiento GDPR**: Diseño orientado a la privacidad
- **Control del usuario**: Gestión completa de datos personales

## 🌍 Internacionalización

La aplicación soporta múltiples idiomas:
- **Español** (es) - Idioma por defecto
- **Inglés** (en)
- **Alemán** (de)

Recursos localizados en `app/src/main/res/values-{locale}/`

## 🎨 Personalización

### 🌙 Temas
- **Tema claro**: Diseño principal con colores claros
- **Tema oscuro**: Optimizado para uso nocturno y ahorro de batería

### 📱 Diseño Responsive
- **Móviles**: Diseño optimizado para pantallas pequeñas
- **Tablets**: Layout expandido para pantallas grandes (sw600dp)
- **Orientación**: Soporte para modo horizontal

## 📝 Licencia

Este proyecto es parte de un Trabajo de Fin de Grado (TFG) y está disponible para fines educativos y de investigación.

## 👥 Contribución

### 🐛 Reporte de Bugs
Por favor reporta bugs creando un issue con:
- Descripción detallada del problema
- Pasos para reproducir
- Información del dispositivo y versión de Android
- Logs relevantes

### 💡 Sugerencias
Las sugerencias de mejora son bienvenidas a través de issues etiquetados como "enhancement".

## 📞 Contacto y Soporte

Para preguntas técnicas o soporte, puedes contactar al desarrollador a través de los issues del repositorio.

---

**Nota**: Esta aplicación forma parte de un proyecto académico (TFG) enfocado en la investigación y desarrollo de soluciones de seguridad para dispositivos móviles.