# 🔑 API Keys Setup Guide for NutriScan

## Required API Keys

You need to obtain and configure the following API keys to make the app fully functional:

---

## 1. 🔥 **Firebase Configuration** (REQUIRED)

### What you need:
- **google-services.json** file
- Firebase project configuration

### Steps to get it:
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project called "NutriScan" (or use existing)
3. Add an Android app with package name: `com.nutriscan.app`
4. Download the `google-services.json` file
5. Place it in: `app/google-services.json`

### Enable these Firebase services:
- ✅ **Authentication** (Email/Password, Google Sign-in)
- ✅ **Cloud Firestore** (for user profiles and data sync)
- ✅ **Cloud Messaging** (for push notifications)
- ✅ **Cloud Storage** (for image uploads)

---

## 2. 🍎 **Food Database APIs**

### OpenFoodFacts API (FREE - Recommended)
- **URL**: https://world.openfoodfacts.org/
- **API Key**: Not required (it's free!)
- **Usage**: Primary food database for barcode lookup
- **Setup**: Already configured in the app

### Edamam Food Database API (FREEMIUM)
- **URL**: https://developer.edamam.com/food-database-api
- **Free Tier**: 100 requests/month
- **Paid Plans**: Starting at $0.10/request
- **Usage**: Enhanced nutrition data and recipe analysis

**To get Edamam API key:**
1. Sign up at https://developer.edamam.com/
2. Create a new application
3. Select "Food Database API"
4. Copy your **Application ID** and **Application Key**

---

## 3. 🏃‍♂️ **Google Fit API** (FREE)

### What you need:
- Google Cloud Console project
- Google Fit API enabled
- OAuth 2.0 credentials

### Steps:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or use existing
3. Enable **Fitness API**
4. Create **OAuth 2.0 Client ID** for Android
5. Add SHA-1 certificate fingerprint

**To get SHA-1 fingerprint:**
```bash
# For debug builds
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

---

## 4. 🗺️ **Google Maps API** (FREEMIUM - Optional)

### Usage: Restaurant location services
- **Free Tier**: $200 credit monthly
- **API needed**: Maps SDK for Android + Places API

### Steps:
1. Go to Google Cloud Console
2. Enable **Maps SDK for Android** and **Places API**
3. Create **API Key**
4. Restrict key to Android apps

---

## 🔧 **How to Add API Keys to the App**

### Method 1: Build Config (Recommended)
Add to `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        buildConfigField("String", "EDAMAM_APP_ID", "\"your_app_id_here\"")
        buildConfigField("String", "EDAMAM_APP_KEY", "\"your_app_key_here\"")
        buildConfigField("String", "MAPS_API_KEY", "\"your_maps_api_key_here\"")
    }
}
```

### Method 2: local.properties (More Secure)
Add to `local.properties`:

```properties
EDAMAM_APP_ID=your_app_id_here
EDAMAM_APP_KEY=your_app_key_here
MAPS_API_KEY=your_maps_api_key_here
```

Then in `build.gradle.kts`:
```kotlin
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    defaultConfig {
        buildConfigField("String", "EDAMAM_APP_ID", "\"${localProperties.getProperty("EDAMAM_APP_ID")}\"")
        buildConfigField("String", "EDAMAM_APP_KEY", "\"${localProperties.getProperty("EDAMAM_APP_KEY")}\"")
        buildConfigField("String", "MAPS_API_KEY", "\"${localProperties.getProperty("MAPS_API_KEY")}\"")
    }
}
```

---

## 📁 **File Locations to Update**

### 1. Replace API key in FoodRepository.kt:
```kotlin
// File: app/src/main/java/com/nutriscan/app/data/repository/FoodRepository.kt
companion object {
    private const val API_KEY = BuildConfig.EDAMAM_APP_KEY // Replace this line
}
```

### 2. Add google-services.json:
```
app/
├── google-services.json  ← Add this file here
├── build.gradle.kts
└── src/
```

### 3. Add Maps API key to AndroidManifest.xml:
```xml
<!-- Add this inside <application> tag -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

---

## 💰 **Cost Breakdown**

| Service | Free Tier | Paid Plans |
|---------|-----------|------------|
| **Firebase** | Generous free tier | Pay as you grow |
| **OpenFoodFacts** | Unlimited FREE | N/A |
| **Edamam** | 100 requests/month | $0.10/request |
| **Google Fit** | FREE | N/A |
| **Google Maps** | $200 credit/month | Pay per use |

### **Estimated Monthly Costs for 1000 Users:**
- 🟢 **$0 - $50/month** with free tiers
- 🟡 **$50 - $200/month** for moderate usage
- 🔴 **$200+/month** for heavy usage

---

## ⚡ **Quick Start (Minimal Setup)**

### To get the app working immediately:

1. **Firebase Setup** (5 minutes):
   - Create Firebase project
   - Download `google-services.json` 
   - Place in `app/` folder

2. **Keep OpenFoodFacts** (Already working):
   - No API key needed
   - Basic barcode scanning works immediately

3. **Skip optional APIs initially**:
   - Edamam (can add later for enhanced features)
   - Google Maps (restaurant features still work with cached data)
   - Google Fit (fitness features can be added later)

### **Result**: Core barcode scanning + nutrition analysis works immediately!

---

## 🔒 **Security Best Practices**

1. **Never commit API keys to Git**
2. **Use `local.properties` for sensitive keys**
3. **Add `local.properties` to `.gitignore`**
4. **Use BuildConfig for production**
5. **Restrict API keys to your app package**
6. **Enable API key restrictions in Google Cloud Console**

---

## 🚀 **Production Deployment**

For production builds, use environment variables or secure key management:

```kotlin
// Production-ready approach
companion object {
    private val API_KEY = BuildConfig.FOOD_API_KEY.takeIf { it.isNotEmpty() } 
        ?: System.getenv("FOOD_API_KEY") 
        ?: "fallback_key"
}
```

---

## ❓ **Need Help?**

- **Firebase Issues**: Check [Firebase Documentation](https://firebase.google.com/docs)
- **API Problems**: Verify keys in respective developer consoles
- **Build Errors**: Ensure `google-services.json` is in correct location
- **Network Issues**: Check API quotas and rate limits

**The app will work with just Firebase setup - other APIs enhance the features!** 🎯
