package com.example.readiumandroidtestapp.core.data.gateway

import com.example.readiumandroidtestapp.core.domain.gateway.UrlGateway
import org.readium.r2.shared.util.AbsoluteUrl
import javax.inject.Inject

class DefaultUrlGateway @Inject constructor() : UrlGateway {
    override fun parseAbsoluteUrl(url: String): AbsoluteUrl? {
        return AbsoluteUrl.Companion(url)
    }
}
