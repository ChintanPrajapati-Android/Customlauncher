package com.example.customlauncher.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.customlauncher.R
import com.example.customlauncher.model.AppInfo
import kotlinx.android.synthetic.main.apps_drawer_row.view.*


class AppAdapter : RecyclerView.Adapter<AppAdapter.AppHolder>(), Filterable {
    private var appsList: ArrayList<AppInfo> = ArrayList()
    private var tempAppsList: ArrayList<AppInfo> = ArrayList()


    var onUninstallApp: ((item: AppInfo) -> Unit)? = null
    var onLaunch: ((item: AppInfo) -> Unit)? = null

    class AppHolder(view: View) : RecyclerView.ViewHolder(view)


    override fun onBindViewHolder(viewHolder: AppHolder, i: Int) {
        viewHolder.itemView.tvName.text = appsList[i].label.toString()
        viewHolder.itemView.ivIcon.setImageDrawable(appsList[i].icon)
        viewHolder.itemView.tvVersion.text = appsList[i].versionName.plus("(" + appsList[i].versionCode).plus(")")
        viewHolder.itemView.tvPackageName.text = appsList[i].packageName
        viewHolder.itemView.setOnClickListener {
            onLaunch?.invoke(appsList[i])
        }
        viewHolder.itemView.setOnLongClickListener {
            onUninstallApp?.invoke(appsList[i])
            true
        }
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        return AppHolder(LayoutInflater.from(parent.context).inflate(R.layout.apps_drawer_row, parent, false))
    }

    fun addApps(app: AppInfo) {
        appsList.add(app)
        tempAppsList.add(app)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return AppFilter()
    }

    fun sort() {
        appsList.sortBy { it.label.toString() }
        notifyDataSetChanged()
    }

    fun removeApp(item: AppInfo) {
        tempAppsList.remove(item)
        if (appsList.contains(item)) {
            appsList.remove(item)
        }
        notifyDataSetChanged()
    }

    fun isInList(packageName: String): Boolean {
        val packageList = tempAppsList.map { it.packageName }
        if (packageList.contains(packageName)) {
            return true
        }
        return false
    }

    inner class AppFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            if (constraint.isNullOrEmpty()) {
                results.count = tempAppsList.size
                results.values = tempAppsList
            } else {
                val alFilter = ArrayList<AppInfo>()
                tempAppsList.forEach {
                    if (it.label?.contains(constraint.toString(), true) == true) {
                        alFilter.add(it)
                    }
                }
                results.count = alFilter.size
                results.values = alFilter
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results?.let {
                appsList = results.values as ArrayList<AppInfo>
                appsList.sortBy { it.label.toString() }
                notifyDataSetChanged()
            }
        }
    }
}