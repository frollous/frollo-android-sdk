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

package us.frollo.frollosdk.model.display.aggregation.providers

import us.frollo.frollosdk.core.FormValidationCompletionListener
import us.frollo.frollosdk.error.LoginFormError
import us.frollo.frollosdk.error.LoginFormErrorType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormRow
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm

/**
 * Provides a representation of the login form more suited for display by the app. It collates matching rows which can be selected between together into each [Container].
 *
 * Each [Container] should only show one [ProviderFormRow] at a time.
 */
data class ProviderLoginFormDisplay(
    /** ID of the login form (optional) */
    val formId: String?,
    /** Forgot password URL for the selected provider (optional) */
    val forgetPasswordUrl: String?,
    /** Additional help message for the current login form (optional) */
    val help: String?,
    /** Additional information title for MFA login forms (optional) */
    val mfaInfoTitle: String?,
    /** Additional information on how to complete the MFA challenge login form (optional) */
    val mfaInfoText: String?,
    /** Time before the MFA challenge times out (optional) */
    val mfaTimeout: Long?,
    /** Type of login form see [ProviderFormType] for details */
    val formType: ProviderFormType,
    /** List of containers containing the login form rows */
    val containers: List<Container>
) {

    /**
     * Container that represents one or more rows that can be selected for a particular row in the login form view
     */
    data class Container(
        /** The field row choice ID used to identify multiple choice rows */
        val fieldRowChoice: String,
        /** Provider login form rows see [ProviderFormRow] for details */
        val rows: MutableList<ProviderFormRow>,
        /** ID of the selected row if there are multiple rows. Update this from the UI as the user chooses different rows */
        val selectedRowID: String?
    )

    /**
     * Convert the view model back to a data model suitable for sending back to the host
     *
     * @return Login form model representation of the view model in its current state
     */
    fun toDataModel(): ProviderLoginForm {
        val rows = mutableListOf<ProviderFormRow>()
        containers.forEach { container ->
            rows.addAll(container.rows)
        }

        return ProviderLoginForm(
            formId = formId,
            forgetPasswordUrl = forgetPasswordUrl,
            help = help,
            mfaInfoTitle = mfaInfoTitle,
            mfaInfoText = mfaInfoText,
            mfaTimeout = mfaTimeout,
            formType = formType,
            rows = rows
        )
    }

    /**
     * Validate any multiple choice rows have at least one valid value filled
     *
     * @param completion Validation completion handler with valid result and optional error if validation fails
     */
    fun validateMultipleChoice(completion: FormValidationCompletionListener) {
        // Validate multiple field choice
        var validValueFound = true
        var invalidRowLabel: String? = null

        containers.forEach { container ->
            if (container.rows.isNotEmpty()) {
                validValueFound = false

                container.rows.forEach { row ->
                    invalidRowLabel = row.label

                    row.fields.forEach { field ->
                        if (field.value?.isNotBlank() == true) {
                            validValueFound = true
                        }
                    }
                }
            }
        }

        // Check final row
        if (!validValueFound) {
            // No filled selection was found, fail validation
            invalidRowLabel?.let {
                completion.invoke(false, LoginFormError(type = LoginFormErrorType.FIELD_CHOICE_NOT_SELECTED, fieldName = it))

                return
            }
        }

        completion.invoke(true, null)
    }
}

/**
 * Creates a display model from the specified login form model
 *
 * @return Display model representing the login form model
 */
fun ProviderLoginForm.toDisplay(): ProviderLoginFormDisplay {
    val containers = mutableListOf<ProviderLoginFormDisplay.Container>()

    var lastCellIndex = -1

    rows.forEach { row ->
        if (lastCellIndex != -1 && row.fieldRowChoice == containers[lastCellIndex].fieldRowChoice) {
            containers[lastCellIndex].rows.add(row)
        } else {
            val cell = ProviderLoginFormDisplay.Container(fieldRowChoice = row.fieldRowChoice, rows = mutableListOf(row), selectedRowID = row.rowId)
            containers.add(cell)

            lastCellIndex = containers.size - 1
        }
    }

    return ProviderLoginFormDisplay(
        formId = formId,
        forgetPasswordUrl = forgetPasswordUrl,
        help = help,
        mfaInfoTitle = mfaInfoTitle,
        mfaInfoText = mfaInfoText,
        mfaTimeout = mfaTimeout,
        formType = formType,
        containers = containers
    )
}
