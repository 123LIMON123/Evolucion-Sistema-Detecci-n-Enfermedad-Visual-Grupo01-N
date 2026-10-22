# Datasets externos evaluados para reforzar el modelo

El dataset principal de entrenamiento es **ODIR-5K**
(`ml/data/full_df.csv` + `ml/data/preprocessed_images/`, fotos de fondo de
ojo, 8 clases: N, D, G, C, A, H, M, O). Se evaluaron tres fuentes externas
para intentar mejorar clases especificas. Resultado resumido:

| Fuente | Clase objetivo | Resultado | Se usa en el modelo final |
|---|---|---|---|
| JSIEC-1000 + RFMiD 2.0 | M (miopia) | **No ayudo** (M ya rendia cerca del techo) | No (`INCLUDE_EXTERNAL_MYOPIA = False`) |
| APTOS 2019 (Kaggle) | D (diabetes) | **Mejoro mucho**, y de rebote tambien N y O | Si (`INCLUDE_EXTERNAL_DIABETES = True`) |
| Conjunctivitis datasets (Kaggle) | CONJ (nueva, 9na clase) | **Mejoro TODO el modelo**, no solo CONJ | Si (`INCLUDE_EXTERNAL_CONJUNCTIVITIS = True`) |

El modelo final tiene **9 clases** (las 8 de ODIR-5K + CONJ) y un AUC
macro de validacion de **~0.88** (0.8817 y 0.8804 en dos corridas), contra
0.7888 del modelo original de 8 clases solo con ODIR-5K.

---

## 1. Miopia (M) — JSIEC-1000 + RFMiD 2.0: no ayudo

M es minoritaria en ODIR-5K (306 de 6392 filas, ~4.8%). Se sumaron 97
imagenes de miopia patologica confirmada desde JSIEC-1000 (54, carpeta
`9.Pathological myopia`) y RFMiD 2.0 (43, columna `MYA = 1`), ambos via
Zenodo (acceso abierto, DOIs
[10.5281/zenodo.3477553](https://doi.org/10.5281/zenodo.3477553) y
[10.5281/zenodo.7505822](https://doi.org/10.5281/zenodo.7505822)).

| Configuracion | AUC macro (val.) | AUC de M (val.) |
|---|---|---|
| Solo ODIR-5K (306 ejemplos de M) | **0.7888** | **0.9765** |
| + 97 imagenes externas (403 ejemplos de M), corrida 1 | 0.7863 | 0.9694 |
| + 97 imagenes externas (403 ejemplos de M), corrida 2 | 0.7886 | 0.9729 |

En las dos corridas con el agregado, el AUC de M quedo levemente **por
debajo** del modelo entrenado solo con ODIR-5K. M ya rendia cerca del
techo (AUC ~0.97-0.98) usando solo ODIR-5K: no estaba limitada por falta
de datos. Las imagenes externas, de otras camaras/poblaciones, metieron
corrimiento de dominio sin aportar señal nueva.

`ml/train.py` deja `INCLUDE_EXTERNAL_MYOPIA = False` por defecto (mismo
patron que `FINE_TUNE` para el backbone): el codigo de integracion se
mantiene, documentado y probado, pero no se usa en el modelo final.

## 2. Diabetes (D) — APTOS 2019: mejoro mucho, y de rebote otras clases

D **no** es minoritaria en ODIR-5K (2123 de 6392 filas, la segunda clase
mas grande), pero su AUC medido era bajo (~0.70) pese a eso — a diferencia
de M, esto sugeria un problema de calidad/ruido de etiqueta mas que de
cantidad (las etiquetas de ODIR-5K vienen de texto diagnostico con
keyword-matching, no de revision experta caso por caso).

Se sumaron **1857 imagenes con retinopatia diabetica confirmada**
(`diagnosis >= 1` en la escala de 5 grados) del dataset
[APTOS 2019 Blindness Detection](https://www.kaggle.com/competitions/aptos2019-blindness-detection)
(Kaggle, imagenes graduadas por el Aravind Eye Hospital, India).

| Configuracion | AUC macro (val., 8 clases) | AUC de D |
|---|---|---|
| Solo ODIR-5K | 0.7888 | 0.70 |
| + APTOS 2019, corrida 1 | 0.8172 | **0.8446** |
| + APTOS 2019, corrida 2 | 0.8182 | — |

El AUC de D subio ~0.14, pero ademas **N (0.69→0.78) y O (0.63→0.71)
mejoraron bastante** sin tocarlas directamente. Hipotesis: las etiquetas
N/D/O de ODIR-5K se confunden entre si (un ojo con signos leves de
diabetes mal anotado puede terminar en N o en O); sumar ejemplos de D
graduados por especialistas ayudo al modelo a trazar mejor esos limites,
no solo a reconocer D. El costo: A (0.785→0.716) y M (0.9765→0.936)
bajaron un poco — efecto esperable de recalcular los pesos de clase sobre
una distribucion distinta.

`INCLUDE_EXTERNAL_DIABETES = True` en `ml/train.py`.

## 3. Conjuntivitis (CONJ, clase nueva) — datasets de Kaggle: mejoro todo el modelo

A diferencia de M y D, conjuntivitis no es una condicion visible en una
foto de **fondo de ojo** (retina) como las 8 clases de ODIR-5K: se ve en
una foto del ojo **por fuera** (esclerotica, parpados) — de hecho, el tipo
de foto que la camara del celular realmente captura. Por eso se agrega
como una **9na clase nueva**, no como refuerzo de una existente.

Se combinaron dos datasets de Kaggle:

| Dataset | Normal | Conjuntivitis | Licencia |
|---|---|---|---|
| [`alisofiya/conjunctivitis`](https://www.kaggle.com/datasets/alisofiya/conjunctivitis) | 181 | 177 | No especificada |
| [`revengerdna/conjunctivitis-dataset`](https://www.kaggle.com/datasets/revengerdna/conjunctivitis-dataset) | 986 | 371 | MIT |
| **Combinado** | **1167** | **548** | |

| Configuracion | AUC macro (9 clases) | AUC de CONJ | AUC promedio de las 8 originales |
|---|---|---|---|
| Solo ODIR-5K + APTOS (8 clases) | 0.8182 | — | 0.8182 |
| + Conjuntivitis (9 clases), corrida 1 | 0.8817 | 0.9979 | **0.8672** |
| + Conjuntivitis (9 clases), corrida 2 | 0.8805 | 0.9982 | **0.8657** |

CONJ es casi trivial de clasificar (AUC ~0.998: una foto de ojo externo no
se parece en nada a una de fondo de ojo), lo que explica buena parte del
salto en el promedio de 9 clases. Pero lo notable es que **las 8 clases
originales tambien mejoraron en promedio** (0.818 → ~0.866), reproducido
en dos corridas independientes. Hipotesis: agregar ~1700 imagenes de un
dominio visual muy distinto actua como regularizacion para la cabeza de
clasificacion (mas diversidad total de ejemplos, mejor calibracion de las
fronteras de decision), aun cuando esas imagenes no le enseñan nada sobre
retinopatia diabetica o glaucoma directamente.

`INCLUDE_EXTERNAL_CONJUNCTIVITIS = True` en `ml/train.py`. El modelo final
(9 clases) es el que se shipea en
`app/src/main/assets/ocular_disease_model.tflite`.

## Astigmatismo: no se encontro dataset usable

No existe ningun dataset publico y descargable de astigmatismo en formato
de foto de ojo compatible con este pipeline, por una razon clinica, no de
disponibilidad: el astigmatismo es un error refractivo (forma de la
cornea), no produce cambios visibles en **ninguna** foto del ojo (ni de
fondo de ojo ni externa). Se agrego solo como contenido informativo en
`DiseaseCatalog.kt` (codigo `AST`, `isDetectableByModel = false`), nunca
como clase entrenable.

## Como se integra cada fuente

Cada fuente externa tiene su propio CSV en `ml/data/` (mismo formato de
columnas que `full_df.csv`, con 0 en las columnas que esa fuente no puede
confirmar) y su propio flag `INCLUDE_EXTERNAL_*` en `ml/train.py`.
`load_dataset()` los concatena si el flag esta en `True` y el archivo
existe, reindexando cada CSV a `LABEL_COLS` (rellena con 0 las columnas
que le falten, por ejemplo CONJ en los CSV de 8 columnas).

| Fuente | CSV | Prefijo de archivo en `preprocessed_images/` |
|---|---|---|
| Miopia (JSIEC-1000 + RFMiD 2.0) | `external_myopia.csv` | `ext_jsiec_myopia_*`, `ext_rfmid2_*` |
| Diabetes (APTOS 2019) | `external_diabetes.csv` | `ext_aptos_dr_*` |
| Conjuntivitis (Kaggle) | `external_conjunctivitis.csv` | `ext_alisofiya_*`, `ext_revengerdna_*` |

## Limitacion de etiquetado (todas las fuentes)

De las imagenes externas solo se confirmo la etiqueta de la clase que
motivo la busqueda desde su dataset de origen; las demas columnas quedan
en 0 aunque el ojo (o, en el caso de conjuntivitis, la persona) pueda
tener ademas otra condicion no reportada por esa fuente puntual. Para las
1167 imagenes "normales" de los datasets de conjuntivitis esto es mas
notorio: al ser fotos de ojo externo, ninguna de las 8 columnas de fondo
de ojo aplica realmente (no se puede evaluar retinopatia diabetica desde
una foto externa), asi que esas filas quedan con las 9 columnas en 0
salvo que sean caso confirmado de conjuntivitis. Es ruido de etiqueta
aceptado a cambio de mas datos reales; dado que las metricas mejoraron de
forma consistente y reproducible, no parece estar perjudicando al modelo.

## Reproducibilidad

`ml/external_raw/` y las imagenes agregadas en
`ml/data/preprocessed_images/` no se suben al repositorio (mismo criterio
que el resto del dataset de entrenamiento). Para reproducir la descarga:

```bash
# JSIEC-1000 (Zenodo, ~403 MB, no se usa en el modelo final)
curl -L -o ml/external_raw/jsiec1000.zip \
  "https://zenodo.org/api/records/3477553/files/1000images.zip/content"

# RFMiD 2.0 (Zenodo, ~64 MB, no se usa en el modelo final)
curl -L -o ml/external_raw/rfmid2.zip \
  "https://zenodo.org/api/records/7505822/files/RFMiD2_0.zip/content"

# APTOS 2019 (Kaggle, ~9.5 GB, requiere cuenta gratuita + unirse a la
# competencia en kaggle.com/competitions/aptos2019-blindness-detection)
kaggle competitions download -c aptos2019-blindness-detection -p ml/external_raw

# Datasets de conjuntivitis (Kaggle, ~75 MB combinados, requiere cuenta)
kaggle datasets download alisofiya/conjunctivitis -p ml/external_raw
kaggle datasets download revengerdna/conjunctivitis-dataset -p ml/external_raw
```

Luego correr el script de extraccion correspondiente a cada fuente (ver el
historial de commits de este archivo y de `ml/train.py`).
