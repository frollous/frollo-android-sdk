package us.frollo.frollosdk.error

class LoginFormError(val type: LoginFormErrorType, val fieldName: String) : FrolloSDKError() {

    var additionalError: String? = null

    override val localizedDescription: String
        get() {
            val description = type.toLocalizedString(context, fieldName)
            additionalError?.let { description.plus(" $it") }
            return description
        }

    override val debugDescription: String
        get() = "LoginFormError: ${ type.name }: $localizedDescription"
}