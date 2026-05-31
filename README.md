# 📡 Geologger — Lab 12 : Localisation Temps Réel via GPS et Google Maps

## Objectif

Construire une application Android complète de géolocalisation qui récupère la position GPS en temps réel, envoie les coordonnées vers un serveur PHP/MySQL via Volley, et affiche toutes les positions enregistrées sur une carte Google Maps.

---

## Concepts Abordés

- `LocationManager` et `LocationListener` pour le GPS
- Envoi de requêtes HTTP POST avec **Volley**
- Récupération de JSON depuis un serveur avec `JsonObjectRequest`
- Affichage de marqueurs multiples sur **Google Maps**
- Navigation entre deux activités (`MainActivity` → `MapsActivity`)
- Récupération de l'identifiant appareil (`ANDROID_ID` / IMEI)
- Permissions runtime (localisation)

---

## Architecture du Système

```
Geologger (Android)
       │
       │  POST (latitude, longitude, date, imei)
       ▼
createPosition.php → MySQL (table position)
       │
       │  POST → JSON {"positions": [...]}
       ▼
showPositions.php → MapsActivity (marqueurs)
```

---

## Aperçu de l'Application

### MainActivity
| Élément            | Description                                      |
|-------------------|--------------------------------------------------|
| Titre             | "Geologger" en orange sur fond sombre            |
| Carte GPS         | Affiche latitude et longitude en temps réel      |
| Statut            | Heure de dernière mise à jour / statut envoi     |
| Bouton            | "Voir la carte" → ouvre MapsActivity            |

### MapsActivity
| Élément            | Description                                      |
|-------------------|--------------------------------------------------|
| Titre             | "📍 Positions enregistrées"                      |
| Carte Google Maps | Affiche tous les marqueurs depuis le serveur     |

---

## Structure du Projet Android

```
Geologger/
├── java/com/example/geologger/
│   ├── MainActivity.java
│   └── MapsActivity.java
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   └── activity_maps.xml
│   └── values/
│       ├── colors.xml
│       ├── strings.xml
│       └── themes.xml
└── AndroidManifest.xml
```

---

## Permissions Requises

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

---

## Configuration

### Clé API Google Maps
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="VOTRE_CLE_API" />
```

### Trafic HTTP (XAMPP local)
```xml
<application android:usesCleartextTraffic="true" ... >
```

---

## Détails Clés de l'Implémentation

### Envoi de position avec Volley
```java
private void sendPosition(final double lat, final double lon) {
    StringRequest request = new StringRequest(
            Request.Method.POST, insertUrl,
            response -> tvStatus.setText("Position envoyée ✓"),
            error -> tvStatus.setText("Erreur réseau")
    ) {
        @Override
        protected Map<String, String> getParams() {
            HashMap<String, String> params = new HashMap<>();
            params.put("latitude", String.valueOf(lat));
            params.put("longitude", String.valueOf(lon));
            params.put("date", sdf.format(new Date()));
            params.put("imei", getDeviceIdentifier());
            return params;
        }
    };
    requestQueue.add(request);
}
```

### Chargement des marqueurs depuis le serveur
```java
JsonObjectRequest jsonRequest = new JsonObjectRequest(
        Request.Method.POST, showUrl, null,
        response -> {
            JSONArray positions = response.getJSONArray("positions");
            for (int i = 0; i < positions.length(); i++) {
                JSONObject pos = positions.getJSONObject(i);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                pos.getDouble("latitude"),
                                pos.getDouble("longitude")))
                        .title("Position " + (i + 1)));
            }
        }, error -> {}
);
```

---

## Structure du Serveur PHP (référence)

```
localisation/
├── classe/Position.php
├── connexion/Connexion.php
├── dao/IDao.php
├── service/PositionService.php
├── createPosition.php
└── showPositions.php
```

### Table MySQL
```sql
CREATE TABLE position (
    id INT AUTO_INCREMENT PRIMARY KEY,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    date DATETIME NOT NULL,
    imei VARCHAR(20) NOT NULL
);
```

---

## Choix de Design

- **Thème :** Sombre / Orange
- **Palette de couleurs :** Fond sombre (`#121212`), Orange (`#FF6D00`), Carte (`#2C2C2E`)
- **Navigation :** MainActivity → MapsActivity via bouton

---

## Comment Exécuter

1. Cloner ou ouvrir le projet dans **Android Studio**
2. Ajouter votre clé API Google Maps dans `AndroidManifest.xml`
3. Lancer sur un émulateur ou appareil physique (Android 7.0+)
4. Accepter les permissions de localisation
5. Simuler une position via **Extended Controls → Location**
6. Appuyer sur **"Voir la carte"** pour afficher les positions

---

## Référence du Lab

- **Numéro du lab :** 12
- **Titre :** Localisation Temps Réel via GPS et Google Maps
- **Langage :** Java
- **Min SDK :** 24 (Android 7.0 Nougat)
- **Dépendances :** Volley 1.2.1, Google Maps SDK 18.2.0
