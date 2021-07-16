/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.error

import android.app.Application
import androidx.annotation.RawRes
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.model.api.shared.APIErrorCode
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson

class APIErrorTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.context = app
    }

    @Test
    fun testAPIErrorType() {
        var apiError = APIError(401, "")
        assertEquals(APIErrorType.OTHER_AUTHORISATION, apiError.type)

        apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        assertEquals(APIErrorType.INVALID_ACCESS_TOKEN, apiError.type)
    }

    @Test
    fun testErrorCode() {
        var apiError = APIError(401, "")
        assertNull(apiError.errorCode)

        apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        assertEquals(APIErrorCode.INVALID_ACCESS_TOKEN, apiError.errorCode)
    }

    @Test
    fun testAPIErrorMessage() {
        var apiError = APIError(401, null)
        assertNull(apiError.message)

        apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        assertEquals("Invalid access token", apiError.message)
    }

    @Test
    fun testLocalizedDescription() {
        var apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        assertEquals(
            app.resources.getString(APIErrorType.INVALID_ACCESS_TOKEN.textResource).plus("\n\nF0101 Invalid access token"),
            apiError.localizedDescription
        )

        apiError = APIError(401, "")
        assertEquals(app.resources.getString(APIErrorType.OTHER_AUTHORISATION.textResource), apiError.localizedDescription)
    }

    @Test
    fun testDebugDescription() {
        var apiError = APIError(401, "{\"error\":{\"error_code\":\"F0101\",\"error_message\":\"Invalid access token\"}}")
        var localizedDescription = app.resources.getString(APIErrorType.INVALID_ACCESS_TOKEN.textResource).plus("\n\nF0101 Invalid access token")
        var str = "APIError: Type [INVALID_ACCESS_TOKEN] HTTP Status Code: 401 F0101: Invalid access token | $localizedDescription"
        assertEquals(str, apiError.debugDescription)

        apiError = APIError(401, "")
        localizedDescription = app.resources.getString(APIErrorType.OTHER_AUTHORISATION.textResource)
        str = "APIError: Type [OTHER_AUTHORISATION] HTTP Status Code: 401  | $localizedDescription"
        assertEquals(str, apiError.debugDescription)
    }

    @Test
    fun testStatusCode() {
        val apiError = APIError(401, "")
        assertEquals(401, apiError.statusCode)
    }

    @Test
    fun testAPIErrorInvalidValue() {
        val errorResponse = readStringFromJson(app, R.raw.error_invalid_value)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.BAD_REQUEST.textResource).plus("\n\nF0001 Invalid value for ID"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.BAD_REQUEST, error.type)
        assertEquals(APIErrorCode.INVALID_VALUE, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorInvalidLength() {
        val errorResponse = readStringFromJson(app, R.raw.error_invalid_length)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.BAD_REQUEST.textResource).plus("\n\nF0002 Invalid length for ID"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.BAD_REQUEST, error.type)
        assertEquals(APIErrorCode.INVALID_LENGTH, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorInvalidAuthHeader() {
        val errorResponse = readStringFromJson(app, R.raw.error_invalid_auth_head)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.BAD_REQUEST.textResource).plus("\n\nF0003 Invalid Authorisation header"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.BAD_REQUEST, error.type)
        assertEquals(APIErrorCode.INVALID_AUTHORISATION_HEADER, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorInvalidUserAgent() {
        val errorResponse = readStringFromJson(app, R.raw.error_invalid_user_agent)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.BAD_REQUEST.textResource).plus("\n\nF0004 Invalid User-Agent header"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.BAD_REQUEST, error.type)
        assertEquals(APIErrorCode.INVALID_USER_AGENT_HEADER, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorValueMustDiffer() {
        val errorResponse = readStringFromJson(app, R.raw.error_value_must_differ)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.PASSWORD_MUST_BE_DIFFERENT.textResource).plus("\n\nF0005 ID must be different"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.PASSWORD_MUST_BE_DIFFERENT, error.type)
        assertEquals(APIErrorCode.INVALID_MUST_BE_DIFFERENT, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorMigration() {
        val errorResponse = readStringFromJson(app, R.raw.error_migration)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.MIGRATION_FAILED.textResource).plus("\n\nF0012 There was an error while migrating the user to Auth0. Please check the logs."), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.MIGRATION_FAILED, error.type)
        assertEquals(APIErrorCode.MIGRATION_FAILED, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorAggregatorBadRequest() {
        val errorResponse = readStringFromJson(app, R.raw.error_aggregator_bad_request)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.AGGREGATOR_BAD_REQUEST.textResource).plus("\n\nF0014 Maximum threshold for the day has been reached. Please try later"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.AGGREGATOR_BAD_REQUEST, error.type)
        assertEquals(APIErrorCode.AGGREGATOR_BAD_REQUEST, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorValueOverLimit() {
        val errorResponse = readStringFromJson(app, R.raw.error_value_over_limit)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.BAD_REQUEST.textResource).plus("\n\nF0006 ID over limit"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.BAD_REQUEST, error.type)
        assertEquals(APIErrorCode.INVALID_OVER_LIMIT, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorInvalidCount() {
        val errorResponse = readStringFromJson(app, R.raw.error_invalid_count)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.BAD_REQUEST.textResource).plus("\n\nF0007 Invalid count for ID"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.BAD_REQUEST, error.type)
        assertEquals(APIErrorCode.INVALID_COUNT, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorInvalidAccessToken() {
        val errorResponse = readStringFromJson(app, R.raw.error_invalid_access_token)

        val error = APIError(401, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.INVALID_ACCESS_TOKEN.textResource).plus("\n\nF0101 Invalid access token"), error.localizedDescription)
        assertEquals(401, error.statusCode)
        assertEquals(APIErrorType.INVALID_ACCESS_TOKEN, error.type)
        assertEquals(APIErrorCode.INVALID_ACCESS_TOKEN, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorInvalidRefreshToken() {
        val errorResponse = readStringFromJson(app, R.raw.error_invalid_refresh_token)

        val error = APIError(401, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.INVALID_REFRESH_TOKEN.textResource).plus("\n\nF0110 Invalid refresh token"), error.localizedDescription)
        assertEquals(401, error.statusCode)
        assertEquals(APIErrorType.INVALID_REFRESH_TOKEN, error.type)
        assertEquals(APIErrorCode.INVALID_REFRESH_TOKEN, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorInvalidUsernamePasswordToken() {
        val errorResponse = readStringFromJson(app, R.raw.error_invalid_username_password)

        val error = APIError(401, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.INVALID_USERNAME_PASSWORD.textResource).plus("\n\nF0111 Invalid username or password"), error.localizedDescription)
        assertEquals(401, error.statusCode)
        assertEquals(APIErrorType.INVALID_USERNAME_PASSWORD, error.type)
        assertEquals(APIErrorCode.INVALID_USERNAME_PASSWORD, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorSuspendedDevice() {
        val errorResponse = readStringFromJson(app, R.raw.error_suspended_device)

        val error = APIError(401, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.SUSPENDED_DEVICE.textResource).plus("\n\nF0113 Suspended device"), error.localizedDescription)
        assertEquals(401, error.statusCode)
        assertEquals(APIErrorType.SUSPENDED_DEVICE, error.type)
        assertEquals(APIErrorCode.SUSPENDED_DEVICE, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorSuspendedUser() {
        val errorResponse = readStringFromJson(app, R.raw.error_suspended_user)

        val error = APIError(401, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.SUSPENDED_USER.textResource).plus("\n\nF0112 Suspended user"), error.localizedDescription)
        assertEquals(401, error.statusCode)
        assertEquals(APIErrorType.SUSPENDED_USER, error.type)
        assertEquals(APIErrorCode.SUSPENDED_USER, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorAccountLocked() {
        val errorResponse = readStringFromJson(app, R.raw.error_account_locked)

        val error = APIError(401, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.ACCOUNT_LOCKED.textResource).plus("\n\nF0114 Account Locked"), error.localizedDescription)
        assertEquals(401, error.statusCode)
        assertEquals(APIErrorType.ACCOUNT_LOCKED, error.type)
        assertEquals(APIErrorCode.ACCOUNT_LOCKED, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorNotAuthorised() {
        val errorResponse = readStringFromJson(app, R.raw.error_not_allowed)

        val error = APIError(403, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.UNAUTHORISED.textResource).plus("\n\nF0200 ID not allowed"), error.localizedDescription)
        assertEquals(403, error.statusCode)
        assertEquals(APIErrorType.UNAUTHORISED, error.type)
        assertEquals(APIErrorCode.UNAUTHORISED, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorNotFound() {
        val errorResponse = readStringFromJson(app, R.raw.error_not_found)

        val error = APIError(404, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.NOT_FOUND.textResource).plus("\n\nF0300 ID not found"), error.localizedDescription)
        assertEquals(404, error.statusCode)
        assertEquals(APIErrorType.NOT_FOUND, error.type)
        assertEquals(APIErrorCode.NOT_FOUND, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorConflict() {
        val errorResponse = readStringFromJson(app, R.raw.error_duplicate)

        val error = APIError(409, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.ALREADY_EXISTS.textResource).plus("\n\nF0400 ID already exists"), error.localizedDescription)
        assertEquals(409, error.statusCode)
        assertEquals(APIErrorType.ALREADY_EXISTS, error.type)
        assertEquals(APIErrorCode.ALREADY_EXISTS, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorAggregatorError() {
        val errorResponse = readStringFromJson(app, R.raw.error_aggregator)

        val error = APIError(503, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.SERVER_ERROR.textResource).plus("\n\nF9000 Aggregator error"), error.localizedDescription)
        assertEquals(503, error.statusCode)
        assertEquals(APIErrorType.SERVER_ERROR, error.type)
        assertEquals(APIErrorCode.AGGREGATOR_ERROR, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorServerError() {
        val errorResponse = readStringFromJson(app, R.raw.error_server)

        val error = APIError(504, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.SERVER_ERROR.textResource).plus("\n\nF9998 Something error"), error.localizedDescription)
        assertEquals(504, error.statusCode)
        assertEquals(APIErrorType.SERVER_ERROR, error.type)
        assertEquals(APIErrorCode.UNKNOWN_SERVER, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorInternalException() {
        val errorResponse = readStringFromJson(app, R.raw.error_internal_exception)

        val error = APIError(500, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.SERVER_ERROR.textResource).plus("\n\nF9999 Internal exception"), error.localizedDescription)
        assertEquals(500, error.statusCode)
        assertEquals(APIErrorType.SERVER_ERROR, error.type)
        assertEquals(APIErrorCode.INTERNAL_EXCEPTION, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorUnknownAuth() {
        val error = APIError(401, null)
        assertEquals(app.resources.getString(APIErrorType.OTHER_AUTHORISATION.textResource), error.localizedDescription)
        assertEquals(401, error.statusCode)
        assertEquals(APIErrorType.OTHER_AUTHORISATION, error.type)
        assertNull(error.errorCode)
        assertNull(error.message)
    }

    @Test
    fun testAPIErrorMaintenance() {
        val error = APIError(502, null)
        assertEquals(app.resources.getString(APIErrorType.MAINTENANCE.textResource), error.localizedDescription)
        assertEquals(502, error.statusCode)
        assertEquals(APIErrorType.MAINTENANCE, error.type)
        assertNull(error.errorCode)
        assertNull(error.message)
    }

    @Test
    fun testAPIErrorNotImplemented() {
        val error = APIError(501, null)
        assertEquals(app.resources.getString(APIErrorType.NOT_IMPLEMENTED.textResource), error.localizedDescription)
        assertEquals(501, error.statusCode)
        assertEquals(APIErrorType.NOT_IMPLEMENTED, error.type)
        assertNull(error.errorCode)
        assertNull(error.message)
    }

    @Test
    fun testAPIErrorRateLimited() {
        val error = APIError(429, null)
        assertEquals(app.resources.getString(APIErrorType.RATE_LIMIT.textResource), error.localizedDescription)
        assertEquals(429, error.statusCode)
        assertEquals(APIErrorType.RATE_LIMIT, error.type)
        assertNull(error.errorCode)
        assertNull(error.message)
    }

    @Test
    fun testAPIErrorDeprecated() {
        val error = APIError(410, null)
        assertEquals(app.resources.getString(APIErrorType.DEPRECATED.textResource), error.localizedDescription)
        assertEquals(410, error.statusCode)
        assertEquals(APIErrorType.DEPRECATED, error.type)
        assertNull(error.errorCode)
        assertNull(error.message)
    }

    @Test
    fun testAPIErrorBadFormat() {
        val errorResponse = readStringFromJson(app, R.raw.error_bad_format)

        val error = APIError(302, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.UNKNOWN.textResource).plus("\n\nF9999 "), error.localizedDescription)
        assertEquals(302, error.statusCode)
        assertEquals(APIErrorType.UNKNOWN, error.type)
        assertEquals(APIErrorCode.INTERNAL_EXCEPTION, error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorUnknownCode() {
        val errorResponse = readStringFromJson(app, R.raw.error_unknown_code)

        val error = APIError(302, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.UNKNOWN.textResource).plus("\n\nH2134 Some weird error"), error.localizedDescription)
        assertEquals(302, error.statusCode)
        assertEquals(APIErrorType.UNKNOWN, error.type)
        assertNull(error.errorCode)
        assertNotNull(error.message)
    }

    @Test
    fun testAPIErrorMissingCode() {
        val errorResponse = readStringFromJson(app, R.raw.error_missing_code)

        val error = APIError(302, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.UNKNOWN.textResource).plus("\n\nSome unknown error"), error.localizedDescription)
        assertEquals(302, error.statusCode)
        assertEquals(APIErrorType.UNKNOWN, error.type)
        assertNull(error.errorCode)
        assertNotNull(error.message)
    }

    // Payment Errors

    @Test
    fun testAPIErrorPaymentAccountRestricted() {
        validatePaymentErrors(R.raw.error_payment_account_restricted, APIErrorCode.PAYMENT_ACCOUNT_RESTRICTED)
    }

    @Test
    fun testAPIErrorPaymentInvalidAmount() {
        validatePaymentErrors(R.raw.error_payment_invalid_amount, APIErrorCode.PAYMENT_INVALID_AMOUNT)
    }

    @Test
    fun testAPIErrorPaymentCharacterLimitExceeded() {
        validatePaymentErrors(R.raw.error_payment_character_limit_exceeded, APIErrorCode.PAYMENT_CHARACTER_LIMIT_EXCEEDED)
    }

    @Test
    fun testAPIErrorPaymentInsufficientFunds() {
        validatePaymentErrors(R.raw.error_payment_insufficient_funds, APIErrorCode.PAYMENT_INSUFFICIENT_FUNDS)
    }

    @Test
    fun testAPIErrorPaymentInvalidAccount() {
        validatePaymentErrors(R.raw.error_payment_invalid_account, APIErrorCode.PAYMENT_INVALID_ACCOUNT)
    }

    @Test
    fun testAPIErrorPaymentInvalidBillerCode() {
        validatePaymentErrors(R.raw.error_payment_invalid_biller_code, APIErrorCode.PAYMENT_INVALID_BILLER_CODE)
    }

    @Test
    fun testAPIErrorPaymentInvalidBpay() {
        validatePaymentErrors(R.raw.error_payment_invalid_bpay, APIErrorCode.PAYMENT_INVALID_BPAY)
    }

    @Test
    fun testAPIErrorPaymentInvalidBSB() {
        validatePaymentErrors(R.raw.error_payment_invalid_bsb, APIErrorCode.PAYMENT_INVALID_BSB)
    }

    @Test
    fun testAPIErrorPaymentInvalidCRN() {
        validatePaymentErrors(R.raw.error_payment_invalid_crn, APIErrorCode.PAYMENT_INVALID_CRN)
    }

    @Test
    fun testAPIErrorPaymentInvalidDate() {
        validatePaymentErrors(R.raw.error_payment_invalid_date, APIErrorCode.PAYMENT_INVALID_DATE)
    }

    @Test
    fun testAPIErrorPaymentInvalidDestinationAccount() {
        validatePaymentErrors(R.raw.error_payment_invalid_destination_account, APIErrorCode.PAYMENT_INVALID_DESTINATION_ACCOUNT)
    }

    @Test
    fun testAPIErrorPaymentInvalidPayAnyone() {
        validatePaymentErrors(R.raw.error_payment_invalid_pay_anyone, APIErrorCode.PAYMENT_INVALID_PAY_ANYONE)
    }

    @Test
    fun testAPIErrorPaymentInvalidSourceAccount() {
        validatePaymentErrors(R.raw.error_payment_invalid_source_account, APIErrorCode.PAYMENT_INVALID_SOURCE_ACCOUNT)
    }

    @Test
    fun testAPIErrorPaymentInvalidTransfer() {
        validatePaymentErrors(R.raw.error_payment_invalid_transfer, APIErrorCode.PAYMENT_INVALID_TRANSFER)
    }

    @Test
    fun testAPIErrorPaymentOther() {
        validatePaymentErrors(R.raw.error_payment_other, APIErrorCode.PAYMENT_OTHER_ERROR)
    }

    @Test
    fun testAPIErrorPaymentProcessorConnectivity() {
        validatePaymentErrors(R.raw.error_payment_processor_connectivity, APIErrorCode.PAYMENT_PROCESSOR_CONNECTIVITY_ERROR)
    }

    @Test
    fun testAPIErrorPaymentOTPMissing() {
        validatePaymentErrors(R.raw.error_payment_missing_otp, APIErrorCode.MISSING_OTP)
    }

    @Test
    fun testAPIErrorPaymentInvalidOTP() {
        validatePaymentErrors(R.raw.error_payment_invalid_otp, APIErrorCode.INVALID_OTP)
    }

    @Test
    fun testAPIErrorPaymentProcessor() {
        validatePaymentErrors(R.raw.error_payment_processor, APIErrorCode.PAYMENT_PROCESSOR_ERROR)
    }

    private fun validatePaymentErrors(@RawRes rawJson: Int, errorCode: APIErrorCode) {
        val errorResponse = readStringFromJson(app, rawJson)

        val error = APIError(400, errorResponse)
        assertEquals(app.resources.getString(APIErrorType.BAD_REQUEST.textResource).plus("\n\n${error.errorCode?.rawValue} ${error.message}"), error.localizedDescription)
        assertEquals(400, error.statusCode)
        assertEquals(APIErrorType.BAD_REQUEST, error.type)
        assertEquals(errorCode, error.errorCode)
        assertNotNull(error.message)
    }
}
