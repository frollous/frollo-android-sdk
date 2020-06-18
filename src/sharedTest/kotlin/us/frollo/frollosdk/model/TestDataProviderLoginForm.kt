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

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFieldType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFieldValidation
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormField
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormRow
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm

internal fun loginFormUsernameRow(): ProviderFormRow {
    val field = ProviderFormField(
        fieldId = "5092",
        image = null,
        isOptional = false,
        maxLength = null,
        name = "LOGIN",
        options = null,
        prefix = null,
        suffix = null,
        type = ProviderFieldType.TEXT,
        validations = null,
        value = "",
        valueEditable = true
    )

    return ProviderFormRow(
        fields = listOf(field),
        fieldRowChoice = "0001",
        form = "0001",
        hint = null,
        rowId = "7331",
        label = "User ID"
    )
}

internal fun loginFormPasswordRow(): ProviderFormRow {
    val field = ProviderFormField(
        fieldId = "5091",
        image = null,
        isOptional = false,
        maxLength = null,
        name = "PASSWORD",
        options = null,
        prefix = null,
        suffix = null,
        type = ProviderFieldType.PASSWORD,
        validations = null,
        value = "",
        valueEditable = true
    )

    return ProviderFormRow(
        fields = listOf(field),
        fieldRowChoice = "0002",
        form = "0001",
        hint = null,
        rowId = "7330",
        label = "Password"
    )
}

internal fun loginFormMaxLengthRow(): ProviderFormRow {
    val field = ProviderFormField(
        fieldId = "7224",
        image = null,
        isOptional = false,
        maxLength = 12,
        name = "MEMBER_NO",
        options = null,
        prefix = null,
        suffix = null,
        type = ProviderFieldType.TEXT,
        validations = null,
        value = "",
        valueEditable = true
    )

    return ProviderFormRow(
        fields = listOf(field),
        fieldRowChoice = "0002",
        form = "0001",
        hint = null,
        rowId = "7330",
        label = "Member Number"
    )
}

internal fun loginFormMultipleChoiceRow(choice: String): ProviderFormRow {
    val field = ProviderFormField(
        fieldId = "65773",
        image = null,
        isOptional = true,
        maxLength = 12,
        name = "OP_LOGIN1",
        options = null,
        prefix = null,
        suffix = null,
        type = ProviderFieldType.TEXT,
        validations = null,
        value = "",
        valueEditable = true
    )

    return ProviderFormRow(
        fields = listOf(field),
        fieldRowChoice = choice,
        form = "0001",
        hint = null,
        rowId = "151124",
        label = "An Option"
    )
}

internal fun loginFormMultipleChoiceRows(): List<ProviderFormRow> {
    return listOf(
        loginFormMultipleChoiceRow(choice = "0002 Choice"),
        loginFormMultipleChoiceRow(choice = "0002 Choice"),
        loginFormMultipleChoiceRow(choice = "0002 Choice")
    )
}

internal fun loginFormValidationField(): ProviderFormRow {
    val field = ProviderFormField(
        fieldId = "65773",
        image = null,
        isOptional = true,
        maxLength = null,
        name = "PASSWORD",
        options = null,
        prefix = null,
        suffix = null,
        type = ProviderFieldType.TEXT,
        validations = listOf(loginFormRegexValidation()),
        value = "",
        valueEditable = true
    )

    return ProviderFormRow(
        fields = listOf(field),
        fieldRowChoice = "0001",
        form = "0001",
        hint = null,
        rowId = "151124",
        label = "Password"
    )
}

internal fun loginFormRegexValidation(): ProviderFieldValidation {
    return ProviderFieldValidation(errorMsg = "Please enter a valid Access Code", regExp = "^[0-9]{0,6}$")
}

internal fun loginFormFilledData(formType: ProviderFormType? = null): ProviderLoginForm {
    val usernameRow = loginFormUsernameRow()
    usernameRow.fields[0].value = "abc123"

    val passwordRow = loginFormPasswordRow()
    passwordRow.fields[0].value = "password"

    return ProviderLoginForm(
        formId = "3410",
        forgetPasswordUrl = "https://secure.amp.com.au/wps/portal/sec/ForgotUsername/!ut/p/a1/04_Sj9CPykssy0xPLMnMz0vMAfGjzOIDDC1cPUzcDbwNLANcDBxdg009vfz9jQxcTfW99KPSc_KTgEoj9SPxKy3IDnIEAM_vx8Q!/",
        formType = formType ?: ProviderFormType.LOGIN,
        help = null,
        mfaInfoText = null,
        mfaTimeout = null,
        mfaInfoTitle = null,
        rows = listOf(usernameRow, passwordRow)
    )
}

internal fun loginFormFilledMissingRequiredField(): ProviderLoginForm {
    val usernameRow = loginFormUsernameRow()
    usernameRow.fields[0].value = ""

    val passwordRow = loginFormPasswordRow()

    return ProviderLoginForm(
        formId = "3410",
        forgetPasswordUrl = "https://secure.amp.com.au/wps/portal/sec/ForgotUsername/!ut/p/a1/04_Sj9CPykssy0xPLMnMz0vMAfGjzOIDDC1cPUzcDbwNLANcDBxdg009vfz9jQxcTfW99KPSc_KTgEoj9SPxKy3IDnIEAM_vx8Q!/",
        formType = ProviderFormType.LOGIN,
        help = null,
        mfaInfoText = null,
        mfaTimeout = null,
        mfaInfoTitle = null,
        rows = listOf(usernameRow, passwordRow)
    )
}

internal fun loginFormFilledInvalidMultipleChoiceField(): ProviderLoginForm {
    val usernameRow = loginFormUsernameRow()
    usernameRow.fields[0].value = "abc123"

    val multipleChoiceRows = loginFormMultipleChoiceRows()

    return ProviderLoginForm(
        formId = "3410",
        forgetPasswordUrl = "https://secure.amp.com.au/wps/portal/sec/ForgotUsername/!ut/p/a1/04_Sj9CPykssy0xPLMnMz0vMAfGjzOIDDC1cPUzcDbwNLANcDBxdg009vfz9jQxcTfW99KPSc_KTgEoj9SPxKy3IDnIEAM_vx8Q!/",
        formType = ProviderFormType.LOGIN,
        help = null,
        mfaInfoText = null,
        mfaTimeout = null,
        mfaInfoTitle = null,
        rows = listOf(usernameRow).plus(multipleChoiceRows)
    )
}

internal fun loginFormMultipleChoiceFields(): ProviderLoginForm {
    val usernameRow = loginFormUsernameRow()

    val multipleChoiceRows = loginFormMultipleChoiceRows()

    return ProviderLoginForm(
        formId = "3410",
        forgetPasswordUrl = "https://secure.amp.com.au/wps/portal/sec/ForgotUsername/!ut/p/a1/04_Sj9CPykssy0xPLMnMz0vMAfGjzOIDDC1cPUzcDbwNLANcDBxdg009vfz9jQxcTfW99KPSc_KTgEoj9SPxKy3IDnIEAM_vx8Q!/",
        formType = ProviderFormType.LOGIN,
        help = null,
        mfaInfoText = null,
        mfaTimeout = null,
        mfaInfoTitle = null,
        rows = listOf(usernameRow).plus(multipleChoiceRows)
    )
}

internal fun loginFormFilledMaxLengthExceededField(): ProviderLoginForm {
    val usernameRow = loginFormUsernameRow()
    usernameRow.fields[0].value = "abc123"

    val maxLengthRow = loginFormMaxLengthRow()
    maxLengthRow.fields[0].value = "This string is way too long"

    return ProviderLoginForm(
        formId = "3410",
        forgetPasswordUrl = "https://secure.amp.com.au/wps/portal/sec/ForgotUsername/!ut/p/a1/04_Sj9CPykssy0xPLMnMz0vMAfGjzOIDDC1cPUzcDbwNLANcDBxdg009vfz9jQxcTfW99KPSc_KTgEoj9SPxKy3IDnIEAM_vx8Q!/",
        formType = ProviderFormType.LOGIN,
        help = null,
        mfaInfoText = null,
        mfaTimeout = null,
        mfaInfoTitle = null,
        rows = listOf(usernameRow, maxLengthRow)
    )
}

internal fun loginFormFilledRegexInvalidField(): ProviderLoginForm {
    val regexField = loginFormValidationField()
    regexField.fields[0].value = "Not an access code"

    return ProviderLoginForm(
        formId = "3410",
        forgetPasswordUrl = "https://secure.amp.com.au/wps/portal/sec/ForgotUsername/!ut/p/a1/04_Sj9CPykssy0xPLMnMz0vMAfGjzOIDDC1cPUzcDbwNLANcDBxdg009vfz9jQxcTfW99KPSc_KTgEoj9SPxKy3IDnIEAM_vx8Q!/",
        formType = ProviderFormType.LOGIN,
        help = null,
        mfaInfoText = null,
        mfaTimeout = null,
        mfaInfoTitle = null,
        rows = listOf(regexField)
    )
}

/*
internal fun testMFAData(): ProviderLoginForm {

}

internal fun testCaptchaData(): ProviderLoginForm {

}

internal fun testMultipleChoiceData(): ProviderLoginForm {

}

internal fun testOptionsData(): ProviderLoginForm {

}
*/
