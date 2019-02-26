package us.frollo.frollosdk.core

internal fun testSDKConfig(serverUrl: String? = null) =
        FrolloSDKConfiguration(
                clientId = "abc123",
                redirectUri = "app://redirect",
                authorizationUrl = "https://id.example.com/oauth/authorize",
                tokenUrl = "https://id.example.com/oauth/token",
                serverUrl = serverUrl ?: "https://api.example.com")
