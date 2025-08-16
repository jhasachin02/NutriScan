# NutriScan - Advanced Nutrition Analysis Android App

A scalable Android application built with Kotlin that provides real-time nutrition analysis through barcode scanning, AR food insights, and personalized dietary recommendations.

## 🎯 Features

### Core Functionality
- **Barcode Scanning**: Real-time barcode scanning using ML Kit with camera integration
- **Menu Access**: Access to restaurant menus and food databases through API integration
- **Nutrition Analysis**: Advanced recipe parsing and nutritional calculation engine
- **AR Food Insights**: Augmented reality food visualization with nutritional overlays
- **Custom Profiles**: Personalized user profiles with dietary preferences and health goals
- **Fitness Sync**: Integration with Google Fit API for calorie and activity tracking

### Advanced Features
- **Real-time API Integration**: Live food data from multiple nutrition databases
- **Offline Support**: Local caching with Room database for offline functionality
- **Push Notifications**: Firebase integration for meal reminders and goal updates
- **Intuitive UI/UX**: Material Design 3 with responsive layouts
- **Multi-language Support**: Localized strings for global audience

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with the following components:

### Project Structure
```
app/
├── src/main/java/com/nutriscan/app/
│   ├── data/
│   │   ├── api/                    # API services and DTOs
│   │   ├── database/               # Room database components
│   │   ├── models/                 # Data models and entities
│   │   └── repository/             # Repository pattern implementation
│   ├── di/                         # Dependency injection (Hilt)
│   ├── nutrition/                  # Nutrition analysis engine
│   ├── services/                   # Background services
│   └── ui/
│       ├── ar/                     # AR functionality
│       ├── main/                   # Main navigation
│       ├── profile/                # User profile management
│       └── scanner/                # Barcode scanning
└── res/
    ├── layout/                     # XML layouts
    ├── values/                     # Resources (colors, strings)
    └── drawable/                   # Icons and graphics
```

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Traditional Views (for camera/AR)
- **Architecture**: MVVM with Repository Pattern
- **Dependency Injection**: Hilt
- **Database**: Room (SQLite)
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil + Glide

### Specialized Libraries
- **Camera**: CameraX
- **ML/AI**: ML Kit (Barcode Scanning)
- **AR**: ARCore + Sceneform
- **Charts**: MPAndroidChart
- **Authentication**: Firebase Auth
- **Cloud Storage**: Firebase Firestore
- **Push Notifications**: Firebase Messaging
- **Fitness Integration**: Google Fit API

### Development Tools
- **Build System**: Gradle (Kotlin DSL)
- **Testing**: JUnit, Espresso, Mockito
- **Code Quality**: Ktlint, Detekt
- **Version Control**: Git

## 📱 Key Components

### 1. Barcode Scanner Module
- Real-time barcode detection using ML Kit
- Camera permission handling
- Product lookup integration
- Manual entry fallback

### 2. Nutrition Analysis Engine
- BMR and TDEE calculations
- Macro/micronutrient analysis
- Goal-based recommendations
- Health scoring algorithm
- Dietary restriction validation

### 3. API Integration Layer
- Multiple food database APIs
- Restaurant menu integration
- Recipe search functionality
- Caching strategy implementation

### 4. AR Visualization System
- ARCore integration
- 3D nutritional overlays
- Real-time data rendering
- Interactive touch controls

### 5. User Profile System
- Firebase Authentication
- Customizable health goals
- Activity level tracking
- Dietary preferences
- Progress monitoring

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26+
- Kotlin 1.9.10+
- Google Services JSON file

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/nutriscan-app.git
   cd nutriscan-app
   ```

2. **Add Google Services configuration**
   - Place your `google-services.json` file in the `app/` directory
   - Configure Firebase project settings

3. **Set up API keys**
   ```kotlin
   // In local.properties or BuildConfig
   FOOD_API_KEY="your-food-database-api-key"
   EDAMAM_API_KEY="your-edamam-api-key"
   MAPS_API_KEY="your-google-maps-api-key"
   ```

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```

### Configuration

#### Firebase Setup
1. Create a Firebase project
2. Enable Authentication, Firestore, and Messaging
3. Download and add `google-services.json`

#### API Integrations
- **OpenFoodFacts**: Free food database API
- **Edamam**: Recipe and nutrition API
- **Google Fit**: Fitness data integration
- **Google Maps**: Location services for restaurants

## 📊 Data Models

### Core Models
```kotlin
data class FoodItem(
    val id: String,
    val name: String,
    val nutritionFacts: NutritionFacts,
    val ingredients: List<String>,
    // ... other properties
)

data class UserProfile(
    val id: String,
    val goals: List<HealthGoal>,
    val dietaryRestrictions: List<DietaryRestriction>,
    // ... other properties
)
```

### Nutrition Analysis
```kotlin
data class NutritionAnalysis(
    val healthScore: Double,
    val recommendationLevel: RecommendationLevel,
    val goalAnalysis: GoalAnalysis,
    // ... analysis results
)
```

## 🔧 Key Features Implementation

### Barcode Scanning
```kotlin
@AndroidEntryPoint
class BarcodeScannerActivity : AppCompatActivity() {
    private val viewModel: BarcodeScannerViewModel by viewModels()
    
    private fun startCamera() {
        val imageAnalyzer = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
        }
        // Camera setup and ML Kit integration
    }
}
```

### Nutrition Analysis
```kotlin
@Singleton
class NutritionAnalyzer @Inject constructor() {
    
    fun calculateDailyGoals(profile: UserProfile): DailyNutritionGoals {
        val bmr = calculateBasalMetabolicRate(profile)
        val tdee = calculateTotalDailyEnergyExpenditure(bmr, profile.activityLevel)
        // Goal calculations based on user profile
    }
}
```

### AR Integration
```kotlin
class ARActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    
    private fun displayNutritionInfo(foodItem: FoodItem) {
        val insights = nutritionAnalyzer.generateARInsights(foodItem, userProfile)
        // AR rendering of nutrition data
    }
}
```

## 🧪 Testing Strategy

### Unit Tests
- Nutrition calculation algorithms
- API response parsing
- Database operations
- Business logic validation

### Integration Tests
- API connectivity
- Database migrations
- Camera functionality
- AR rendering

### UI Tests
- User flows
- Navigation
- Form validation
- Accessibility

## 📈 Performance Optimizations

### Network Efficiency
- Response caching
- Image optimization
- Background sync
- Offline-first approach

### Memory Management
- Image loading optimization
- Database query optimization
- AR scene management
- Lifecycle-aware components

### Battery Optimization
- Efficient camera usage
- Background task optimization
- Location service management
- Push notification batching

## 🔐 Security & Privacy

### Data Protection
- User data encryption
- Secure API communication
- Local storage security
- Privacy compliance (GDPR)

### Permissions
- Runtime permission handling
- Minimal permission requests
- Clear permission rationale
- Graceful degradation

## 🚀 Deployment

### Release Build
```bash
./gradlew assembleRelease
```

### Play Store Preparation
- App signing configuration
- ProGuard optimization
- Asset optimization
- Metadata preparation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Follow coding standards
4. Add tests for new features
5. Submit a pull request

### Code Style
- Follow Kotlin coding conventions
- Use Ktlint for formatting
- Document public APIs
- Write meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

For support, email support@nutriscan.app or create an issue in the repository.

## 🗺️ Roadmap

### Phase 1 (Current)
- [x] Core barcode scanning
- [x] Basic nutrition analysis
- [x] User profile system
- [x] API integration

### Phase 2
- [ ] AR food insights
- [ ] Restaurant menu integration
- [ ] Fitness app sync
- [ ] Advanced analytics

### Phase 3
- [ ] AI-powered recommendations
- [ ] Social features
- [ ] Meal planning
- [ ] Wearable integration

---

**Built with ❤️ for healthier eating habits**
