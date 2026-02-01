package com.example.readiumandroidtestapp.core.domain.gateway

import org.readium.r2.shared.util.AbsoluteUrl

interface UrlGateway {
    fun parseAbsoluteUrl(url: String): AbsoluteUrl?
}
