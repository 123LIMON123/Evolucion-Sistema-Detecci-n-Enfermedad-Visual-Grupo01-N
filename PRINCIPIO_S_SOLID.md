# Principio S (Responsabilidad Única) en OcuCheck AI

## ¿Qué dice el principio?

**Una clase (o función) debe tener una sola razón para cambiar.**

En la práctica: cada archivo hace **una sola cosa** y la hace bien. Si mañana cambia el modelo de IA, solo se toca el archivo del modelo. Si cambia una regla médica, solo se toca el archivo de esa regla. Si cambia el diseño de un botón, solo se toca ese componente. Nada se mezcla.

Esta tabla recorre **todos los archivos del proyecto**, agrupados por capa, con qué hace cada uno explicado de forma simple.

---

## 1. Capa de Machine Learning (`ml/`)

Todo lo relacionado a preparar la imagen y correr el modelo de IA.

| Archivo | Qué hace |
|---|---|
| `ImagePreprocessor` | Toma la foto del ojo y la convierte al formato exacto que el modelo necesita (tamaño 160x160, corrige la rotación). No sabe nada de enfermedades. |
| `EyeDiseaseClassifier` (interfaz) | Define "el contrato": cualquier motor de IA que se use debe saber recibir una imagen preparada y devolver probabilidades por enfermedad. |
| `TFLiteDiseaseClassifier` | Es quien realmente carga el modelo `.tflite` y le pregunta "¿qué ves en esta imagen?". Solo sabe de TensorFlow Lite, nada de reglas médicas. |

## 2. Capa de Dominio / Reglas Médicas (`domain/`)

Todo lo relacionado a interpretar los resultados del modelo con criterio clínico.

| Archivo | Qué hace |
|---|---|
| `DiagnosisResult` | Guarda el resultado de un análisis: qué enfermedad, con qué confianza y qué nivel de riesgo. Es solo un dato, no hace nada. |
| `RiskLevel` | Lista los 3 niveles de riesgo posibles: bajo, moderado, alta sospecha. |
| `DiagnosticThresholds` | Guarda los números de corte (ej. 75% = alta sospecha) para que cambiarlos sea editar un dato, no reescribir lógica. |
| `MedicalDiagnosticInterpreter` | Traduce el número crudo del modelo (ej. "73%") en un nivel de riesgo ("moderado"). No sabe de cámaras ni de IA, solo de umbrales clínicos. |
| `DiseaseCatalog` | Es el "diccionario" de las 8 enfermedades: nombre, descripción, síntomas y recomendación, en español e inglés. |

## 3. Capa de Datos / Estado de la App (`data/`)

Guarda información que la app necesita recordar mientras se usa.

| Archivo | Qué hace |
|---|---|
| `AnalysisHistoryRepository` | Guarda la lista de análisis que el usuario fue haciendo, para mostrarla en "Historial". |
| `UserSessionRepository` | Guarda si el usuario está logueado y sus datos de perfil (nombre, correo). |
| `LocalizationRepository` | Guarda qué idioma está activo ahora mismo (español o inglés). |

## 4. Cámara (`ui/camera/`)

Todo lo relacionado a la cámara en vivo y su feedback visual.

| Archivo | Qué hace |
|---|---|
| `CameraPreview` | Prende la cámara y muestra la imagen en vivo en pantalla. |
| `FrameQualityAnalyzer` | Revisa cada foto capturada y decide si hay suficiente luz y nitidez para analizarla. No sabe nada de enfermedades. |
| `ViewfinderOverlay` | Dibuja el aro circular de encuadre sobre la cámara (cian, verde o ámbar según la calidad). Solo dibuja, no decide nada. |

## 5. Componentes visuales reutilizables (`ui/components/`)

Piezas de interfaz chicas que se usan en varias pantallas.

| Archivo | Qué hace |
|---|---|
| `AppTopBar` | Dibuja la barra superior (título, botón volver, botón de idioma) igual en todas las pantallas. |
| `ClinicalStatusChip` | Dibuja la "pastilla" de estado (verde/ámbar/rojo) que muestra el nivel de riesgo. |
| `MedicalDisclaimerBanner` | Muestra el aviso de "esto no reemplaza a un médico" con su botón de "Entendido". |
| `ProbabilityMeterRow` | Dibuja una fila con el nombre de una enfermedad, su porcentaje y una barra de progreso. |

## 6. Coordinador de pantalla (`ui/viewmodel/`)

| Archivo | Qué hace |
|---|---|
| `CameraAnalysisViewModel` | Es el "director de orquesta" de la pantalla de cámara: decide cuándo analizar una foto y guarda el resultado para que la pantalla lo muestre. No procesa imágenes ni corre el modelo él mismo, se lo pide a las clases de `ml/`. |

## 7. Pantallas (`ui/screens/`)

Cada pantalla se encarga únicamente de lo que el usuario ve en ese momento.

| Archivo | Qué hace |
|---|---|
| `LoginScreen` | Pantalla de inicio de sesión. |
| `RegisterScreen` | Pantalla de registro. |
| `HomeScreen` | Menú principal con las 4 opciones (analizar, historial, enfermedades, perfil). |
| `CameraScreen` | Arma la pantalla de cámara conectando las piezas de abajo con el ViewModel. |
| `CameraViewfinderSection` | Muestra solo la cámara/foto subida con su aro de encuadre. |
| `CameraResultsPanel` | Muestra solo el resultado del análisis y los botones de acción. |
| `CameraPermissionDeniedContent` | Muestra el mensaje de "falta permiso de cámara". |
| `AnalysisResultScreen` | Muestra el resultado final de un análisis puntual. |
| `DiseaseInfoScreen` | Lista las 8 enfermedades con su información. |
| `HistoryScreen` | Lista los análisis guardados anteriormente. |
| `ProfileScreen` | Muestra los datos del usuario y el botón de cerrar sesión. |

## 8. Diseño / Tema visual (`ui/theme/`)

| Archivo | Qué hace |
|---|---|
| `Color.kt` | Solo declara los colores de la marca (paleta clínica). |
| `Shape.kt` | Solo declara qué tan redondeadas son las esquinas (botones, tarjetas, chips). |
| `Type.kt` | Solo declara las tipografías y sus tamaños. |
| `Theme.kt` | Junta los colores, formas y tipografías en un solo tema para toda la app. |
| `ClinicalTier` | Define los 4 niveles de color clínico (sano/atención/crítico/neutral) que usan los chips y medidores. |

## 9. Idiomas (`l10n/`)

| Archivo | Qué hace |
|---|---|
| `AppLanguage` | Lista los idiomas disponibles: español e inglés. |
| `AppStrings` | Es el diccionario con todos los textos de la app en ambos idiomas. |
| `LocalizationCompositionLocals` | Hace que el idioma activo llegue a todas las pantallas sin tener que pasarlo a mano una por una. |

## 10. Cableado de la app (`di/`, `navigation/`)

| Archivo | Qué hace |
|---|---|
| `AppContainer` | Es el único lugar donde se crean las clases concretas (el modelo, los repositorios, etc.) y se conectan entre sí. |
| `CameraAnalysisViewModelFactory` | Solo sabe cómo construir el `CameraAnalysisViewModel` con sus dependencias. |
| `AppNavHost` | Define el mapa de navegación: qué pantalla se muestra en cada ruta. |

## 11. Entrada de la aplicación

| Archivo | Qué hace |
|---|---|
| `EyeDiseaseApp` | Crea el `AppContainer` cuando arranca la app. |
| `MainActivity` | Arma la pantalla raíz de Android: tema, idioma y navegación. No tiene lógica propia. |

---

## En resumen

Cada archivo de este proyecto responde a **una sola pregunta**. Por ejemplo:

- ¿Cómo se ve un botón? → `Theme.kt` / `Shape.kt`
- ¿Cómo se prepara la foto para la IA? → `ImagePreprocessor`
- ¿Qué significa clínicamente un 80% de confianza? → `MedicalDiagnosticInterpreter`
- ¿Qué pantalla se abre al tocar "Historial"? → `AppNavHost`

Si el día de mañana cambia una sola de esas cosas (por ejemplo, se sube el umbral de "alta sospecha" de 75% a 80%), **solo hay que tocar un archivo** (`MedicalDiagnosticInterpreter` / `DiagnosticThresholds`), sin arriesgar romper la cámara, la interfaz o el idioma.
