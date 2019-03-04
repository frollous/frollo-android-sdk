package us.frollo.frollosdk.core

/** Argument Keys for the Intent Data */
object ARGUMENT {
   /**
    * Generic Key for any data sent with an intent.
    */
   const val ARG_DATA = "arg_data"

   /**
    * Key for authentication status sent with [ACTION.ACTION_AUTHENTICATION_CHANGED] broadcast.
    */
   const val ARG_AUTHENTICATION_STATUS = "arg_authentication_status"

   /**
    * Key for transaction IDs sent with [ACTION.ACTION_REFRESH_TRANSACTIONS] broadcast.
    */
   internal const val ARG_TRANSACTION_IDS = "arg_transaction_ids"
}

/** Intent Actions */
object ACTION {
   /**
    * Local Broadcast triggered when ever the User data is updated.
    * Listen to this to detect the update to User data.
    */
   const val ACTION_USER_UPDATED = "us.frollo.frollosdk.ACTION_USER_UPDATED"


   /**
    * Local Broadcast triggered when ever the Transactions need to be refreshed.
    * Internal Only.
    */
   internal const val ACTION_REFRESH_TRANSACTIONS = "us.frollo.frollosdk.ACTION_REFRESH_TRANSACTIONS"

   /**
    * Local Broadcast triggered when ever the authentication status of the SDK changes.
    * Listen to this to detect if the SDK user has authenticated or been logged out.
    */
   const val ACTION_AUTHENTICATION_CHANGED = "us.frollo.frollosdk.ACTION_AUTHENTICATION_CHANGED"
}