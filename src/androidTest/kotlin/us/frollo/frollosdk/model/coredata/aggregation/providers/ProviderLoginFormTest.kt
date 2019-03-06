package us.frollo.frollosdk.model.coredata.aggregation.providers

import android.app.Application
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.error.LoginFormError
import us.frollo.frollosdk.error.LoginFormErrorType
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.loginFormFilledData
import us.frollo.frollosdk.model.loginFormFilledMaxLengthExceededField
import us.frollo.frollosdk.model.loginFormFilledMissingRequiredField
import us.frollo.frollosdk.model.loginFormFilledRegexInvalidField
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson

class ProviderLoginFormTest {

    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setUp() {
        FrolloSDK.app = app
    }

    @Test
    fun testParsingLoginForm() {
        val form: ProviderLoginForm = Gson().fromJson(readStringFromJson(app, R.raw.provider_login_form_login))

        assertEquals("13039", form.formId)
        assertEquals(ProviderFormType.LOGIN, form.formType)
        assertEquals("https://ib.mebank.com.au/auth/ib/login.html", form.forgetPasswordUrl)
        assertNull(form.help)
        assertNull(form.mfaInfoText)
        assertNull(form.mfaTimeout)
        assertNull(form.mfaInfoTitle)

        assertEquals(2, form.rows.size)
        assertEquals("Customer ID", form.rows[0].label)
        assertEquals("0001", form.rows[0].form)
        assertEquals("0001", form.rows[0].fieldRowChoice)
        assertNull(form.rows[0].hint)

        assertEquals(1, form.rows[0].fields.size)
        assertEquals("53364", form.rows[0].fields[0].fieldId)
        assertEquals("LOGIN", form.rows[0].fields[0].name)
        assertEquals(8, form.rows[0].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[0].fields[0].type)
        assertEquals("", form.rows[0].fields[0].value)
        assertFalse(form.rows[0].fields[0].isOptional)
        assertTrue(form.rows[0].fields[0].valueEditable)
        assertNull(form.rows[0].fields[0].image)
        assertNull(form.rows[0].fields[0].prefix)
        assertNull(form.rows[0].fields[0].options)
        assertNull(form.rows[0].fields[0].suffix)
        assertNull(form.rows[0].fields[0].validations)

        assertEquals("Access Code", form.rows[1].label)
        assertEquals("0001", form.rows[1].form)
        assertEquals("0002", form.rows[1].fieldRowChoice)
        assertNull(form.rows[1].hint)

        assertEquals(1, form.rows[1].fields.size)
        assertEquals("53363", form.rows[1].fields[0].fieldId)
        assertEquals("PASSWORD", form.rows[1].fields[0].name)
        assertEquals(255, form.rows[1].fields[0].maxLength)
        assertEquals(ProviderFieldType.PASSWORD, form.rows[1].fields[0].type)
        assertEquals("", form.rows[1].fields[0].value)
        assertFalse(form.rows[1].fields[0].isOptional)
        assertTrue(form.rows[1].fields[0].valueEditable)
        assertNull(form.rows[1].fields[0].image)
        assertNull(form.rows[1].fields[0].prefix)
        assertNull(form.rows[1].fields[0].options)
        assertNull(form.rows[1].fields[0].suffix)
        assertNull(form.rows[1].fields[0].validations)
    }

    @Test
    fun testParsingCaptchaForm() {
        val form: ProviderLoginForm = Gson().fromJson(readStringFromJson(app, R.raw.provider_login_form_captcha))

        assertNull(form.formId)
        assertEquals(ProviderFormType.IMAGE, form.formType)
        assertNull(form.forgetPasswordUrl)
        assertNull(form.help)
        assertNull(form.mfaInfoText)
        assertEquals(98580L, form.mfaTimeout)
        assertNull(form.mfaInfoTitle)

        assertEquals(1, form.rows.size)
        assertEquals("image_row", form.rows[0].rowId)
        assertEquals("Enter the words as shown in the image", form.rows[0].label)
        assertEquals("0001", form.rows[0].form)
        assertEquals("0001", form.rows[0].fieldRowChoice)
        assertNull(form.rows[0].hint)

        assertEquals(1, form.rows[0].fields.size)

        val bitmap = BitmapFactory.decodeStream(app.resources.openRawResource(R.raw.captcha))

        assertEquals("image", form.rows[0].fields[0].fieldId)
        assertEquals("imageValue", form.rows[0].fields[0].name)
        assertEquals(10, form.rows[0].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[0].fields[0].type)
        assertEquals("", form.rows[0].fields[0].value)
        assertFalse(form.rows[0].fields[0].isOptional)
        assertTrue(form.rows[0].fields[0].valueEditable)
        assertTrue(form.rows[0].fields[0].imageBitmap?.sameAs(bitmap) == true)
        assertNull(form.rows[0].fields[0].prefix)
        assertNull(form.rows[0].fields[0].options)
        assertNull(form.rows[0].fields[0].suffix)
        assertNull(form.rows[0].fields[0].validations)
    }

    @Test
    fun testParsingMultipleChoiceForm() {
        val form: ProviderLoginForm = Gson().fromJson(readStringFromJson(app, R.raw.provider_login_form_multiple_choice))

        assertEquals("3326", form.formId)
        assertEquals(ProviderFormType.LOGIN, form.formType)
        assertEquals("https://ibank.barclays.co.uk/fp/1_2m/online/1,17266,loginForgottenDetails,00.html?forgottenLoginDetails=true", form.forgetPasswordUrl)
        assertEquals("To link your Barclay account you must enter your Surname and one of the following: your Membership Number, Card Number, or Sort Code and Account Number.", form.help)
        assertNull(form.mfaInfoText)
        assertNull(form.mfaTimeout)
        assertNull(form.mfaInfoTitle)

        assertEquals(4, form.rows.size)

        assertEquals("7223", form.rows[0].rowId)
        assertEquals("Surname", form.rows[0].label)
        assertEquals("0001", form.rows[0].form)
        assertEquals("0001", form.rows[0].fieldRowChoice)
        assertNull(form.rows[0].hint)

        assertEquals(1, form.rows[0].fields.size)

        assertEquals("4956", form.rows[0].fields[0].fieldId)
        assertEquals("LOGIN", form.rows[0].fields[0].name)
        assertNull(form.rows[0].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[0].fields[0].type)
        assertEquals("", form.rows[0].fields[0].value)
        assertFalse(form.rows[0].fields[0].isOptional)
        assertTrue(form.rows[0].fields[0].valueEditable)
        assertNull(form.rows[0].fields[0].image)
        assertNull(form.rows[0].fields[0].prefix)
        assertNull(form.rows[0].fields[0].options)
        assertNull(form.rows[0].fields[0].suffix)
        assertNull(form.rows[0].fields[0].validations)

        assertEquals("7224", form.rows[1].rowId)
        assertEquals("Membership number", form.rows[1].label)
        assertEquals("0001", form.rows[1].form)
        assertEquals("0002 Choice", form.rows[1].fieldRowChoice)
        assertNull(form.rows[1].hint)

        assertEquals(1, form.rows[1].fields.size)

        assertEquals("4958", form.rows[1].fields[0].fieldId)
        assertEquals("OP_LOGIN1", form.rows[1].fields[0].name)
        assertEquals(12, form.rows[1].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[1].fields[0].type)
        assertEquals("", form.rows[1].fields[0].value)
        assertTrue(form.rows[1].fields[0].isOptional)
        assertTrue(form.rows[1].fields[0].valueEditable)
        assertNull(form.rows[1].fields[0].image)
        assertNull(form.rows[1].fields[0].prefix)
        assertNull(form.rows[1].fields[0].options)
        assertNull(form.rows[1].fields[0].suffix)
        assertNull(form.rows[1].fields[0].validations)

        assertEquals("151124", form.rows[2].rowId)
        assertEquals("Card number", form.rows[2].label)
        assertEquals("0001", form.rows[2].form)
        assertEquals("0002 Choice", form.rows[2].fieldRowChoice)
        assertNull(form.rows[2].hint)

        assertEquals(1, form.rows[2].fields.size)

        assertEquals("65773", form.rows[2].fields[0].fieldId)
        assertEquals("OP_LOGIN2", form.rows[2].fields[0].name)
        assertEquals(16, form.rows[2].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[2].fields[0].type)
        assertEquals("", form.rows[2].fields[0].value)
        assertTrue(form.rows[2].fields[0].isOptional)
        assertTrue(form.rows[2].fields[0].valueEditable)
        assertNull(form.rows[2].fields[0].image)
        assertNull(form.rows[2].fields[0].prefix)
        assertNull(form.rows[2].fields[0].options)
        assertNull(form.rows[2].fields[0].suffix)
        assertNull(form.rows[2].fields[0].validations)

        assertEquals("151125", form.rows[3].rowId)
        assertEquals("Sort code and Account number", form.rows[3].label)
        assertEquals("0001", form.rows[3].form)
        assertEquals("0002 Choice", form.rows[3].fieldRowChoice)
        assertNull(form.rows[3].hint)

        assertEquals(2, form.rows[3].fields.size)

        assertEquals("65774", form.rows[3].fields[0].fieldId)
        assertEquals("OP_LOGIN3", form.rows[3].fields[0].name)
        assertEquals(6, form.rows[3].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[3].fields[0].type)
        assertEquals("", form.rows[3].fields[0].value)
        assertTrue(form.rows[3].fields[0].isOptional)
        assertTrue(form.rows[3].fields[0].valueEditable)
        assertNull(form.rows[3].fields[0].image)
        assertNull(form.rows[3].fields[0].prefix)
        assertNull(form.rows[3].fields[0].options)
        assertNull(form.rows[3].fields[0].suffix)
        assertNull(form.rows[3].fields[0].validations)

        assertEquals("65764", form.rows[3].fields[1].fieldId)
        assertEquals("OP_LOGIN4", form.rows[3].fields[1].name)
        assertEquals(8, form.rows[3].fields[1].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[3].fields[1].type)
        assertEquals("", form.rows[3].fields[1].value)
        assertTrue(form.rows[3].fields[1].isOptional)
        assertTrue(form.rows[3].fields[1].valueEditable)
        assertNull(form.rows[3].fields[1].image)
        assertNull(form.rows[3].fields[1].prefix)
        assertNull(form.rows[3].fields[1].options)
        assertNull(form.rows[3].fields[1].suffix)
        assertNull(form.rows[3].fields[1].validations)
    }

    @Test
    fun testParsingQuestionAnswerForm() {
        val form: ProviderLoginForm = Gson().fromJson(readStringFromJson(app, R.raw.provider_login_form_question_answer))

        assertNull(form.formId)
        assertEquals(ProviderFormType.QUESTION_AND_ANSWER, form.formType)
        assertNull(form.forgetPasswordUrl)
        assertNull(form.help)
        assertNull(form.mfaInfoText)
        assertEquals(99670L, form.mfaTimeout)
        assertNull(form.mfaInfoTitle)

        assertEquals(2, form.rows.size)

        assertEquals("SQandA--QUESTION_1--Row--1", form.rows[0].rowId)
        assertEquals("What is the name of your state?", form.rows[0].label)
        assertEquals("0001", form.rows[0].form)
        assertEquals("0001", form.rows[0].fieldRowChoice)
        assertNull(form.rows[0].hint)

        assertEquals(1, form.rows[0].fields.size)

        assertEquals("SQandA--QUESTION_1--1", form.rows[0].fields[0].fieldId)
        assertEquals("QUESTION_1", form.rows[0].fields[0].name)
        assertNull(form.rows[0].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[0].fields[0].type)
        assertEquals("", form.rows[0].fields[0].value)
        assertFalse(form.rows[0].fields[0].isOptional)
        assertTrue(form.rows[0].fields[0].valueEditable)
        assertNull(form.rows[0].fields[0].image)
        assertNull(form.rows[0].fields[0].prefix)
        assertNull(form.rows[0].fields[0].options)
        assertNull(form.rows[0].fields[0].suffix)
        assertNull(form.rows[0].fields[0].validations)

        assertEquals("SQandA--QUESTION_2--Row--2", form.rows[1].rowId)
        assertEquals("What is the name of your first school", form.rows[1].label)
        assertEquals("0001", form.rows[1].form)
        assertEquals("0002", form.rows[1].fieldRowChoice)
        assertNull(form.rows[1].hint)

        assertEquals(1, form.rows[1].fields.size)

        assertEquals("SQandA--QUESTION_2--2", form.rows[1].fields[0].fieldId)
        assertEquals("QUESTION_2", form.rows[1].fields[0].name)
        assertNull(form.rows[1].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[1].fields[0].type)
        assertEquals("", form.rows[1].fields[0].value)
        assertFalse(form.rows[1].fields[0].isOptional)
        assertTrue(form.rows[1].fields[0].valueEditable)
        assertNull(form.rows[1].fields[0].image)
        assertNull(form.rows[1].fields[0].prefix)
        assertNull(form.rows[1].fields[0].options)
        assertNull(form.rows[1].fields[0].suffix)
        assertNull(form.rows[1].fields[0].validations)
    }

    @Test
    fun testParsingTokenForm() {
        val form: ProviderLoginForm = Gson().fromJson(readStringFromJson(app, R.raw.provider_login_form_token))

        assertNull(form.formId)
        assertEquals(ProviderFormType.TOKEN, form.formType)
        assertNull(form.forgetPasswordUrl)
        assertNull(form.help)
        assertNull(form.mfaInfoText)
        assertEquals(99180L, form.mfaTimeout)
        assertNull(form.mfaInfoTitle)

        assertEquals(1, form.rows.size)

        assertEquals("token_row", form.rows[0].rowId)
        assertEquals("Security Key", form.rows[0].label)
        assertEquals("0001", form.rows[0].form)
        assertEquals("0001", form.rows[0].fieldRowChoice)
        assertNull(form.rows[0].hint)

        assertEquals(1, form.rows[0].fields.size)

        assertEquals("token", form.rows[0].fields[0].fieldId)
        assertEquals("tokenValue", form.rows[0].fields[0].name)
        assertEquals(6, form.rows[0].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[0].fields[0].type)
        assertEquals("", form.rows[0].fields[0].value)
        assertFalse(form.rows[0].fields[0].isOptional)
        assertTrue(form.rows[0].fields[0].valueEditable)
        assertNull(form.rows[0].fields[0].image)
        assertNull(form.rows[0].fields[0].prefix)
        assertNull(form.rows[0].fields[0].options)
        assertNull(form.rows[0].fields[0].suffix)
        assertNull(form.rows[0].fields[0].validations)

    }

    @Test
    fun testParsingOptionsForm() {
        val form: ProviderLoginForm = Gson().fromJson(readStringFromJson(app, R.raw.provider_login_form_options))

        assertEquals("12525", form.formId)
        assertEquals(ProviderFormType.LOGIN, form.formType)
        assertNull(form.forgetPasswordUrl)
        assertNull(form.help)
        assertNull(form.mfaInfoText)
        assertNull(form.mfaTimeout)
        assertNull(form.mfaInfoTitle)

        assertEquals(4, form.rows.size)

        assertEquals("User ID", form.rows[0].label)
        assertEquals("0001", form.rows[0].form)
        assertEquals("0001", form.rows[0].fieldRowChoice)
        assertNull(form.rows[0].hint)

        assertEquals(1, form.rows[0].fields.size)

        assertEquals("49685", form.rows[0].fields[0].fieldId)
        assertEquals("LOGIN", form.rows[0].fields[0].name)
        assertNull(form.rows[0].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[0].fields[0].type)
        assertEquals("", form.rows[0].fields[0].value)
        assertFalse(form.rows[0].fields[0].isOptional)
        assertTrue(form.rows[0].fields[0].valueEditable)
        assertNull(form.rows[0].fields[0].image)
        assertNull(form.rows[0].fields[0].prefix)
        assertNull(form.rows[0].fields[0].options)
        assertNull(form.rows[0].fields[0].suffix)
        assertNull(form.rows[0].fields[0].validations)

        assertEquals("83722", form.rows[1].rowId)
        assertEquals("Password", form.rows[1].label)
        assertEquals("0001", form.rows[1].form)
        assertEquals("0002", form.rows[1].fieldRowChoice)
        assertNull(form.rows[1].hint)

        assertEquals(1, form.rows[1].fields.size)

        assertEquals("49684", form.rows[1].fields[0].fieldId)
        assertEquals("PASSWORD", form.rows[1].fields[0].name)
        assertNull(form.rows[1].fields[0].maxLength)
        assertEquals(ProviderFieldType.PASSWORD, form.rows[1].fields[0].type)
        assertEquals("", form.rows[1].fields[0].value)
        assertFalse(form.rows[1].fields[0].isOptional)
        assertTrue(form.rows[1].fields[0].valueEditable)
        assertNull(form.rows[1].fields[0].image)
        assertNull(form.rows[1].fields[0].prefix)
        assertNull(form.rows[1].fields[0].options)
        assertNull(form.rows[1].fields[0].suffix)
        assertNull(form.rows[1].fields[0].validations)

        assertEquals("83720", form.rows[2].rowId)
        assertEquals("Question 1", form.rows[2].label)
        assertEquals("0001", form.rows[2].form)
        assertEquals("0003", form.rows[2].fieldRowChoice)
        assertNull(form.rows[2].hint)

        assertEquals(1, form.rows[2].fields.size)

        assertEquals("49686", form.rows[2].fields[0].fieldId)
        assertEquals("OP_OPTIONS1", form.rows[2].fields[0].name)
        assertNull(form.rows[2].fields[0].maxLength)
        assertEquals(ProviderFieldType.OPTION, form.rows[2].fields[0].type)
        assertEquals("", form.rows[2].fields[0].value)
        assertTrue(form.rows[2].fields[0].isOptional)
        assertTrue(form.rows[2].fields[0].valueEditable)
        assertNull(form.rows[2].fields[0].image)
        assertNull(form.rows[2].fields[0].prefix)
        assertNotNull(form.rows[2].fields[0].options)
        assertNull(form.rows[2].fields[0].suffix)
        assertNull(form.rows[2].fields[0].validations)

        assertEquals(10, form.rows[2].fields[0].options?.size)

        assertEquals("What is your father's middle name?", form.rows[2].fields[0].options?.get(0)?.displayText)
        assertEquals("your father's middle name", form.rows[2].fields[0].options?.get(0)?.optionValue)

        assertEquals("83719", form.rows[3].rowId)
        assertEquals("Answer 1", form.rows[3].label)
        assertEquals("0001", form.rows[3].form)
        assertEquals("0004", form.rows[3].fieldRowChoice)
        assertNull(form.rows[3].hint)

        assertEquals(1, form.rows[3].fields.size)

        assertEquals("49687", form.rows[3].fields[0].fieldId)
        assertEquals("OP_LOGIN1", form.rows[3].fields[0].name)
        assertNull(form.rows[3].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[3].fields[0].type)
        assertEquals("", form.rows[3].fields[0].value)
        assertTrue(form.rows[3].fields[0].isOptional)
        assertTrue(form.rows[3].fields[0].valueEditable)
        assertNull(form.rows[3].fields[0].image)
        assertNull(form.rows[3].fields[0].prefix)
        assertNull(form.rows[3].fields[0].options)
        assertNull(form.rows[3].fields[0].suffix)
        assertNull(form.rows[3].fields[0].validations)
    }

    @Test
    fun testParsingValidationForm() {
        val form: ProviderLoginForm = Gson().fromJson(readStringFromJson(app, R.raw.provider_login_form_validation))

        assertEquals("3008", form.formId)
        assertEquals(ProviderFormType.LOGIN, form.formType)
        assertEquals("https://www.ingdirect.com.au/client/index.aspx", form.forgetPasswordUrl)
        assertNull(form.help)
        assertNull(form.mfaInfoText)
        assertNull(form.mfaTimeout)
        assertNull(form.mfaInfoTitle)

        assertEquals(2, form.rows.size)

        assertEquals("6797", form.rows[0].rowId)
        assertEquals("Client Number", form.rows[0].label)
        assertEquals("0001", form.rows[0].form)
        assertEquals("0001", form.rows[0].fieldRowChoice)
        assertNull(form.rows[0].hint)

        assertEquals(1, form.rows[0].fields.size)

        assertEquals("4410", form.rows[0].fields[0].fieldId)
        assertEquals("LOGIN", form.rows[0].fields[0].name)
        assertEquals(8, form.rows[0].fields[0].maxLength)
        assertEquals(ProviderFieldType.TEXT, form.rows[0].fields[0].type)
        assertEquals("", form.rows[0].fields[0].value)
        assertFalse(form.rows[0].fields[0].isOptional)
        assertTrue(form.rows[0].fields[0].valueEditable)
        assertNull(form.rows[0].fields[0].image)
        assertNull(form.rows[0].fields[0].prefix)
        assertNull(form.rows[0].fields[0].options)
        assertNull(form.rows[0].fields[0].suffix)
        assertNotNull(form.rows[0].fields[0].validations)

        assertEquals(1, form.rows[0].fields[0].validations?.size)

        assertEquals("^[0-9]{0,8}$", form.rows[0].fields[0].validations?.get(0)?.regExp)
        assertEquals("Please enter a valid Client Number", form.rows[0].fields[0].validations?.get(0)?.errorMsg)

        assertEquals("6796", form.rows[1].rowId)
        assertEquals("Access Code", form.rows[1].label)
        assertEquals("0001", form.rows[1].form)
        assertEquals("0002", form.rows[1].fieldRowChoice)
        assertNull(form.rows[1].hint)

        assertEquals(1, form.rows[1].fields.size)

        assertEquals("4409", form.rows[1].fields[0].fieldId)
        assertEquals("PASSWORD", form.rows[1].fields[0].name)
        assertEquals(6, form.rows[1].fields[0].maxLength)
        assertEquals(ProviderFieldType.PASSWORD, form.rows[1].fields[0].type)
        assertEquals("", form.rows[1].fields[0].value)
        assertFalse(form.rows[1].fields[0].isOptional)
        assertTrue(form.rows[1].fields[0].valueEditable)
        assertNull(form.rows[1].fields[0].image)
        assertNull(form.rows[1].fields[0].prefix)
        assertNull(form.rows[1].fields[0].options)
        assertNull(form.rows[1].fields[0].suffix)
        assertNotNull(form.rows[1].fields[0].validations)

        assertEquals(1, form.rows[1].fields[0].validations?.size)

        assertEquals("^[0-9]{0,6}$", form.rows[1].fields[0].validations?.get(0)?.regExp)
        assertEquals("Please enter a valid Access Code", form.rows[1].fields[0].validations?.get(0)?.errorMsg)
    }

    @Test
    fun testProviderLoginFormEncryptValues() {
        val encryptionAlias = "09282016_1"
        val encryptionKey = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1eXKHvPBlS4A41OvQqFn0SfNH7OgEs2MXMLeyp3xKorEipEKuzv/JDtHFHRAfYwyeiC0q+me0R8GLA6NEDGDfpxGv/XUFyza609ZqtCTOiGCp8DcjLG0mPljdGA1Df0BKhF3y5uata1y0dKSI8aY8lXPza+Tsw4TtjdmHbJ2rR3sFZkYch1RTmNKxKDxMgUmtIk785lIfLJ2x6lvh4ZS9QhuAnsoVM91WWKHrLHYfAeA/zD1TxHDm5/4wPbmFLEBe2+5zGae19nsA/9zDwKP4whpte9HuDDQa5Vsq+aWj5pDJuvFgwA/DStqcHGijn5gzB/JXEoE9qx+dcG92PpvfwIDAQAB\n------END PUBLIC KEY------"

        val form = loginFormFilledData()

        form.encryptValues(encryptionKey = encryptionKey, encryptionAlias = encryptionAlias)

        assertTrue(form.rows[0].fields[0].value?.contains(encryptionAlias) == true)
        assertEquals(form.rows[0].fields[0].value?.length, 523)
        assertTrue(form.rows[1].fields[0].value?.contains(encryptionAlias) == true)
        assertEquals(form.rows[1].fields[0].value?.length, 523)
        assertNotEquals(form.rows[0].fields[0].value, form.rows[1].fields[0].value)
    }

    @Test
    fun testProviderLoginFormOptionalValidation() {
        val loginForm = loginFormFilledMissingRequiredField()

        loginForm.validateForm { valid, error ->
            assertFalse(valid)
            assertNotNull(error)

            assertTrue(error is LoginFormError)
            assertEquals(LoginFormErrorType.MISSING_REQUIRED_FIELD, (error as LoginFormError).type)
            assertEquals("User ID", error.fieldName)
        }
    }

    @Test
    fun testProviderLoginFormMaxLengthValidation() {
        val loginForm = loginFormFilledMaxLengthExceededField()

        loginForm.validateForm { valid, error ->
            assertFalse(valid)
            assertNotNull(error)

            assertTrue(error is LoginFormError)
            assertEquals(LoginFormErrorType.MAX_LENGTH_EXCEEDED, (error as LoginFormError).type)
            assertEquals("Member Number", error.fieldName)
        }
    }

    @Test
    fun testProviderLoginFormRegexValidation() {
        val loginForm = loginFormFilledRegexInvalidField()

        loginForm.validateForm { valid, error ->
            assertFalse(valid)
            assertNotNull(error)

            assertTrue(error is LoginFormError)
            assertEquals(LoginFormErrorType.VALIDATION_FAILED, (error as LoginFormError).type)
            assertEquals("Password", error.fieldName)
            assertEquals("Please enter a valid Access Code", error.additionalError)
        }
    }

    @Test
    fun testProviderLoginFormShouldEncrypt() {
        var form = loginFormFilledData(ProviderFormType.LOGIN)
        assertTrue(form.shouldEncrypt(ProviderEncryptionType.ENCRYPT_VALUES))
        form = loginFormFilledData(ProviderFormType.TOKEN)
        assertFalse(form.shouldEncrypt(ProviderEncryptionType.ENCRYPT_VALUES))
        form = loginFormFilledData(ProviderFormType.QUESTION_AND_ANSWER)
        assertTrue(form.shouldEncrypt(ProviderEncryptionType.ENCRYPT_VALUES))
        form = loginFormFilledData(ProviderFormType.IMAGE)
        assertFalse(form.shouldEncrypt(ProviderEncryptionType.ENCRYPT_VALUES))

        assertFalse(form.shouldEncrypt(ProviderEncryptionType.UNSUPPORTED))
    }

}