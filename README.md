## Getting Started

### Requirements

- Android Studio 3.4+
- Kotlin version 1.3.30+
- Gradle tools plugin version 3.4.1+ - In your project level **build.gradle**    
    ```    
        dependencies {    
            classpath "com.android.tools.build:gradle:3.4.0"    
            //..    
        }    
    ```
- Gradle version must be 5.1.1+

    Modify Gradle version in your **gradle_wrapper.properties** as below

    ```
        distributionUrl=https\://services.gradle.org/distributions/gradle-5.1.1-all.zip
    ```
- You need to define a **appAuthRedirectScheme** in your module level **build.gradle**. This should be unique redirect uri for your app.    

    Example: If your redirect url is `frollo-sdk-example://authorize`, then you would do as below

    ```
        defaultConfig {
            //..
            manifestPlaceholders = [
                'appAuthRedirectScheme': 'frollo-sdk-example'
            ]
            //..
        }
    ```
- **minSdkVersion** in your gradle file must be **23** or above. Frollo SDK does not support Android versions below Marshmallow (6.0).
- Frollo SDK disables auto-backup by default to ensure no data persists between installs. You might run into conflicts during integration if your app has defined **android:allowBackup="true"** in its manifest. Either you can disable auto-backup for your app or override by adding **tools:replace="android:allowBackup"** to **`<application>`** element in your **AndroidManifest.xml**.
- Use AndroidX for your project instead of legacy support libraries. You can either enable "**Use AndroidX**" checkbox while creating a new Android project in Android Studio or migrate your existing project to AndroidX - [Migrating to AndroidX](https://developer.android.com/jetpack/androidx/migrate)

### Integration instructions

To integrate Frollo Android SDK to your Android app use the following steps:

1. Pull the Frollo SDK code base    

      - If you are using GIT version control for your project, add Frollo SDK as a submodule in your project    

        `git submodule add git@bitbucket.org:frollo1/frollo-android-sdk.git`    
    
        `git submodule update --init --recursive`    

      or

      - Clone SDK repo inside your project's root directory    

        `git clone git@bitbucket.org:frollo1/frollo-android-sdk.git`    

    You should see a folder named _frollo-android-sdk_ inside your root project directory and within it, the SDK code.

2. Add _frollo-android-sdk_ module to your **settings.gradle** file

    `include ':app', ':frollo-android-sdk'`

3. Add below line to the dependencies in your **app/build.gradle** file    
    ```    
        dependencies {    
            //..    
            implementation project(":frollo-android-sdk")    
        }    
    ```
4. Build! 👷‍♂️