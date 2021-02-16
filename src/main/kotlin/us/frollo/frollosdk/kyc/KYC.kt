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

package us.frollo.frollosdk.kyc

import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.coredata.kyc.UserKyc
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.KycAPI

/**
 * Manages user KYC
 */
class KYC(network: NetworkService) {

    companion object {
        private const val TAG = "KYC"
    }

    private val kycAPI: KycAPI = network.create(KycAPI::class.java)

    /**
     * Get KYC for user from the host
     *
     * @param completion Completion handler with optional error if the request fails else data if success
     */
    fun fetchKyc(completion: OnFrolloSDKCompletionListener<Resource<UserKyc>>) {
        kycAPI.fetchKyc().enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    completion.invoke(resource)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#fetchKyc", resource.error?.localizedDescription)
                    completion.invoke(resource)
                }
            }
        }
    }

    /**
     * Create or update KYC for the user
     *
     * @param userKyc KYC of the user to create
     * @param completion Completion handler with optional error if the request fails else data if success
     */
    fun submitKyc(userKyc: UserKyc, completion: OnFrolloSDKCompletionListener<Resource<UserKyc>>) {
        kycAPI.submitKyc(userKyc).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    completion.invoke(resource)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#submitKyc", resource.error?.localizedDescription)
                    completion.invoke(resource)
                }
            }
        }
    }
}
