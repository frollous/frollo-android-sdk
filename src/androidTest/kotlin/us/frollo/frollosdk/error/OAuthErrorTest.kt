package us.frollo.frollosdk.error

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import net.openid.appauth.AuthorizationException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK

class OAuthErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testAccessDeniedError() {
        val exception = AuthorizationException.AuthorizationRequestErrors.ACCESS_DENIED
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.ACCESS_DENIED, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.ACCESS_DENIED.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testClientError() {
        val exception = AuthorizationException.AuthorizationRequestErrors.CLIENT_ERROR
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.CLIENT_ERROR, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.CLIENT_ERROR.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testInvalidClientError() {
        val exception = AuthorizationException.TokenRequestErrors.INVALID_CLIENT
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.INVALID_CLIENT, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.INVALID_CLIENT.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testInvalidClientMetadataError() {
        val exception = AuthorizationException.RegistrationRequestErrors.INVALID_CLIENT_METADATA
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.INVALID_CLIENT_METADATA, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.INVALID_CLIENT_METADATA.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testInvalidGrantError() {
        val exception = AuthorizationException.TokenRequestErrors.INVALID_GRANT
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.INVALID_GRANT, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.INVALID_GRANT.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testInvalidRedirectURIError() {
        val exception = AuthorizationException.RegistrationRequestErrors.INVALID_REDIRECT_URI
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.INVALID_REDIRECT_URI, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.INVALID_REDIRECT_URI.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testInvalidRequestError() {
        val exception = AuthorizationException.AuthorizationRequestErrors.INVALID_REQUEST
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.INVALID_REQUEST, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.INVALID_REQUEST.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testInvalidScopeError() {
        val exception = AuthorizationException.AuthorizationRequestErrors.INVALID_SCOPE
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.INVALID_SCOPE, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.INVALID_SCOPE.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testUnauthorizedClientError() {
        val exception = AuthorizationException.AuthorizationRequestErrors.UNAUTHORIZED_CLIENT
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.UNAUTHORIZED_CLIENT, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.UNAUTHORIZED_CLIENT.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testUnsupportedGrantTypeError() {
        val exception = AuthorizationException.TokenRequestErrors.UNSUPPORTED_GRANT_TYPE
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.UNSUPPORTED_GRANT_TYPE, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.UNSUPPORTED_GRANT_TYPE.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testUnsupportedResponseTypeError() {
        val exception = AuthorizationException.AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.UNSUPPORTED_RESPONSE_TYPE, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.UNSUPPORTED_RESPONSE_TYPE.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testNetworkError() {
        val exception = AuthorizationException.GeneralErrors.NETWORK_ERROR
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.NETWORK_ERROR, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.NETWORK_ERROR.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testServerError() {
        val exception = AuthorizationException.AuthorizationRequestErrors.SERVER_ERROR
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.SERVER_ERROR, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.SERVER_ERROR.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testUserCancelledError() {
        val exception = AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.USER_CANCELLED, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.USER_CANCELLED.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }

    @Test
    fun testOtherAuthorisationError() {
        val exception = AuthorizationException.GeneralErrors.INVALID_DISCOVERY_DOCUMENT
        val authError = OAuthError(exception)
        assertEquals(OAuthErrorType.OTHER_AUTHORISATION, authError.type)
        val localizedDescription = app.resources.getString(OAuthErrorType.OTHER_AUTHORISATION.textResource)
        assertEquals(localizedDescription, authError.localizedDescription)
    }
}