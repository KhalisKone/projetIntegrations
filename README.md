# SmartVision OCR 📱

Une application Android avancée de reconnaissance optique de caractères (OCR) avec authentification Firebase et monétisation intégrée.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![TensorFlow](https://img.shields.io/badge/TensorFlow-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)

## ✨ Fonctionnalités

### 🔐 Authentification
- Connexion/Inscription avec email et mot de passe
- Authentification sécurisée via Firebase Authentication
- Gestion des sessions utilisateur

### 🤖 OCR Avancé
- **Deux méthodes OCR :**
  - **Simple** : Google ML Kit (rapide et fiable)
  - **Avancé** : Modèle CRAFT TensorFlow Lite (précision supérieure)
- Sélection utilisateur via interface radio buttons
- Prétraitement automatique des images

### 💰 Système Freemium
- **Utilisateurs abonnés** : Copie immédiate du texte extrait
- **Utilisateurs gratuits** : Regardent une publicité pour copier
- Gestion des abonnements dans Firebase Realtime Database

### 📸 Traitement d'Images
- Chargement depuis Galerie ou Appareil photo
- Prise de photo directement dans l'application
- Optimisation automatique des images

### 🎨 Interface Moderne
- Design Material Design
- Logo personnalisé comme icône d'application
- Interface adaptative selon le statut d'abonnement
- Feedback visuel intuitif

## 🛠️ Technologies Utilisées

- **Langage** : Kotlin
- **Framework** : Android SDK (API 34)
- **Build** : Gradle 8.10
- **Authentification** : Firebase Authentication
- **Base de données** : Firebase Realtime Database
- **OCR** : Google ML Kit + TensorFlow Lite
- **Monétisation** : Google AdMob
- **Architecture** : MVVM

## 📋 Prérequis

- Android Studio Arctic Fox ou supérieur
- JDK 17
- Android SDK API 34
- Dispositif Android ou émulateur

## 🚀 Installation & Configuration

### 1. Cloner le repository
```bash
git clone https://github.com/KhalisKone/projetIntegrations.git
cd projetIntegrations/mobile
```

### 2. Configuration Firebase
1. Créer un projet Firebase sur [Firebase Console](https://console.firebase.google.com/)
2. Activer Authentication et Realtime Database
3. Télécharger `google-services.json` et le placer dans `app/`
4. Configurer les règles de base de données :
```json
{
  "rules": {
    "users": {
      "$userId": {
        "subscription": {
          ".read": "auth != null && auth.uid == $userId",
          ".write": "auth != null && auth.uid == $userId"
        }
      }
    }
  }
}
```

### 3. Configuration AdMob
1. Créer un compte AdMob
2. Créer une application et obtenir l'App ID
3. Remplacer l'App ID dans `AndroidManifest.xml` :
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-VOTRE_APP_ID"/>
```

### 4. Modèles OCR (Optionnel)
Pour utiliser la méthode CRAFT avancée :
- Placer `craft_mlt_25k.tflite` dans `app/src/main/assets/`
- Le modèle sera automatiquement chargé au démarrage

### 5. Compilation
```bash
# Configurer Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Compiler l'APK Debug
./gradlew assembleDebug

# Compiler l'APK Release
./gradlew assembleRelease
```

## 📱 Utilisation

### Installation
```bash
# Via ADB (téléphone connecté)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Ou installer manuellement l'APK sur votre téléphone
```

### Fonctionnement
1. **Lancer l'application** : SmartVision OCR
2. **S'authentifier** : Créer un compte ou se connecter
3. **Charger une image** : Galerie ou appareil photo
4. **Sélectionner la méthode OCR** : Simple (ML Kit) ou Avancé (CRAFT)
5. **Extraire le texte** : L'application analyse l'image
6. **Copier le texte** :
   - **Abonnés** : Copie immédiate
   - **Non-abonnés** : Regarder une pub puis copier

## 🏗️ Architecture

```
app/
├── src/main/
│   ├── java/com/smartvision/app/
│   │   ├── MainActivity.kt              # Écran d'authentification
│   │   ├── ImageProcessingActivity.kt   # Écran principal OCR
│   │   └── OCRProcessor.kt              # Logique OCR
│   ├── res/
│   │   ├── layout/                      # Interfaces utilisateur
│   │   ├── drawable/                    # Images et icônes
│   │   └── values/                      # Styles et chaînes
│   └── assets/                          # Modèles ML (optionnel)
└── build.gradle.kts                     # Configuration build
```

## 🔧 Scripts Disponibles

```bash
# Compilation complète
./gradlew build

# Génération APK Debug
./gradlew assembleDebug

# Génération APK Release
./gradlew assembleRelease

# Nettoyage du cache
./gradlew clean

# Installation sur device
./gradlew installDebug
```

## 📊 Fonctionnalités Détaillées

### OCRProcessor.kt
- **extractText()** : Interface principale avec choix de méthode
- **extractTextWithMlKit()** : OCR Google ML Kit
- **extractTextWithCraft()** : OCR TensorFlow Lite CRAFT
- **Gestion des erreurs** et fallback automatique

### Authentification
- **MainActivity** : Login/Register avec validation
- **ImageProcessingActivity** : Vérification automatique du statut
- **Navigation fluide** entre écrans

### Monétisation
- **AdMob intégré** : Publicités interstitielles
- **Système d'abonnement** : Stockage Firebase
- **Interface adaptative** : Comportement différent selon abonnement

## 🐛 Dépannage

### Problèmes courants :
- **Java version** : Utiliser JDK 17 (`export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`)
- **Firebase** : Vérifier `google-services.json`
- **Modèles manquants** : L'application fonctionne sans modèles (ML Kit uniquement)
- **Permissions** : Vérifier les permissions caméra et stockage

### Logs de débogage :
```bash
# Voir les logs de l'application
adb logcat | grep -i smartvision

# Logs détaillés
adb logcat -v time | grep -i "ocr\|firebase\|admob"
```

## 📈 Métriques & Performance

- **Taille APK** : ~135-140 MB (avec modèles)
- **API minimum** : Android 7.0 (API 24)
- **API cible** : Android 14 (API 34)
- **Dépendances** : Firebase, ML Kit, TensorFlow Lite, AdMob

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📝 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👨‍💻 Auteur

**KhalisKone** - *Développement complet*

- GitHub: [@KhalisKone](https://github.com/KhalisKone)
- LinkedIn: [Votre profil LinkedIn]

## 🙏 Remerciements

- Google pour ML Kit et Firebase
- TensorFlow pour les modèles d'OCR
- Communauté Android pour les ressources

---

**⭐ Si ce projet vous plaît, n'hésitez pas à lui donner une étoile !**

*Dernière mise à jour : Octobre 2025*</content>
<filePath>/home/khalis/proint/mobile/README.md