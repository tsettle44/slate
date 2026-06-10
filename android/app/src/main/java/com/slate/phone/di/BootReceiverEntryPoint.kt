package com.slate.phone.di

import com.slate.phone.policy.PolicyEnforcer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootReceiverEntryPoint {
    fun policyEnforcer(): PolicyEnforcer
}
