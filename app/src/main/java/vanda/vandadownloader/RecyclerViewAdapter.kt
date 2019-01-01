package vanda.vandadownloader

import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    val list = ArrayList<ItemData>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        p0.item?.title(list[p1].title)
        p0.item?.speed(list[p1].speed)
        p0.item?.progress(list[p1].progress)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerViewAdapter.MyViewHolder {
        val item = ViewItem(p0.context)
        return MyViewHolder(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: ArrayList<ItemData>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun getItemData(index: Int): ItemData {
        return list[index]
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var item: ViewItem? = null
        init {
            item = view as ViewItem
        }
    }
}