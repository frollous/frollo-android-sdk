package us.frollo.frollosdk.core

internal fun testSDKConfig(serverUrl: String? = null, tokenUrl: String? = null) =
        FrolloSDKConfiguration(
                clientId = "abc123",
                redirectUrl = "app://redirect",
                authorizationUrl = "https://id.example.com/oauth/authorize/",
                tokenUrl = tokenUrl ?: "https://id.example.com/oauth/token/",
                serverUrl = serverUrl ?: "https://api.example.com/")
