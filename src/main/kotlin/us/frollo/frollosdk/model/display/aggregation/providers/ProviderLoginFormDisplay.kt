package us.frollo.frollosdk.model.display.aggregation.providers

import us.frollo.frollosdk.core.FormValidationCompletionListener
import us.frollo.frollosdk.error.LoginFormError
import us.frollo.frollosdk.error.LoginFormErrorType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormRow
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm

data class ProviderLoginFormDisplay(
        val formId: String?,
        val forgetPasswordUrl: String?,
        val help: String?,
        val mfaInfoTitle: String?,
        val mfaInfoText: String?,
        val mfaTimeout: Long?,
        val formType: ProviderFormType,
        val containers: List<Container>) {

    data class Container(
            val fieldRowChoice: String,
            val rows: MutableList<ProviderFormRow>,
            val selectedRowID: String?)

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