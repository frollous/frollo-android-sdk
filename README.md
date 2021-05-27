## Getting Started

### Requirements

- Android Studio 4.2+
- Kotlin version 1.5.10+
- Gradle tools plugin version 4.2.0+ - In your project level **build.gradle**
    ```
    dependencies {
        classpath "com.android.tools.build:gradle:4.2.1"
        //..
    }
    ```
- Gradle version must be 6.7.1+

    Modify Gradle version in your **gradle-wrapper.properties** as below

    ```
    distributionUrl=https\://services.gradle.org/distributions/gradle-6.7.1-all.zip
    ```
    
- **minSdkVersion** in your gradle file must be **23** or above. Frollo SDK does not support Android versions below Marshmallow (6.0).
    ```
    defaultConfig {
        //..
        minSdkVersion 23
        //..
    }
    ```

- Use AndroidX for your project instead of legacy support libraries. You can either enable "**Use AndroidX**" checkbox while creating a new Android project in Android Studio or migrate your existing project to AndroidX - [Migrating to AndroidX](https://developer.android.com/jetpack/androidx/migrate)

### Integration instructions

To integrate Frollo Android SDK to your Android app use the following steps:

#### Integration using AAR file

1. Goto File > New > New Module > Import JAR/AAR Package
2. Select the file frollo-android-sdk-release-3.8.0.aar and Finish
3. Add below line to the dependencies in your **app/build.gradle** file
    ```
    dependencies {
        //..
        implementation project(":frollo-android-sdk-release-3.8.0")
    }
    ```
4. Copy the provided frollosdk.gradle file to your project's root directory
5. Add this line just below your apply plugins in your **app/build.gradle** file
    ```
    apply from: '../frollosdk.gradle'
    ```
6. Define a **appAuthRedirectScheme** in your module level **build.gradle**. This should be unique redirect uri for your app.

   Example: If your redirect url is `frollo-sdk-example://authorize`, then you would do as below
   ```
   defaultConfig {
       //..
       manifestPlaceholders = ['appAuthRedirectScheme': 'frollo-sdk-example']
       //..
   }
   ```
   For more details see **Integration Requirements** under **OAuth2 Authentication using Authorization Code**
7. Frollo SDK disables auto-backup by default to ensure no data persists between installs. You might run into conflicts during integration if your app has defined **android:allowBackup="true"** in its manifest. Either you can disable auto-backup for your app or override by adding **tools:replace="android:allowBackup"** to **`<application>`** element in your **AndroidManifest.xml**.
8. If you have enabled progaurd please add below lines to your progaurd rules file
   ```
   # KEEP FROM OBFUCATION - Frollo SDK
   -keep class us.frollo.frollosdk.** {*;}
   -keepclassmembers  class us.frollo.frollosdk.** {*;}
   ```
9. Build!

#### Integration by cloning SDK code base

1. Pull the Frollo SDK code base    

      - If you are using GIT version control for your project, add Frollo SDK as a submodule in your project    

        `git submodule add git@github.com:frollous/frollo-android-sdk.git`
    
        `git submodule update --init --recursive`    

      or

      - Clone SDK repo inside your project's root directory    

        `git clone git@github.com:frollous/frollo-android-sdk.git`

    You should see a folder named _frollo-android-sdk_ inside your root project directory and within it, the SDK code.
    
    Checkout a stable release branch
    
      `cd frollo-android-sdk`    
    
      `git fetch`    
            
      `git checkout release/3.8.0` (replace the version number with the most stable version number)

2. Add _frollo-android-sdk_ module to your **settings.gradle** file

    `include ':app', ':frollo-android-sdk'`

3. Add below line to the dependencies in your **app/build.gradle** file    
    ```
    dependencies {
        //..
        implementation project(":frollo-android-sdk")
    }
    ```
4. Define a **appAuthRedirectScheme** in your module level **build.gradle**. This should be unique redirect uri for your app.

   Example: If your redirect url is `frollo-sdk-example://authorize`, then you would do as below

   ```
   defaultConfig {
       //..
       manifestPlaceholders = ['appAuthRedirectScheme': 'frollo-sdk-example']
       //..
   }
   ```
   For more details see **Integration Requirements** under **OAuth2 Authentication using Authorization Code**
5. Frollo SDK disables auto-backup by default to ensure no data persists between installs. You might run into conflicts during integration if your app has defined **android:allowBackup="true"** in its manifest. Either you can disable auto-backup for your app or override by adding **tools:replace="android:allowBackup"** to **`<application>`** element in your **AndroidManifest.xml**.
6. If you have enabled progaurd please add below lines to your progaurd rules file
   ```
   # KEEP FROM OBFUCATION - Frollo SDK
   -keep class us.frollo.frollosdk.** {*;}
   -keepclassmembers  class us.frollo.frollosdk.** {*;}
   ```
7. Build! 👷‍♂️

#### Integration using AAR file inside 'libs' folder of another library module

1. Place the Frollo SDK AAR file inside the 'libs' folder in the desried library module
2. Add below line to the dependencies in your **<_library module name_>/build.gradle** file
    ```
    dependencies {
        //..
        implementation(name: 'frollo-android-sdk-release-3.8.0', ext: 'aar')
    }
    ```
3. Add below to your project level **build.gradle** file
    ```
    allprojects {
        //..
        repositories {
            //..
            flatDir {
                dirs project(':<library module name>').file('libs')
            }
        }
    }
    ```
4. Copy the provided frollosdk.gradle file to your library module's directory
5. Add this line just below your apply plugins in your **<_library module name_>/build.gradle** file
    ```
    apply from: 'frollosdk.gradle'
    ```
6. Define a **appAuthRedirectScheme** in your **app** and **module** level **build.gradle** files. This should be unique redirect uri for your app.

   Example: If your redirect url is `frollo-sdk-example://authorize`, then you would do as below
   ```
   defaultConfig {
       //..
       manifestPlaceholders = ['appAuthRedirectScheme': 'frollo-sdk-example']
       //..
   }
   ```
   For more details see **Integration Requirements** under **OAuth2 Authentication using Authorization Code**
7. Frollo SDK disables auto-backup by default to ensure no data persists between installs. You might run into conflicts during integration if your app has defined **android:allowBackup="true"** in its manifest. Either you can disable auto-backup for your app or override by adding **tools:replace="android:allowBackup"** to **`<application>`** element in your **AndroidManifest.xml**.
8. If you have enabled progaurd please add below lines to your progaurd rules file
   ```
   # KEEP FROM OBFUCATION - Frollo SDK
   -keep class us.frollo.frollosdk.** {*;}
   -keepclassmembers  class us.frollo.frollosdk.** {*;}
   ```
9. Build!