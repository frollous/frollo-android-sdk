package us.frollo.frollosdk.core

import us.frollo.frollosdk.error.LoginFormError

/**
 * Frollo SDK Completion Handler with success state or error state if an issue occurs
 */
typealias OnFrolloSDKCompletionListener<T> = (T) -> Unit

/**
 * Frollo SDK Validation Completion Handler with optional error if validation fails
 */
typealias FormValidationCompletionListener = (valid: Boolean, error: LoginFormError?) -> Unit