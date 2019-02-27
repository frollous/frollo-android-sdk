package us.frollo.frollosdk.extensions

import android.os.Bundle
import androidx.sqlite.db.SimpleSQLiteQuery
import us.frollo.frollosdk.model.api.user.UserUpdateRequest
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload
import us.frollo.frollosdk.model.coredata.user.User
import us.frollo.frollosdk.notifications.NotificationPayloadNames
import java.lang.StringBuilder

internal fun User.updateRequest(): UserUpdateRequest =
        UserUpdateRequest(
                firstName = firstName,
                email = email,
                primaryCurrency = primaryCurrency,
                attribution = attribution,
                lastName = lastName,
                mobileNumber = mobileNumber,
                gender = gender,
                currentAddress = currentAddress,
                householdSize = householdSize,
                householdType = householdType,
                occupation = occupation,
                industry = industry,
                dateOfBirth = dateOfBirth,
                driverLicense = driverLicense)

internal fun generateSQLQueryMessages(searchParams: List<String>, read: Boolean? = null): SimpleSQLiteQuery {
    val sb = StringBuilder()

    sb.append("(")

    searchParams.forEachIndexed { index, str ->
        sb.append("(message_types LIKE '%|$str|%')")
        if (index < searchParams.size - 1) sb.append(" OR ")
    }

    sb.append(")")

    read?.let { sb.append(" AND read = ${ it.toInt() }") }

    return SimpleSQLiteQuery("SELECT * FROM message WHERE $sb")
}

internal fun sqlForTransactionStaleIds(fromDate: String, toDate: String, accountIds: LongArray? = null, transactionIncluded: Boolean? = null): SimpleSQLiteQuery {
    val sb = StringBuilder()

    accountIds?.let { sb.append(" AND account_id IN (${accountIds.joinToString(",")}) ") }

    transactionIncluded?.let { sb.append(" AND included = ${ it.toInt() } ") }

    val query = "SELECT transaction_id FROM transaction_model " +
            "WHERE ((transaction_date BETWEEN Date('$fromDate') AND Date('$toDate')) $sb)"

    return SimpleSQLiteQuery(query)
}

internal fun Bundle.toNotificationPayload(): NotificationPayload =
        createNotificationPayload(
                getString(NotificationPayloadNames.EVENT.toString()),
                getString(NotificationPayloadNames.LINK.toString()),
                getString(NotificationPayloadNames.TRANSACTION_IDS.toString()),
                getString(NotificationPayloadNames.USER_EVENT_ID.toString()),
                getString(NotificationPayloadNames.USER_MESSAGE_ID.toString()))

internal fun Map<String, String>.toNotificationPayload(): NotificationPayload =
        createNotificationPayload(
                get(NotificationPayloadNames.EVENT.toString()),
                get(NotificationPayloadNames.LINK.toString()),
                get(NotificationPayloadNames.TRANSACTION_IDS.toString()),
                get(NotificationPayloadNames.USER_EVENT_ID.toString()),
                get(NotificationPayloadNames.USER_MESSAGE_ID.toString()))

internal fun createNotificationPayload(event: String? = null, link: String? = null, transactionIDs: String? = null, userEventID: String? = null, userMessageID: String? = null) =
        NotificationPayload(
                event = event,
                link = link,
                transactionIDs = transactionIDs
                        ?.replace("[", "")
                        ?.replace("]", "")
                        ?.split(",")
                        ?.map { it.toLong() }
                        ?.toList(),
                userEventID = userEventID?.let { it.trim().toLong() },
                userMessageID = userMessageID?.let { it.trim().toLong() })