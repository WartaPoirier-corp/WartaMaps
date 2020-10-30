package org.eu.wp_corp.maps.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.florent37.expansionpanel.ExpansionHeader
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.eu.wp_corp.maps.Entity
import org.eu.wp_corp.maps.R
import org.eu.wp_corp.maps.backend
import org.eu.wp_corp.maps.network.Message


class CardPagerAdapter : RecyclerView.Adapter<CardPagerAdapter.CardViewHolder>() {

    var activeCollectionName = ""
        set(value) {
            field = value
            reloadActiveCollection()
        }

    var activeCollection = mutableListOf<Entity>()

    private val collections = mutableMapOf<String, MutableList<Entity>>()

    fun init(ctx: Context) {
        ctx.backend
            .getStream()
            .observeOn(AndroidSchedulers.mainThread())
            .forEach { msg ->
                when (msg) {
                    is Message.Full -> {
                        collections.putAll(msg.collections)
                        reloadActiveCollection()
                    }
                    is Message.CollectionAdd -> {
                        val col = collections["pedibus"]!!
                        col.add(msg.change.first, msg.change.second)
                        notifyItemInserted(msg.change.first)
                    }
                    is Message.CollectionRemove -> {
                        val col = collections["pedibus"]!!
                        col.removeAt(msg.change.first)
                        notifyItemRemoved(msg.change.first)
                    }
                    is Message.CollectionSet -> {
                        val col = collections["pedibus"]!!
                        col[msg.change.first] = msg.change.second
                        notifyItemChanged(msg.change.first)
                    }
                }
            }
    }

    private fun reloadActiveCollection() {
        notifyItemRangeRemoved(0, itemCount)
        activeCollection = collections[activeCollectionName] ?: mutableListOf()
        notifyItemRangeInserted(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false).apply {
                val header = findViewById<ExpansionHeader>(R.id.expansion_header)
                header.setExpansionLayoutId(R.id.expansion_layout)
                header.setExpansionHeaderIndicator(findViewById(R.id.expansion_chevron))
            })
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.text = activeCollection[position].toString()
    }

    override fun getItemCount(): Int = activeCollection.size

    class CardViewHolder(private val iv: View) : RecyclerView.ViewHolder(iv) {
        var text = ""
            set(value) {
                iv.findViewById<TextView>(R.id.text).text = value
                field = value
            }
    }

}
