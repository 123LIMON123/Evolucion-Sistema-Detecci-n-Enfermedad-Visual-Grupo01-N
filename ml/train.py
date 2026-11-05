"""
Entrena un clasificador multi-etiqueta (N, C, CONJ) de condiciones oculares visibles en una
foto de OJO EXTERNO (camara de celular, no fondo de ojo) y exporta un .tflite listo para la
app Android.

Historial: la primera version de este modelo (ver ml/EXTERNAL_DATASETS.md) tenia 9 clases
(N, D, G, C, A, H, M, O, CONJ) entrenadas sobre ODIR-5K, un dataset de fotos de FONDO DE OJO
(retina). El problema: la app le pide al usuario una foto del ojo POR FUERA ("Centra el ojo
dentro del aro", camara normal del celular), no una foto de fondo de ojo (que requiere una
camara especializada apuntando a traves de la pupila dilatada). De esas 8 clases de ODIR-5K,
solo Catarata (C) tiene un correlato visible externamente (opacidad blanquecina en la pupila
en estadio avanzado); el resto (D, G, A, H, M, O) son hallazgos de retina o un error
refractivo, invisibles en cualquier foto externa sin importar cuantos datos se sumen. Ver
ml/EXTERNAL_DATASETS.md para el detalle completo de esta investigacion.

Por eso este modelo se redujo a 3 clases, todas entrenadas EXCLUSIVAMENTE con fotos de ojo
externo (nunca fondo de ojo): N (normal), C (catarata) y CONJ (conjuntivitis). ODIR-5K ya no
se usa como fuente de entrenamiento.

Requiere: ml/data/external_conjunctivitis.csv + ml/data/external_cataract.csv y sus imagenes
en ml/data/preprocessed_images/.

Nota: esta maquina no tiene soporte GPU nativo en TensorFlow para Windows
(TF >= 2.11 solo acelera por GPU vía WSL2/DirectML), asi que se entrena en CPU
con una arquitectura liviana (MobileNetV2 alpha=0.35 @ 160x160) para que el
entrenamiento sea razonable en tiempo.
"""
import os
import time

import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split

DATA_DIR = os.path.join(os.path.dirname(__file__), "data")
IMAGES_DIR = os.path.join(DATA_DIR, "preprocessed_images")

# 548 imagenes de conjuntivitis + 1167 de ojo externo sin conjuntivitis (Kaggle:
# alisofiya/conjunctivitis, revengerdna/conjunctivitis-dataset). Las 1167 "sin conjuntivitis"
# llegan en el CSV con las 9 columnas en 0 (ver ml/EXTERNAL_DATASETS.md, "Limitacion de
# etiquetado": solo se confirmo lo que motivo la busqueda desde ese dataset). Para el modelo de
# 3 clases, donde N significa "ojo externo sin catarata ni conjuntivitis visibles", esas filas
# se reetiquetan N=1 aqui mismo (no se edita el CSV original, para no perder el dato crudo).
EXTERNAL_CONJUNCTIVITIS_CSV = os.path.join(DATA_DIR, "external_conjunctivitis.csv")

# 2124 imagenes de catarata (986 immature + 1138 mature) + 1103 normales, foto externa, de
# Kaggle (rifdana/cataract-photo-image-dataset, CC BY-SA 4.0). Ya vienen con N/C correctos.
EXTERNAL_CATARACT_CSV = os.path.join(DATA_DIR, "external_cataract.csv")

OUT_DIR = os.path.dirname(__file__)

LABEL_COLS = ["N", "C", "CONJ"]
IMG_SIZE = 160
BATCH_SIZE = 32
SEED = 42

ASSETS_OUT = os.path.join(
    os.path.dirname(__file__), "..", "app", "src", "main", "assets", "ocular_disease_model.tflite"
)


def log(msg):
    print(f"[{time.strftime('%H:%M:%S')}] {msg}", flush=True)


def _read_labels_csv(csv_path, relabel_zero_rows_as_normal=False):
    """Lee un CSV de 9 columnas (mismo formato historico) y se queda solo con LABEL_COLS."""
    raw = pd.read_csv(csv_path)
    out = raw[["filename"] + LABEL_COLS].copy()
    if relabel_zero_rows_as_normal:
        all_zero = (out[LABEL_COLS].sum(axis=1) == 0)
        out.loc[all_zero, "N"] = 1
    return out


def load_dataset():
    log("Leyendo CSVs de etiquetas (solo fuentes de ojo externo)...")
    conj_df = _read_labels_csv(EXTERNAL_CONJUNCTIVITIS_CSV, relabel_zero_rows_as_normal=True)
    log(f"  conjuntivitis: {len(conj_df)} filas ({int(conj_df['CONJ'].sum())} CONJ, {int(conj_df['N'].sum())} N)")

    cataract_df = _read_labels_csv(EXTERNAL_CATARACT_CSV)
    log(f"  catarata: {len(cataract_df)} filas ({int(cataract_df['C'].sum())} C, {int(cataract_df['N'].sum())} N)")

    df = pd.concat([conj_df, cataract_df], ignore_index=True)

    df = df[df["filename"].apply(lambda f: os.path.exists(os.path.join(IMAGES_DIR, f)))]
    log(f"{len(df)} imagenes con archivo presente en preprocessed_images/")

    labels = df[LABEL_COLS].values.astype("float32")
    filenames = df["filename"].tolist()

    log("Cargando y redimensionando imagenes en memoria (una sola vez)...")
    images = np.zeros((len(filenames), IMG_SIZE, IMG_SIZE, 3), dtype="uint8")
    for i, fname in enumerate(filenames):
        img = tf.io.read_file(os.path.join(IMAGES_DIR, fname))
        img = tf.io.decode_jpeg(img, channels=3)
        img = tf.image.resize(img, [IMG_SIZE, IMG_SIZE], method="bilinear")
        images[i] = img.numpy().astype("uint8")
        if (i + 1) % 1000 == 0:
            log(f"  {i + 1}/{len(filenames)} imagenes procesadas")

    return images, labels


def build_model():
    base = tf.keras.applications.MobileNetV2(
        input_shape=(IMG_SIZE, IMG_SIZE, 3),
        alpha=0.35,
        include_top=False,
        weights="imagenet",
        pooling="avg",
    )
    base.trainable = False

    inputs = tf.keras.Input(shape=(IMG_SIZE, IMG_SIZE, 3))
    x = tf.keras.applications.mobilenet_v2.preprocess_input(inputs)
    x = base(x, training=False)
    x = tf.keras.layers.Dropout(0.3)(x)
    outputs = tf.keras.layers.Dense(len(LABEL_COLS), activation="sigmoid")(x)
    model = tf.keras.Model(inputs, outputs)
    return model, base


def main():
    images, labels = load_dataset()

    primary = labels.argmax(axis=1)
    idx = np.arange(len(images))
    train_idx, val_idx = train_test_split(
        idx, test_size=0.15, random_state=SEED, stratify=primary
    )
    log(f"Train: {len(train_idx)}  Val: {len(val_idx)}")

    # peso por clase para compensar el desbalance (CONJ es minoria: 548 de ~4942)
    class_weights = {}
    for i, col in enumerate(LABEL_COLS):
        pos = labels[train_idx, i].sum()
        neg = len(train_idx) - pos
        class_weights[i] = float(neg / max(pos, 1)) if pos > 0 else 1.0
    log(f"Pesos por clase (para referencia): {dict(zip(LABEL_COLS, [round(class_weights[i], 2) for i in range(len(LABEL_COLS))]))}")

    augmenter = tf.keras.Sequential([
        tf.keras.layers.RandomFlip("horizontal"),
        tf.keras.layers.RandomRotation(0.05),
        tf.keras.layers.RandomZoom(0.1),
        tf.keras.layers.RandomContrast(0.15),
        tf.keras.layers.RandomBrightness(0.15),
    ])

    def make_dataset(indices, training):
        x = images[indices]
        y = labels[indices]
        ds = tf.data.Dataset.from_tensor_slices((x, y))
        if training:
            ds = ds.shuffle(2000, seed=SEED)

        ds = ds.batch(BATCH_SIZE)
        if training:
            ds = ds.map(
                lambda img, lbl: (augmenter(img, training=True), lbl),
                num_parallel_calls=tf.data.AUTOTUNE,
            )
        return ds.prefetch(tf.data.AUTOTUNE)

    train_ds = make_dataset(train_idx, training=True)
    val_ds = make_dataset(val_idx, training=False)

    model, base = build_model()

    weight_vec = tf.constant([class_weights[i] for i in range(len(LABEL_COLS))], dtype=tf.float32)

    def weighted_bce(y_true, y_pred):
        eps = 1e-7
        y_pred = tf.clip_by_value(y_pred, eps, 1 - eps)
        per_class = -(y_true * tf.math.log(y_pred) * weight_vec + (1 - y_true) * tf.math.log(1 - y_pred))
        return tf.reduce_mean(per_class)

    checkpoint_path = os.path.join(OUT_DIR, "best_weights.weights.h5")
    checkpoint = tf.keras.callbacks.ModelCheckpoint(
        checkpoint_path, monitor="val_auc", mode="max", save_best_only=True, save_weights_only=True
    )

    def early_stopping(patience):
        return tf.keras.callbacks.EarlyStopping(monitor="val_auc", mode="max", patience=patience)

    def reduce_lr(patience, min_lr):
        return tf.keras.callbacks.ReduceLROnPlateau(
            monitor="val_auc", mode="max", factor=0.5, patience=patience, min_lr=min_lr
        )

    log("=== Fase 1: entrenando solo la cabeza (backbone congelado) ===")
    model.compile(
        optimizer=tf.keras.optimizers.Adam(1e-3),
        loss=weighted_bce,
        metrics=[tf.keras.metrics.AUC(name="auc", multi_label=True)],
    )
    model.fit(
        train_ds, validation_data=val_ds, epochs=35,
        callbacks=[early_stopping(patience=8), reduce_lr(patience=3, min_lr=1e-5), checkpoint],
    )

    # Mismo hallazgo que en la version de 9 clases: con un dataset de este tamano, descongelar
    # el backbone sobreajusta antes de aportar algo. FINE_TUNE queda en False.
    FINE_TUNE = False
    if FINE_TUNE:
        log("=== Fase 2: fine-tuning del backbone (lr bajo) ===")
        base.trainable = True
        for layer in base.layers[:-25]:
            layer.trainable = False

        model.compile(
            optimizer=tf.keras.optimizers.Adam(5e-6),
            loss=weighted_bce,
            metrics=[tf.keras.metrics.AUC(name="auc", multi_label=True)],
        )
        model.fit(
            train_ds, validation_data=val_ds, epochs=15,
            callbacks=[early_stopping(patience=5), checkpoint],
        )

    log(f"Restaurando el mejor peso visto en todo el entrenamiento ({checkpoint_path})...")
    model.load_weights(checkpoint_path)

    log("Evaluando en validacion...")
    results = model.evaluate(val_ds)
    log(f"Resultado final val: {dict(zip(model.metrics_names, results))}")

    log("Convirtiendo a TFLite (float16)...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]
    tflite_model = converter.convert()

    os.makedirs(os.path.dirname(ASSETS_OUT), exist_ok=True)
    with open(ASSETS_OUT, "wb") as f:
        f.write(tflite_model)
    log(f"Modelo guardado en {ASSETS_OUT} ({len(tflite_model) / 1e6:.2f} MB)")
    log(f"Orden de clases del modelo: {LABEL_COLS}")


if __name__ == "__main__":
    main()
