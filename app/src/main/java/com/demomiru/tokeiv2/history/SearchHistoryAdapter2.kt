package com.demomiru.tokeiv2.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageView

import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.history.SearchHistory

class SearchHistoryAdapter2(private val onClick :(SearchHistory,Boolean)->Unit):
    ListAdapter<SearchHistory, SearchHistoryAdapter2.ViewHolder>(SearchDiffCallBack) {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val searchText:TextView  = view.findViewById(R.id.search_text)

        val deleteRecord: ImageView = view.findViewById(R.id.delete_record)
        fun bind(query: SearchHistory) {
            searchText.text = query.query
        }
    }
    object SearchDiffCallBack : DiffUtil.ItemCallback<SearchHistory>() {
        override fun areItemsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val query = getItem(position)
        holder.bind(query)
        holder.deleteRecord.setOnClickListener {
            onClick(query,false)
        }
        holder.itemView.setOnClickListener {
            onClick(query,true)
        }
    }
}

