package com.demomiru.tokeiv2


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EpisodeAdapter(private val episodeNumber : List<Int>,
private val clickHandler : (Int) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val episodeText : TextView = itemView.findViewById(R.id.episode_no_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_item_viem,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episodeN = episodeNumber[position]
        holder.episodeText.text = "Episode $episodeN"
        holder.itemView.setOnClickListener {
            clickHandler(episodeN)
        }
    }

    override fun getItemCount(): Int {
        return episodeNumber.size
    }
}