## Changelog

### 1.0.0
- Initial release

#### Features
- Events
- Messages
- Push Notifications
- User Authentication

## Getting Started

### Installation

TBD

### Basic Usage

#### SDK Setup

Import the FrolloSDK and ensure you run setup with your tenant URL provided by us. Do not attempt to use any APIs before the setup completion handler returns.

```kotlin
    class StartupActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // ...

            val serverUrl = "https://<tenant>.frollo.us"

            val configuration = FrolloSDKConfiguration(
                                      clientId = "",
                                      clientSecret = "",
                                      redirectUri = "",
                                      authorizationUrl = "",
                                      tokenUrl = "",
                                      serverUrl = serverUrl,
                                      logLevel = LogLevel.DEBUG)

            FrolloSDK.setup(application, configuration = configuration) { error ->
                if (error != null)
                    Log.e(TAG, error.localizedDescription)
                else
                    completeStartup()
            }
        }
    }
```

#### Authentication

Before any data can be refreshed for a user they must be authenticated first. You can check the logged in status of the user on the [Authentication](us.frollo.frollosdk.auth/-authentication/index.html) class.

```kotlin
    if (FrolloSDK.authentication.loggedIn) {
        showMainActivity()
    } else {
        showLoginActivity()
    }
```

If the user is not authenticated the [loginUser](us.frollo.frollosdk.auth/-authentication/login-user.html) API should be called with the user's credentials.

```kotlin
    FrolloSDK.authentication.loginUser(method = AuthType.EMAIL, email = email, password = password) { error ->
        if (error != null) {
            displayError(error.localizedDescription, "Login Failed")
        } else {
            showMainActivity()
        }
    }
```

#### Refreshing Data

After logging in, your cache will be empty in the SDK. Refresh important data such as [Messages](us.frollo.frollosdk.messages/-messages/index.html) immediately after login.

```kotlin
    FrolloSDK.messages.refreshUnreadMessages { error ->
        if (error != null)
            displayError(error.localizedDescription, "Refreshing Messages Failed")
    }
```

Alternatively refresh data on startup in an optimized way using [refreshData](us.frollo.frollosdk/-frollo-s-d-k/refresh-data.html) on the main SDK. This will refresh important user data first, delaying less important ones until later.

```kotlin
    FrolloSDK.refreshData()
```

#### Retrieving Cached Data

Fetching objects from the cache store is easy. Just call the SDK fetch APIs and observe the returned LiveData.

```kotlin
    FrolloSDK.messages.fetchMessages(read = false).observe(owner, Observer<Resource<List<Message>>> {
        when (it?.status) {
            Resource.Status.SUCCESS -> loadMessages(it.data)
            Resource.Status.ERROR -> displayError(it.error?.localizedDescription, "Fetch Messages Failed")
            Resource.Status.LOADING -> Log.d(TAG, "Loading Messages...")
        }
    })
```

#### Lifecyle Handlers (Optional)

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
