package us.frollo.frollosdk.core

import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.error.LoginFormError

/**
 * Frollo SDK Completion Handler with optional error if an issue occurs
 */
typealias OnFrolloSDKCompletionListener = (error: FrolloSDKError?) -> Unit

/**
 * Frollo SDK Validation Completion Handler with optional error if validation fails
 */
typealias FormValidationCompletionListener = (valid: Boolean, error: LoginFormError?) -> Unit