package us.frollo.frollosdk.core

internal fun testSDKConfig(clientId: String? = null, serverUrl: String? = null,
                           tokenUrl: String? = null, authorizationUrl: String? = null,
                           redirectUrl: String? = null) =
        FrolloSDKConfiguration(
                clientId = clientId ?: "abc123",
                redirectUrl = redirectUrl ?: "app://redirect",
                authorizationUrl = authorizationUrl ?: "https://id.example.com/oauth/authorize/",
                tokenUrl = tokenUrl ?: "https://id.example.com/oauth/token/",
                serverUrl = serverUrl ?: "https://api.example.com/")
