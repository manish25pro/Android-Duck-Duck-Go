/*
 * Copyright (c) 2023 DuckDuckGo
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

package com.duckduckgo.networkprotection.impl.waitlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.global.DuckDuckGoActivity
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.mobile.android.ui.view.gone
import com.duckduckgo.mobile.android.ui.view.show
import com.duckduckgo.mobile.android.ui.viewbinding.viewBinding
import com.duckduckgo.networkprotection.impl.R
import com.duckduckgo.networkprotection.impl.databinding.ActivityNetpWaitlistBinding
import com.duckduckgo.networkprotection.impl.management.NetworkProtectionManagementActivity
import com.duckduckgo.networkprotection.impl.waitlist.NetPWaitlistState.CodeRedeemed
import com.duckduckgo.networkprotection.impl.waitlist.NetPWaitlistState.InBeta
import com.duckduckgo.networkprotection.impl.waitlist.NetPWaitlistState.NotJoinedQueue
import com.duckduckgo.networkprotection.impl.waitlist.NetPWaitlistState.NotUnlocked
import com.duckduckgo.networkprotection.impl.waitlist.NetPWaitlistViewModel.Command
import com.duckduckgo.networkprotection.impl.waitlist.NetPWaitlistViewModel.Command.EnterInviteCode
import com.duckduckgo.networkprotection.impl.waitlist.NetPWaitlistViewModel.Command.OpenNetP
import com.duckduckgo.networkprotection.impl.waitlist.NetPWaitlistViewModel.ViewState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@InjectWith(ActivityScope::class)
class NetPWaitlistActivity : DuckDuckGoActivity() {

    private val viewModel: NetPWaitlistViewModel by bindViewModel()
    private val binding: ActivityNetpWaitlistBinding by viewBinding()

    private val toolbar
        get() = binding.includeToolbar.toolbar

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        viewModel.onCodeRedeemed(result.resultCode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.viewState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { render(it) }
            .launchIn(lifecycleScope)
        viewModel.commands.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { executeCommand(it) }
            .launchIn(lifecycleScope)

        setContentView(binding.root)
        setupToolbar(toolbar)
        configureUiEventHandlers()
    }

    private fun configureUiEventHandlers() {
        binding.enterCodeButton.setOnClickListener { viewModel.haveAnInviteCode() }
        binding.getStartedButton.setOnClickListener { viewModel.getStarted() }
    }

    private fun render(viewState: ViewState) {
        when (viewState.waitlist) {
            is NotUnlocked -> renderNotJoinedQueue() // Should not happen
            is NotJoinedQueue -> renderNotJoinedQueue()
            is CodeRedeemed -> renderCodeRedeemed()
            is InBeta -> openNetP()
        }
    }
    private fun renderNotJoinedQueue() {
        binding.headerImage.setImageResource(R.drawable.ic_lock)
        binding.getStartedButton.gone()
        binding.enterCodeButton.show()
        binding.footerDescription.show()
    }

    private fun renderCodeRedeemed() {
        binding.statusTitle.text = getString(R.string.netpWaitlistRedeemedCodeStatus)
        binding.headerImage.setImageResource(R.drawable.ic_dragon)
        binding.getStartedButton.show()
        binding.enterCodeButton.gone()
        binding.footerDescription.gone()
    }

    private fun executeCommand(command: Command) {
        when (command) {
            is EnterInviteCode -> openRedeemCode()
            is OpenNetP -> openNetP()
        }
    }

    private fun openRedeemCode() {
        startForResult.launch(NetPWaitlistRedeemCodeActivity.intent(this))
    }

    private fun openNetP() {
        startActivity(NetworkProtectionManagementActivity.intent(this))
        finish()
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, NetPWaitlistActivity::class.java)
        }
    }
}
