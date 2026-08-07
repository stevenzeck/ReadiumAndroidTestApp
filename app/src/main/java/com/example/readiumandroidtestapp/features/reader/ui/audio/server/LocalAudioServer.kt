package com.example.readiumandroidtestapp.features.reader.ui.audio.server

import android.net.Uri
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondFile
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import timber.log.Timber
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface

class LocalAudioServer(private val port: Int = 8080) {
    private var server: EmbeddedServer<*, *>? = null
    var publication: Publication? = null

    fun start() {
        if (server != null) return
        server = embeddedServer(factory = CIO, port = port) {
            install(plugin = PartialContent)
            install(plugin = CORS) {
                anyHost()
            }
            routing {
                get(path = "/audio") {
                    val path = call.request.queryParameters["path"]
                    if (path != null) {
                        val pub = publication
                        if (pub != null) {
                            val url = Url(path)
                            if (url != null) {
                                val link = Link(href = url)
                                val resource = pub.get(link)
                                val bytes = resource?.read()?.getOrNull()
                                if (bytes != null) {
                                    call.respondBytes(bytes)
                                } else {
                                    call.respond(
                                        status = HttpStatusCode.NotFound,
                                        message = "File not found in publication: $path",
                                    )
                                }
                            } else {
                                call.respond(
                                    status = HttpStatusCode.BadRequest,
                                    message = "Invalid path URL",
                                )
                            }
                        } else {
                            val file = File(path)
                            if (file.exists()) {
                                call.respondFile(file)
                            } else {
                                call.respond(
                                    status = HttpStatusCode.NotFound,
                                    message = "File not found: $path",
                                )
                            }
                        }
                    } else {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Missing path parameter",
                        )
                    }
                }
            }
        }
        server?.start(wait = false)
        Timber.i("Local audio server started on port $port")
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
        Timber.i("Local audio server stopped")
    }

    fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to get local IP address")
        }
        return null
    }

    fun getServerUrl(filePath: String): String? {
        val ip = getLocalIpAddress() ?: return null
        return "http://$ip:$port/audio?path=${Uri.encode(filePath)}"
    }
}
