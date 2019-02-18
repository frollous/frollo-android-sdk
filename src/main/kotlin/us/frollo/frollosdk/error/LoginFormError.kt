package us.frollo.frollosdk.error

/**
 * Error occuring when using the aggregation provider login forms
 */
class LoginFormError(
        /** Login form error type */
        val type: LoginFormErrorType,
        /** Affected field name */
        val fieldName: String) : FrolloSDKError() {

    /** Additional error information */
    var additionalError: String? = null

    /** Localized description */
    override val localizedDescription: String?
        get() {
            var description = type.toLocalizedString(context, fieldName)
            additionalError?.let { description = description.plus(" $it") }
            return description
        }

    /** Debug description */
    override val debugDescription: String?
        get() = "LoginFormError: ${ type.name }: $localizedDescription"
}