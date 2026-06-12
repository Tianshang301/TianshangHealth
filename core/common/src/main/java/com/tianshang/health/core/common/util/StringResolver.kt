package com.tianshang.health.core.common.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StringResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(resId: Int): String = context.getString(resId)

    fun getString(resId: Int, vararg args: Any): String = context.getString(resId, *args)
}
