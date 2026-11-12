# Enlaces de los datasets utilizados

Este archivo reune en un solo lugar los enlaces de todos los datasets
mencionados en [`EXTERNAL_DATASETS.md`](EXTERNAL_DATASETS.md) y usados en algun
momento por `ml/train.py`. El detalle de por que se uso o se descarto cada
uno esta en ese archivo; aca solo quedan los enlaces de origen.

## Datasets del modelo que se shipea hoy (3 clases: N, C, CONJ)

Foto de ojo externo, no de fondo de ojo.

| Dataset | Enlace | Licencia | Clase |
|---|---|---|---|
| Cataract Photo Image Dataset (rifdana) | https://www.kaggle.com/datasets/rifdana/cataract-photo-image-dataset | CC BY-SA 4.0 | C (catarata) |
| Conjunctivitis (alisofiya) | https://www.kaggle.com/datasets/alisofiya/conjunctivitis | No especificada | CONJ (conjuntivitis) |
| Conjunctivitis Dataset (revengerdna) | https://www.kaggle.com/datasets/revengerdna/conjunctivitis-dataset | MIT | CONJ (conjuntivitis) |

## Datasets de la etapa anterior (modelo de 9 clases, fondo de ojo — ya no se shipea)

| Dataset | Enlace | Licencia | Clase | Se uso en el modelo final de esa etapa |
|---|---|---|---|---|
| ODIR-5K (dataset principal original) | No hay enlace registrado en este repo | — | N, D, G, C, A, H, M, O | Si (fue el dataset base) |
| APTOS 2019 Blindness Detection | https://www.kaggle.com/competitions/aptos2019-blindness-detection | Reglas de la competencia de Kaggle | D (diabetes) | Si |
| JSIEC-1000 | https://doi.org/10.5281/zenodo.3477553 | Acceso abierto (Zenodo) | M (miopia) | No (no mejoro el resultado) |
| RFMiD 2.0 | https://doi.org/10.5281/zenodo.7505822 | Acceso abierto (Zenodo) | M (miopia) | No (no mejoro el resultado) |

> **Nota:** el modelo de 9 clases de esta tabla ya no es el que usa la app
> (ver "Correccion de alcance" en `EXTERNAL_DATASETS.md`): detectaba
> enfermedades de fondo de ojo que la camara del celular nunca puede
> fotografiar. Se deja esta tabla solo como referencia historica/reproducibilidad.
