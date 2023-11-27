package com.demomiru.tokeiv2.tracks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.R


data class Track(val id: Int, val format: String, val resolution: Pair<Int, Int>, var selected: Boolean)

class TrackAdapter(private val tracks: List<Track>, private val onTrackSelected: (Track) -> Unit) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

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

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        if (track.format != "Auto")
            holder.trackResolution.text = "${track.resolution.first} x ${track.resolution.second}"
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
