package us.frollo.frollosdk.core

import us.frollo.frollosdk.error.FrolloSDKError

/**
 * Frollo SDK Completion Handler with optional error if an issue occurs
 */
typealias OnFrolloSDKCompletionListener = (error: FrolloSDKError?) -> Unit