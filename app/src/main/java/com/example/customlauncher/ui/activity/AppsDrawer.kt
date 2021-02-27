package com.example.customlauncher.ui.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.customlauncher.R
import com.example.customlauncher.extensions.isSystemApp
import com.example.customlauncher.extensions.launchApp
import com.example.customlauncher.extensions.uninstallApp
import com.example.customlauncher.model.AppInfo
import com.example.customlauncher.ui.adapter.AppAdapter
import kotlinx.android.synthetic.main.apps_drawer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppsDrawer : AppCompatActivity() {
    private var broadcast: AppsDrawer.AppNotifier? = null
    private lateinit var adapter: AppAdapter
    private var item: AppInfo? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apps_drawer)
        adapter = AppAdapter()
        appList.adapter = adapter
        appList.layoutManager = GridLayoutManager(this, 3)
        addApplications()
        broadcast = AppNotifier()
        edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        adapter.onUninstallApp = { item: AppInfo ->
            if (isSystemApp(item.packageName.toString()).not()) {
                this.item = item
                uninstallApp(item.packageName.toString())
            } else {
                Toast.makeText(this, getString(R.string.uninstall_app_error), Toast.LENGTH_LONG).show()
            }
        }
        adapter.onLaunch = {
            edSearch.text = null
            launchApp(it.packageName.toString())
        }
    }


    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addDataScheme("package")
        registerReceiver(broadcast, intentFilter)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun addApplications() {
        GlobalScope.launch {
            val i = Intent(Intent.ACTION_MAIN, null)
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            val allApps = packageManager.queryIntentActivities(i, 0)
            for (ri in allApps) {
                val app = AppInfo()
                app.label = ri.loadLabel(packageManager)
                app.packageName = ri.activityInfo.packageName
                app.icon = ri.activityInfo.loadIcon(packageManager)
                val packageInfo = packageManager.getPackageInfo(ri.activityInfo.packageName, 0)
                app.versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
                app.versionName = packageInfo.versionName
                runOnUiThread {
                    if (adapter.isInList(ri.activityInfo.packageName).not())
                        adapter.addApps(app)
                }
            }
            withContext(Dispatchers.Main) {
                adapter.sort()
            }
        }
    }

    inner class AppNotifier : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_PACKAGE_REMOVED) {
                item?.let {
                    adapter.removeApp(it)
                }
            } else {
                addApplications()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcast)
    }
}