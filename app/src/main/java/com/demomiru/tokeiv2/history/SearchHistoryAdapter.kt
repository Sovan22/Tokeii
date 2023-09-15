package com.demomiru.tokeiv2.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.TVshow


class SearchHistoryAdapter(private val searchHistory : List<String>,
                           private val clickHandler : (String) -> Unit
//                           private val deleteRecordClick : (String) -> Unit

):
RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>()
{
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val searchText:TextView  = view.findViewById(R.id.search_text)
        val deleteRecord: ImageView = view.findViewById(R.id.delete_record)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return searchHistory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.searchText.text = searchHistory[position]
        holder.deleteRecord.setOnClickListener {
            clickHandler(searchHistory[position])
        }
        holder.itemView.setOnClickListener {
            clickHandler(searchHistory[position])
        }
    }
}