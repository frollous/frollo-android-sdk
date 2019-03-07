package us.frollo.frollosdk.model.coredata.aggregation.merchants

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "merchant",
        indices = [Index("merchant_id")])

/** Data representation of a Merchant */
data class Merchant(

        /** Unique ID for the merchant */
        @PrimaryKey
        @ColumnInfo(name = "merchant_id") val merchantId: Long,

        /** Name of the merchant */
        @ColumnInfo(name = "name") val name: String,

        /** Type of merchant */
        @ColumnInfo(name = "merchant_type") val merchantType: MerchantType,

        /** URL of the merchant's small logo image */
        @ColumnInfo(name = "small_logo_url") val smallLogoUrl: String

): IAdapterModel