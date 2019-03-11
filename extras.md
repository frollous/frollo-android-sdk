## Basic Usage

### SDK Setup

Import the FrolloSDK and ensure you run setup with your tenant URL provided by us. Do not attempt to use any APIs before the setup completion handler returns.

```kotlin
    class StartupActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // ...

            val configuration = FrolloSDKConfiguration(
                                      clientId = "<APPLICATION_CLIENT_ID>",
                                      redirectUri = "<REDIRECT_URI>",
                                      authorizationUrl = "https://id.frollo.us/oauth/authorize",
                                      tokenUrl = "https://id.frollo.us/oauth/token",
                                      serverUrl = "https://<API_TENANT>.frollo.us/api/v2/")

            FrolloSDK.setup(application, configuration = configuration) { result ->
                when (result.status) {
                    Result.Status.SUCCESS -> completeStartup()
                    Result.Status.ERROR -> Log.e(TAG, result.error?.localizedDescription)
                }
            }
        }
    }
```

### Authentication

Before any data can be refreshed for a user they must be authenticated first. You can check the logged in status of the user on the [Authentication](us.frollo.frollosdk.auth/-authentication/index.html) class.

```kotlin
    if (FrolloSDK.authentication.loggedIn) {
        showMainActivity()
    } else {
        showLoginActivity()
    }
```

#### OAuth2 Authentication using ROPC
If the user is not authenticated the [loginUser](us.frollo.frollosdk.auth/-authentication/login-user.html) API should be called with the user's credentials.

```kotlin
    FrolloSDK.authentication.loginUser(email = "jacob@example.com", password = "$uPer5ecr@t") { result ->
        when (result.status) {
            Result.Status.ERROR -> displayError(result.error?.localizedDescription, "Login Failed")
            Result.Status.SUCCESS -> showMainActivity()
        }
    }
```

#### OAuth2 Authentication using Authorization Code

##### Integration Requirements

If you are going to authenticate using Authorization Code, some extra setup steps are required for your app.

- Gradle version must be 5.1.1+

    Modify Gradle version in your **gradle_wrapper.properties** as below

        `distributionUrl=https\://services.gradle.org/distributions/gradle-5.1.1-all.zip`

- Jetifier version must be 1.0.0-beta04+

    Add below line in your project level **build.gradle** dependencies section
    ```
        dependencies {
            //..
            classpath "com.android.tools.build.jetifier:jetifier-processor:1.0.0-beta04"
            //..
        }
    ```

- You need to define a **appAuthRedirectScheme** in your module level **build.gradle**. This should be unique redirect uri for your app.

    Example:

    ```
        defaultConfig {
            //..
            manifestPlaceholders = [
                'appAuthRedirectScheme': 'frollo-sdk-example://authorize'
            ]
            //..
        }
    ```

##### Method 1 - Using Pending Intents

Completion intent and Cancelled intent should be provided to the SDK to support web based OAuth2 login and other links that can affect application behaviour.

```kotlin
    class LoginActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            //...
            startAuthorizationCodeFlow()
        }

        private fun startAuthorizationCodeFlow() {
            val completionIntent = Intent(this, CompletionLoginWebActivity::class.java)

            val cancelIntent = Intent(this, LoginActivity::class.java)
            cancelIntent.putExtra(EXTRA_FAILED, true)
            cancelIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            FrolloSDK.authentication.loginUserUsingWeb(
                    activity = this,
                    completedIntent = PendingIntent.getActivity(this, 0, completionIntent, 0),
                    cancelledIntent = PendingIntent.getActivity(this, 0, cancelIntent, 0),
                    toolBarColor = resources.getColor(R.color.colorPrimary, null))
        }
    }

    class CompletionLoginWebActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            //...
            FrolloSDK.authentication.handleWebLoginResponse(intent) { result ->
                when (result.status) {
                    Result.Status.SUCCESS -> {
                        startActivity<MainActivity>()
                        finish()
                    }

                    Result.Status.ERROR -> displayError(result.error?.localizedDescription, "Login Failed")
                }
            }
        }
    }
```

##### Method 2 - Using onActivityResult Callback

```kotlin
    class LoginActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            //...
            startAuthorizationCodeFlow()
        }

        private fun startAuthorizationCodeFlow() {
            FrolloSDK.authentication.loginUserUsingWeb(
                    activity = this,
                    toolBarColor = resources.getColor(R.color.colorPrimary, null))
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == Authentication.RC_AUTH) {
                if (resultCode == RESULT_CANCELED) {
                    displayAuthCancelled();
                } else {
                    FrolloSDK.authentication.handleWebLoginResponse(intent) { result ->
                        when (result.status) {
                            Result.Status.SUCCESS -> {
                                startActivity<MainActivity>()
                                finish()
                            }

                            Result.Status.ERROR -> displayError(result.error?.localizedDescription, "Login Failed")
                        }
                    }
                }
            }
        }
    }
```

#### Refreshing Data

After logging in, your cache will be empty in the SDK. Refresh important data such as [Messages](us.frollo.frollosdk.messages/-messages/index.html) immediately after login.

```kotlin
    FrolloSDK.messages.refreshUnreadMessages { result ->
        when (result.status) {
            Result.Status.ERROR -> displayError(result.error?.localizedDescription, "Refreshing Messages Failed")
            Result.Status.SUCCESS -> Log.d("Accounts Refreshed")
        }
    }
```

Alternatively refresh data on startup in an optimized way using [refreshData](us.frollo.frollosdk/-frollo-s-d-k/refresh-data.html) on the main SDK. This will refresh important user data first, delaying less important ones until later.

```kotlin
    FrolloSDK.refreshData()
```

#### Retrieving Cached Data

Fetching objects from the cache store is easy. Just call the SDK fetch APIs and observe the returned LiveData.

```kotlin
    FrolloSDK.messages.fetchMessages(read = false).observe(owner, Observer<Resource<List<Message>>> { resource ->
        when (resource?.status) {
            Resource.Status.SUCCESS -> loadMessages(resource.data)
            Resource.Status.ERROR -> displayError(result.error?.localizedDescription, "Fetching Messages Failed")
        }
    })
```

### Lifecyle Handlers (Optional)

Optionally implement the lifecycle handlers by extending Application class to ensure FrolloSDK can keep cached data fresh when suspending and resuming the app.

```kotlin
    class MyApplication : Application(), LifecycleObserver {

        override fun onCreate() {
            super.onCreate()

            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onAppBackgrounded() {

            FrolloSDK.onAppBackgrounded()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onAppForegrounded() {

            if (FrolloSDK.authentication.loggedIn)
                FrolloSDK.onAppForegrounded()
        }
    }
```

## Push Notifications

### Setup

Follow the steps [here](https://firebase.google.com/docs/android/setup) and [here](https://firebase.google.com/docs/cloud-messaging/android/client) to setup Firebase client for Android.
- Create project in Firebase.
- Copy Server Key & Sender ID. ***// May not be required***
- Create app in Firebase.
- Copy the google_services.json file to your app.
- Add dependencies to your gradle files.
- Add Firebase components to your app's manifest file.

### Registering for Notifications

- Register for push notifications at an appropriate point in the onboarding journey, for example after login/registration and at every app launch to register the device token for notifications.

```kotlin
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result?.token
                    token?.let { FrolloSDK.notifications.registerPushNotificationToken(it) }
                })
```

- Also register the new token in your FirebaseMessagingService instance inside onNewToken() method.

```kotlin
            class MyFirebaseMessagingService : FirebaseMessagingService() {
                override fun onNewToken(token: String?) {
                    token?.let { FrolloSDK.notifications.registerPushNotificationToken(it) }
                }
            }
```

### Handling Notifications and Events

- In your FirebaseMessagingService instance inside onMessageReceived() method, pass the info received from the remote notification to the SDK by implementing the following method.

```kotlin
            class MyFirebaseMessagingService : FirebaseMessagingService() {
                override fun onMessageReceived(remoteMessage: RemoteMessage?) {
                    remoteMessage?.data?.let { data ->
                        if (data.isNotEmpty()) {
                            FrolloSDK.notifications.handlePushNotification(data)
                        }
                    }
                }
            }
```

- Also in your launcher activity implement below method in onCreate after SDK setup.

```kotlin
            intent.extras?.let {
                FrolloSDK.notifications.handlePushNotification(it)
            }
```

 
## Reference
