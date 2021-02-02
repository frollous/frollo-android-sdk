package us.frollo.frollosdk.model.coredata.contacts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel

@Entity(
    tableName = "contact",
    indices = [
        Index("contact_id"),
    ]
)

/** Data representation of a Contact */
data class Contact(

    /** Unique ID of the contact */
    @PrimaryKey
    @ColumnInfo(name = "contact_id") val contactId: Long,

    /** Contact created date */
    @ColumnInfo(name = "created_date") val createdDate: String,

    /** Contact modified date */
    @ColumnInfo(name = "modified_date") val modifiedDate: String,

    /** Contact verified status */
    @ColumnInfo(name = "verified") val verified: Boolean,

    /** Related provider account IDs of the contact */
    @ColumnInfo(name = "related_provider_account_ids") val relatedProviderAccountIds: List<Long>?,

    /** Name of the contact */
    @ColumnInfo(name = "name") var name: String,

    /** Nick name of the contact */
    @ColumnInfo(name = "nick_name") var nickName: String,

    /** Description */
    @ColumnInfo(name = "description") var description: String?,

    /** Payment Method of the contact */
    @ColumnInfo(name = "payment_method") var paymentMethod: PaymentMethod,

    /** Payment Details of the contact  */
    @ColumnInfo(name = "payment_details") var paymentDetails: PaymentDetails

) : IAdapterModel