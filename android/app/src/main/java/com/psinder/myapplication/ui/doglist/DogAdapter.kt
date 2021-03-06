package com.psinder.myapplication.ui.doglist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.psinder.myapplication.R
import com.psinder.myapplication.network.Psyna

// TODO: move to entity or API


class DogAdapter : RecyclerView.Adapter<DogAdapter.ViewHolder>() {

    var psynasList: List<Psyna> = emptyList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val avatarImageView = itemView.findViewById<ImageView>(R.id.avatarImageView)

        val dogNameTextView = itemView.findViewById<TextView>(R.id.dogNameTextView)

        val dogDescriptionTextView = itemView.findViewById<TextView>(R.id.dogDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_dog, parent, false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.avatarImageView)
            .load(if (psynasList[position].photoLink.isEmpty()) "https://emojigraph.org/media/openmoji/dog-face_1f436.png" else psynasList[position].photoLink)
            .circleCrop()
            .into(holder.avatarImageView)

        holder.dogNameTextView.text = psynasList[position].name
        holder.dogDescriptionTextView.text = psynasList[position].description
    }

    override fun getItemCount(): Int = psynasList.size
}