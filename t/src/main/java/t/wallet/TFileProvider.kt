package t.wallet

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.FileProvider


internal class TFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        context?.let {
            if (!isAppDebug()) {
                try {
                    EncryptUtils.init(it.applicationContext as Application)
                } catch (t: Throwable) {
                }
            }
        }
        return super.onCreate()
    }


    private fun isAppDebug(): Boolean {
        val packageName = context?.applicationContext?.packageName ?: return false
        return try {
            val pm: PackageManager = context?.packageManager ?: return false
            val ai = pm.getApplicationInfo(packageName, 0)
            ai.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}