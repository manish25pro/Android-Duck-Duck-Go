/*
 * Copyright (c) 2022 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.networkprotection.impl.di

import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.prefs.VpnSharedPreferencesProvider
import com.duckduckgo.networkprotection.impl.waitlist.store.NetPWaitlistDataStoreSharedPreferences
import com.duckduckgo.networkprotection.impl.waitlist.store.NetPWaitlistRepository
import com.duckduckgo.networkprotection.impl.waitlist.store.RealNetPWaitlistRepository
import com.duckduckgo.networkprotection.store.NetworkProtectionRepository
import com.duckduckgo.networkprotection.store.RealNetworkProtectionPrefs
import com.duckduckgo.networkprotection.store.RealNetworkProtectionRepository
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.SingleInstanceIn

@Module
@ContributesTo(AppScope::class)
class DataModule {
    @Provides
    @SingleInstanceIn(AppScope::class)
    fun provideNetworkProtectionRepository(
        vpnSharedPreferencesProvider: VpnSharedPreferencesProvider,
    ): NetworkProtectionRepository = RealNetworkProtectionRepository(RealNetworkProtectionPrefs(vpnSharedPreferencesProvider))

    @Provides
    fun provideNetPWaitlistRepository(
        vpnSharedPreferencesProvider: VpnSharedPreferencesProvider,
    ): NetPWaitlistRepository = RealNetPWaitlistRepository(NetPWaitlistDataStoreSharedPreferences(vpnSharedPreferencesProvider))
}
