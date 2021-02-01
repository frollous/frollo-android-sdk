package us.frollo.frollosdk.model.coredata.contacts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel

@Entity(
    tableName = "bill",
    indices = [
        Index("contact_id"),
    ]
)

/** Data representation of a Contact */
data class Contact(

    /** Unique ID of the contact */
    @PrimaryKey
    @ColumnInfo(name = "contact_id") val contactId: Int,

    /** Contact created date */
    @ColumnInfo(name = "created_date") var createdDate: String,

    /** Contact modified date */
    @ColumnInfo(name = "modified_date") val modifiedDate: String,

    /** Contact verified status */
    @ColumnInfo(name = "verified") var verified: Boolean,

    /** Related provider account IDs of the contact */
    @ColumnInfo(name = "related_provider_account_ids") var relatedProviderAccountIds: List<Int>?,

    /** Name of the contact */
    @ColumnInfo(name = "name") val name: String,

    /** Nick name of the contact */
    @ColumnInfo(name = "nick_name") var nickName: String,

    /** Description */
    @ColumnInfo(name = "description") val description: String?,

    /** Payment Method  */
    @ColumnInfo(name = "payment_method") var paymentMethod: ContactType,

) : IAdapterModel
