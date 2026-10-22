# Datasets externos evaluados para reforzar la clase Miopia (M)

El dataset principal de entrenamiento es **ODIR-5K**
(`ml/data/full_df.csv` + `ml/data/preprocessed_images/`), pero la clase M
(miopia patologica) es minoritaria ahi: solo 306 de 6392 filas (~4.8%).
Para intentar reforzarla se sumaron imagenes de miopia patologica
confirmada desde otros dos datasets publicos e independientes de ODIR-5K.

## Resultado (importante, leer antes de reactivar `INCLUDE_EXTERNAL_MYOPIA`)

**La integracion funciona, pero no mejoro la metrica.** Se corrio el mismo
entrenamiento dos veces con las 97 imagenes externas sumadas:

| Configuracion | AUC macro (val.) | AUC de M (val.) |
|---|---|---|
| Solo ODIR-5K (306 ejemplos de M) | **0.7888** | **0.9765** |
| + 97 imagenes externas (403 ejemplos de M), corrida 1 | 0.7863 | 0.9694 |
| + 97 imagenes externas (403 ejemplos de M), corrida 2 | 0.7886 | 0.9729 |

En las dos corridas con el agregado, el AUC de M quedo levemente **por
debajo** del modelo entrenado solo con ODIR-5K, no por encima. La lectura
mas probable: M ya rendia cerca del techo (AUC ~0.97-0.98) usando solo
ODIR-5K, es decir, no estaba limitada por falta de datos. JSIEC-1000 y
RFMiD 2.0 vienen de camaras de fondo de ojo y poblaciones distintas a
ODIR-5K; sumar esas 97 imagenes introduce un corrimiento de dominio (otro
balance de color, otro recorte, otro sensor) que pesa mas que el beneficio
de tener mas ejemplos, justamente porque la clase no necesitaba mas
ejemplos para empezar.

Por eso `ml/train.py` deja `INCLUDE_EXTERNAL_MYOPIA = False` por defecto
(mismo patron que `FINE_TUNE` para el backbone): el codigo de integracion
se mantiene, documentado y probado, pero el modelo que se shipea en
`app/src/main/assets/ocular_disease_model.tflite` es el entrenado solo con
ODIR-5K.

Dato para una futura iteracion: las clases que de verdad rinden mal en
este modelo no son las de miopia/astigmatismo, sino **N (Normal, AUC
~0.69), D (Diabetes, AUC ~0.70) y O (Otras, AUC ~0.63)** — ahi es donde un
dataset adicional probablemente tendria mas impacto real.

## Fuentes

| Dataset | Origen | Licencia | Imagenes sumadas |
|---|---|---|---|
| **JSIEC-1000** ("1000 Fundus images with 39 categories") | Joint Shantou International Eye Centre, via Zenodo (DOI [10.5281/zenodo.3477553](https://doi.org/10.5281/zenodo.3477553)) | Open ("other-open" en el registro de Zenodo) | 54 (carpeta `9.Pathological myopia`) |
| **RFMiD 2.0** (Retinal Fundus Multi-Disease Image Dataset 2.0) | Panchal et al., via Zenodo (DOI [10.5281/zenodo.7505822](https://doi.org/10.5281/zenodo.7505822)) | Acceso abierto | 43 (columna `MYA = 1` en las etiquetas, combinando los splits train/val/test) |

**Total agregado: 97 imagenes adicionales con miopia patologica confirmada**,
desde dos fuentes con equipo y poblacion distintos a ODIR-5K, lo que
tambien ayuda a que el modelo no sobreajuste a las particularidades de una
sola fuente de imagenes para esa clase.

No se encontro ningun dataset publico y descargable de **astigmatismo**
en formato de foto de ojo compatible con este pipeline (ver la decision
documentada en `DiseaseCatalog.kt`: el astigmatismo es un error refractivo,
no una condicion visible en fondo de ojo, por eso se agrego solo como
contenido informativo y no como clase entrenable).

## Como se integraron

1. Se descargaron ambos datasets desde Zenodo (acceso abierto, sin
   necesidad de cuenta) a `ml/external_raw/` (carpeta ignorada por git,
   igual que `ml/data/`).
2. De JSIEC-1000 se extrajeron las 54 imagenes de la carpeta
   `9.Pathological myopia` dentro del zip.
3. De RFMiD 2.0 se leyeron las tres planillas de etiquetas
   (`RFMiD_2_{Training,Validation,Testing}_labels.csv`) y se extrajeron las
   43 imagenes cuya columna `MYA` (miopia) vale 1.
4. Ambos subconjuntos se copiaron a `ml/data/preprocessed_images/` con
   prefijo `ext_jsiec_myopia_*` / `ext_rfmid2_*` para dejar claro el origen,
   y sus etiquetas quedaron en `ml/data/external_myopia.csv` (mismo formato
   de columnas que `full_df.csv`: `filename,N,D,G,C,A,H,M,O`).
5. `ml/train.py` (`load_dataset`) concatena `full_df.csv` con
   `external_myopia.csv` si este ultimo existe, antes de construir los
   arreglos de imagenes/etiquetas.

## Limitacion de etiquetado (importante)

De las 97 imagenes externas solo se confirmo la etiqueta de miopia desde su
dataset de origen. Las otras 7 columnas (N, D, G, C, A, H, O) se dejaron en
0 por simplicidad, aunque en teoria un ojo con miopia patologica podria
tener ademas, por ejemplo, retinopatia diabetica no reportada por esa
fuente puntual. Esto introduce algo de ruido de etiqueta en esas 97 filas
(un posible falso negativo en alguna de las otras 7 clases), aceptado a
cambio de mas datos reales para la clase minoritaria M. Mapear las ~46
categorias de RFMiD 2.0 y las 39 de JSIEC-1000 una por una a las 8
categorias de ODIR-5K no se hizo por alcance: solo se extrajo la etiqueta
de miopia, que es la que se necesitaba reforzar.

## Reproducibilidad

`ml/external_raw/` y las imagenes agregadas en `ml/data/preprocessed_images/`
no se suben al repositorio (mismo criterio que el resto del dataset de
entrenamiento). Para reproducir la descarga y extraccion:

```bash
# JSIEC-1000 (Zenodo, ~403 MB)
curl -L -o ml/external_raw/jsiec1000.zip \
  "https://zenodo.org/api/records/3477553/files/1000images.zip/content"

# RFMiD 2.0 (Zenodo, ~64 MB)
curl -L -o ml/external_raw/rfmid2.zip \
  "https://zenodo.org/api/records/7505822/files/RFMiD2_0.zip/content"
```

Luego correr el script de extraccion (ver el historial de commits de este
archivo, o reconstruirlo siguiendo el paso 2-4 de la seccion anterior).
