# Principio O (Abierto/Cerrado) en OcuCheck AI

## ¿Qué dice el principio?

**Las entidades de software (clases, módulos, funciones) deben estar abiertas a la extensión, pero cerradas a la modificación.**

En la práctica: cuando aparece un caso nuevo (una regla nueva, un modelo nuevo, un motor de inferencia nuevo), la idea es **agregar una clase**, no **editar una clase que ya funciona**. Así no se arriesga romper algo que ya estaba probado y andando.

El síntoma de que **NO** se cumple este principio es un `when` (o `if/else` largo) que hay que volver a abrir cada vez que aparece un caso nuevo. Esta expo muestra los 3 lugares del proyecto donde sí se aplicó, con su versión "antes" (cerrada, violaba OCP) y "después" (abierta, cumple OCP).

---

## 1. `FrameQualityAnalyzer` — calidad del encuadre de la cámara

**Qué hace:** decide si una foto de la cámara está lo bastante oscura, brillosa o borrosa antes de mandarla al modelo de IA.

### ❌ Antes (violaba OCP)

Un solo método con un `when` adentro. Agregar un chequeo nuevo (por ejemplo, detectar reflejo/glare) obligaba a **editar esta clase**.

```kotlin
class FrameQualityAnalyzer {
    fun analyze(bitmap: Bitmap): FrameQuality {
        ...
        return when {
            brightness < DARK_THRESHOLD -> FrameQuality.TOO_DARK
            brightness > BRIGHT_THRESHOLD -> FrameQuality.TOO_BRIGHT
            laplacianVariance(...) < SHARPNESS_THRESHOLD -> FrameQuality.BLURRY
            else -> FrameQuality.GOOD
        }
    }
}
```

### ✅ Después (cumple OCP)

Cada chequeo es su propia clase, todas implementan la misma interfaz `FrameQualityRule`. El analizador solo las recorre una por una.

| Función / Clase | Qué hace |
|---|---|
| `FrameQualityRule` (interfaz) | El "molde": cualquier chequeo nuevo debe poder responder "¿encontré un problema en esta imagen o no?". Es el punto de extensión. |
| `TooDarkRule` | Chequea si el brillo promedio es muy bajo. |
| `TooBrightRule` | Chequea si hay demasiado brillo o reflejo. |
| `BlurryRule` | Chequea si la imagen está borrosa (usa una fórmula llamada "varianza del laplaciano"). |
| `FrameQualityAnalyzer` | Recibe una **lista** de reglas y les pregunta una por una si encontraron algo malo. No sabe qué reglas existen, solo sabe recorrerlas. |

**¿Por qué cumple OCP?** Para sumar un chequeo nuevo (ej. "no se detecta un ojo"), se escribe una clase `NoEyeDetectedRule` que implemente `FrameQualityRule` y se agrega a la lista en `AppContainer`. **`FrameQualityAnalyzer` no se vuelve a tocar nunca.**

```kotlin
class FrameQualityAnalyzer(
    private val rules: List<FrameQualityRule> = listOf(TooDarkRule(), TooBrightRule(), BlurryRule())
) {
    fun analyze(bitmap: Bitmap): FrameQuality {
        val sample = sampleLuminance(bitmap)
        for (rule in rules) {
            val issue = rule.evaluate(sample)
            if (issue != null) return issue
        }
        return FrameQuality.GOOD
    }
}
```

---

## 2. `ImagePreprocessor` — normalización de píxeles

**Qué hace:** convierte la foto en números que el modelo de IA pueda entender.

### ❌ Antes (violaba OCP)

`PixelNormalization` era un `enum` con 3 opciones fijas, y `ImagePreprocessor` tenía un `when` para decidir qué fórmula usar. Un modelo nuevo con otro rango de entrada (por ejemplo, uno cuantizado que espera valores entre -128 y 127) obligaba a **editar el enum y el `when`**.

```kotlin
enum class PixelNormalization { RAW_0_255, ZERO_TO_ONE, MINUS_ONE_TO_ONE }

private fun normalize(pixel: Int): Triple<Float, Float, Float> {
    return when (spec.normalization) {
        PixelNormalization.RAW_0_255 -> ...
        PixelNormalization.ZERO_TO_ONE -> ...
        PixelNormalization.MINUS_ONE_TO_ONE -> ...
    }
}
```

### ✅ Después (cumple OCP)

| Función / Clase | Qué hace |
|---|---|
| `PixelNormalizationStrategy` (interfaz) | El punto de extensión: cualquier forma nueva de normalizar píxeles solo necesita saber convertir (r, g, b) en 3 números. |
| `Raw0To255Normalization` | Deja los píxeles tal cual (0 a 255). |
| `ZeroToOneNormalization` | Los escala a un rango de 0 a 1. |
| `MinusOneToOneNormalization` | Los centra entre -1 y 1. |
| `ImagePreprocessor` | Recibe **una** estrategia ya elegida y solo la ejecuta. No sabe cuántas estrategias existen. |

**¿Por qué cumple OCP?** Un modelo nuevo con un rango de entrada distinto = una clase nueva que implemente `PixelNormalizationStrategy`. **`ImagePreprocessor` no se vuelve a tocar nunca.**

```kotlin
fun interface PixelNormalizationStrategy {
    fun normalize(r: Int, g: Int, b: Int): Triple<Float, Float, Float>
}

object Raw0To255Normalization : PixelNormalizationStrategy {
    override fun normalize(r: Int, g: Int, b: Int) = Triple(r.toFloat(), g.toFloat(), b.toFloat())
}
```

---

## 3. `EyeDiseaseClassifier` — motor de inferencia (ejemplo que ya existía)

**Qué hace:** define el "contrato" que cualquier motor de IA debe cumplir: recibir una imagen preparada y devolver probabilidades por enfermedad.

Este ya estaba diseñado así desde que se aplicó el principio S, y también es un ejemplo perfecto de OCP: hoy la única implementación es `TFLiteDiseaseClassifier` (usa TensorFlow Lite), pero el resto de la app (el `CameraAnalysisViewModel`) **nunca habla con TFLite directamente**, solo con la interfaz.

**¿Por qué cumple OCP?** Si mañana se quiere probar ONNX Runtime, o un modelo en la nube, o un modelo falso para hacer pruebas automáticas: se escribe una clase nueva que implemente `EyeDiseaseClassifier` y se cambia **una sola línea** en `AppContainer` (donde se decide cuál usar). Ni el ViewModel, ni la pantalla de cámara, ni el resto del pipeline se enteran del cambio.

```kotlin
interface EyeDiseaseClassifier {
    fun classify(input: ByteBuffer): Map<String, Float>
    fun close()
}

// Hoy: class TFLiteDiseaseClassifier : EyeDiseaseClassifier { ... }
// Mañana, sin tocar nada de lo anterior:
// class OnnxDiseaseClassifier : EyeDiseaseClassifier { ... }
```

---

## Dónde NO se forzó el principio (y por qué)

`RiskLevel` (bajo / moderado / alta sospecha) y `ClinicalTier` (sano / atención / crítico) siguen siendo `enum` cerrados con `when` en varias pantallas (`AnalysisResultScreen`, `HistoryScreen`, `CameraScreen`).

No se tocaron a propósito: son exactamente 3 niveles clínicos fijos, no hay un caso real de "agregar un cuarto nivel" a la vista, y convertirlos en algo abierto habría significado reescribir varias pantallas que ya funcionan bien, solo para "cumplir la regla" sin un beneficio concreto. Aplicar OCP donde no hace falta es sobre-ingeniería, y eso también es un error de diseño.

---

## En resumen

| Lugar | ¿Qué se puede agregar sin tocar código existente? |
|---|---|
| `FrameQualityAnalyzer` | Un chequeo nuevo de calidad de imagen |
| `ImagePreprocessor` | Una forma nueva de normalizar píxeles para otro modelo |
| `EyeDiseaseClassifier` | Un motor de inferencia distinto a TFLite |
