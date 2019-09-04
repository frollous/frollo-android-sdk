## Basic Usage

### SDK Setup

Import the FrolloSDK and ensure you run setup with your tenant URL provided by us. Do not attempt to use any APIs before the setup completion handler returns. You will also need to pass in your custom authentication handler or use the default OAuth2 implementation.

```kotlin
    class MyApplication : Application() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // OAuth2 Config
            val configuration = FrolloSDKConfiguration(
                                      authenticationType = OAuth2(
                                              redirectUrl = "<REDIRECT_URI>",
                                              authorizationUrl = "https://id.frollo.us/oauth/authorize",
                                              tokenUrl = "https://id.frollo.us/oauth/token"),
                                      clientId = "<APPLICATION_CLIENT_ID>",
                                      serverUrl = "https://<API_TENANT>.frollo.us/api/v2/")

            // Custom Authentication Config
            val customAuthentication = CustomAuthentication()
            val configuration = FrolloSDKConfiguration(
                                      authenticationType = Custom(
                                              accessTokenProvider = customAuthentication,
                                              authenticationCallback = customAuthentication),
                                      clientId = "<APPLICATION_CLIENT_ID>",
                                      serverUrl = "https://<API_TENANT>.frollo.us/api/v2/")

            FrolloSDK.setup(this, configuration = configuration) { result ->
                when (result.status) {
                    Result.Status.SUCCESS -> completeSetup()
                    Result.Status.ERROR -> Log.e(TAG, result.error?.localizedDescription)
                }
            }
        }
    }
```

### Authentication

Before any data can be refreshed for a user they must be authenticated first. If using OAuth2 authentication you can check the logged in status of the user on the [OAuth2Authentication](us.frollo.frollosdk.authentication/-o-auth2-authentication/index.html) class.

```kotlin
    if (FrolloSDK.oAuth2Authentication.loggedIn) {
        showMainActivity()
    } else {
        showLoginActivity()
    }
```

If the user is not authenticated then the user must login or an access token must be provided by the custom Authentication access token provider. Authentication can be done using OAuth2 or a custom implementation can be provided if you wish to manage the user's access token manually or share it with other APIs.

#### OAuth2 Authentication

Using OAuth2 based authentication Resource Owner Password Credential flow and Authorization Code with PKCE flow are supported. Identity Providers must be OpenID Connect compliant to use the in-built [OAuth2Authentication](us.frollo.frollosdk.authentication/-o-auth2-authentication/index.html) authentication class. If using OAuth2 authentication you can use [oAuth2Authentication](us.frollo.frollosdk/-frollo-s-d-k/o-auth2-authentication.html)

##### ROPC Flow

Using the ROPC flow is the simplest and can be used if you are implementing the SDK in your own highly trusted first party application. All it requires is email and password and can be used in conjunction with a native UI.

See [loginUser(email:password:completion:)](us.frollo.frollosdk.authentication/-o-auth2-authentication/login-user.html)


```kotlin
    FrolloSDK.oAuth2Authentication.loginUser(email = "jacob@example.com", password = "$uPer5ecr@t") { result ->
        when (result.status) {
            Result.Status.ERROR -> displayError(result.error?.localizedDescription, "Login Failed")
            Result.Status.SUCCESS -> completeLogin()
        }
    }
```

#### Authorization Code with PKCE Flow

Authenticating the user using Authorization Code flow involves a couple of extra steps to configure. The first is to present the ChromeTabs to the user to take them through the web based authorization flow. The Activity this should be presented from must be passed to the SDK.

See [loginUserUsingWeb](us.frollo.frollosdk.authentication/-o-auth2-authentication/login-user-using-web.html)

##### Integration Requirements

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
- If you are using a deep link scheme which is same as your `appAuthRedirectScheme`, then add below component to your manifest

    Example: If your redirect url is `frollo-sdk-example://authorize`, and you have defined `appAuthRedirectScheme: 'frollo-sdk-example'` in your gradle file.
    
    ```
        <!-- AppAuth Custom Redirect URI -->
        <activity android:name="net.openid.appauth.RedirectUriReceiverActivity"
            android:exported="true"
            tools:node="replace">
            <intent-filter>
                <action android:name="android.intent.action.VIEW"/>
                <category android:name="android.intent.category.DEFAULT"/>
                <category android:name="android.intent.category.BROWSABLE"/>
                
                <data android:scheme="${appAuthRedirectScheme}" android:host="authorize"/>
            </intent-filter>
        </activity>
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

            FrolloSDK.oAuth2Authentication.loginUserUsingWeb(
                    activity = this,
                    scopes = listOf(OAuth2Scope.OFFLINE_ACCESS, OAuth2Scope.EMAIL, OAuth2Scope.OPENID),
                    completedIntent = PendingIntent.getActivity(this, 0, completionIntent, 0),
                    cancelledIntent = PendingIntent.getActivity(this, 0, cancelIntent, 0),
                    toolBarColor = resources.getColor(R.color.colorPrimary, null))
        }
    }
```

The next step is to pass the intent received by the Completion Activity to the SDK to complete the login process and exchange the authorization code for a token.

```kotlin
    class CompletionLoginWebActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            //...
            FrolloSDK.oAuth2Authentication.handleWebLoginResponse(intent) { result ->
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
                    scopes = listOf(OAuth2Scope.OFFLINE_ACCESS, OAuth2Scope.EMAIL, OAuth2Scope.OPENID),
                    toolBarColor = resources.getColor(R.color.colorPrimary, null))
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == Authentication.RC_AUTH) {
                if (resultCode == RESULT_CANCELED) {
                    displayAuthCancelled();
                } else {
                    // The next step is to pass the intent received to the SDK to complete the login process and exchange the authorization code for a token.
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

#### Custom Authentication

Custom authentication can be provided by conforming to the [AccessTokenProvider](us.frollo.frollosdk.authentication/-access-token-provider/index.html) interface and [AuthenticationCallback](us.frollo.frollosdk.authentication/-authentication-callback/index.html) interface ensuring all interface functions are implemented appropriately.

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
