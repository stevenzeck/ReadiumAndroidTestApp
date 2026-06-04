package com.example.readiumandroidtestapp.core.designsystem.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                val resolvedArgs = ArrayList<Any>(args.size)
                for (arg in args) {
                    if (arg is UiText) {
                        resolvedArgs.add(arg.asString())
                    } else {
                        resolvedArgs.add(arg)
                    }
                }
                stringResource(id = resId, *resolvedArgs.toTypedArray())
            }
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                val resolvedArgs = args.map { arg ->
                    if (arg is UiText) arg.asString(context) else arg
                }.toTypedArray()
                context.getString(resId, *resolvedArgs)
            }
        }
    }
}
