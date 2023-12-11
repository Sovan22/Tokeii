package com.demomiru.tokeiv2.tracks

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.history.SearchHistory
import com.demomiru.tokeiv2.history.SearchHistoryAdapter2
import com.demomiru.tokeiv2.utils.ExtractedData


data class Track(val id: Int, val format: String, val resolution: Pair<String, String>, var selected: Boolean)

class TrackAdapter(private val tracks: List<Track>,private val onTrackSelected: (Track) -> Unit) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trackResolution: CheckedTextView = itemView.findViewById(R.id.track_quality)
    }
    private var selectedPosition = 0

    private fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return TrackViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        if (track.format != "Auto" && track.format!="super")
            holder.trackResolution.text = "${track.resolution.first} x ${track.resolution.second}"
        else if( track.format == "super")
            holder.trackResolution.text = track.resolution.second
        else
            holder.trackResolution.text = "Auto"
        holder.trackResolution.isChecked = position == selectedPosition
        holder.itemView.setOnClickListener {
            setSelectedPosition(holder.bindingAdapterPosition)
            onTrackSelected(track)
        }
    }

    override fun getItemCount() = tracks.size
}

class SourceAdapter(private val onClick :(ExtractedData)->Unit):
    ListAdapter<ExtractedData, SourceAdapter.ViewHolder>(SearchDiffCallBack) {

    private var selectedPosition = 0

    private fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val sourceTv: CheckedTextView = itemView.findViewById(R.id.track_quality)
    }
    object SearchDiffCallBack : DiffUtil.ItemCallback<ExtractedData>() {
        override fun areItemsTheSame(oldItem: ExtractedData, newItem: ExtractedData): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ExtractedData, newItem: ExtractedData): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val source = getItem(position)
        holder.sourceTv.isChecked = position == selectedPosition
        holder.sourceTv.text = "Source : ${source.source}"
        holder.itemView.setOnClickListener {
            setSelectedPosition(holder.bindingAdapterPosition)
            onClick(source)
        }
    }
}
