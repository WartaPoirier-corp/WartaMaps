package org.eu.wp_corp.maps

import com.koushikdutta.async.http.AsyncHttpClient
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.eu.wp_corp.maps.network.Message


class Backend {

    private val sessions: PublishSubject<Session> = PublishSubject.create()
    private val messages =
        sessions.flatMap { it.start() }.doOnError(Throwable::printStackTrace).publish()

    private val full = run {
        // Cached collections
        val collections: MutableMap<String, MutableList<Entity>> = HashMap()

        messages.subscribe { msg ->
            when (msg) {
                is Message.Full -> {
                    collections.putAll(msg.collections)
                }
                is Message.CollectionAdd -> {
                    val col = collections["pedibus"]!!
                    col.add(msg.change.first, msg.change.second)
                }
                is Message.CollectionRemove -> {
                    val col = collections["pedibus"]!!
                    col.removeAt(msg.change.first)
                }
                is Message.CollectionSet -> {
                    val col = collections["pedibus"]!!
                    col[msg.change.first] = msg.change.second
                }
            }
        }

        messages.connect()

        collections
    }

    class Session(private val url: String, private val auth: Unit) {
        fun start(): Observable<Message> {
            return Observable.create { emitter ->
                AsyncHttpClient.getDefaultInstance().websocket(
                    url,
                    ""
                ) { ex, webSocket ->
                    if (ex != null) {
                        ex.printStackTrace()
                        emitter.onError(ex)
                        return@websocket
                    }

                    webSocket.setStringCallback { msg ->
                        emitter.onNext(Message.from(msg))
                    }

                    emitter.setDisposable(object : Disposable {
                        override fun dispose() {
                            webSocket.close()
                        }

                        override fun isDisposed(): Boolean {
                            return !webSocket.isOpen
                        }
                    })
                }
            }
        }
    }

    fun queueSession(url: String, auth: Unit) {
        sessions.onNext(Session(url, auth))
    }

    /**
     * Ideally, this stream is an infinite sequence of messages forwarded directly from what the
     * server sends, that each describe a relatively small change, so that the UI doesn't have to
     * refresh for nothing. However, this stream also have to be 100% reliable, which implies new
     * rules:
     *   * As soon as it is created, a few messages are forged and sent immediately in order to
     *     "fast-forward" to the current cached state, without missing a step, in case the stream is
     *     opened after the websockets client connected (which will probably always be the case)
     *   * If an unrecoverable network error occurs, a "reset" message might be sent. As soon as the
     *     server is reachable again, all its messages will immediately be forwarded.
     */
    fun getStream(): Observable<Message> {
        return Observable.just<Message>(Message.Full(HashMap(full))).concatWith(messages)
    }

}
