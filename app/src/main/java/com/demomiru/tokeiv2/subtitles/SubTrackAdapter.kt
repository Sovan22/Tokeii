package com.demomiru.tokeiv2.subtitles


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView

import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.R

data class SubtitleConfig(
    val subConfig : SingleSampleMediaSource,
    var isChecked : Boolean = false
)

class SubTrackAdapter(private val subList: List<SubtitleConfig>, private val title : String,
private val onClick : (SubtitleConfig) -> Unit
                      ): RecyclerView.Adapter<SubTrackAdapter.ViewHolder> (){
    private var selectedPosition = 0

    private fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val subTitle : CheckedTextView = view.findViewById(R.id.sub_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sub_view,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return subList.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sub = subList[position]
        holder.subTitle.text = "$title Subtitle ${position + 1}"
        holder.subTitle.isChecked = position == selectedPosition

        holder.itemView.setOnClickListener {
            setSelectedPosition(holder.bindingAdapterPosition)
            onClick(sub)
        }
    }

}