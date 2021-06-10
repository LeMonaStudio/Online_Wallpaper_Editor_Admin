package com.thenativecitizens.onlinewallpapereditoradmin.ui.upload


import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.ListImageUploadingViewBinding
import com.thenativecitizens.onlinewallpapereditoradmin.util.UploadedImage

class ImageUploadListAdapter: RecyclerView.Adapter<ImageUploadListAdapter.ViewHolder>(){

    var data = listOf<UploadedImage>()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ctx = holder.itemView.context
        val uploadedImage = data[position]
        holder.bind(uploadedImage, ctx)
    }


    class ViewHolder private constructor(private val binding: ListImageUploadingViewBinding)
        : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListImageUploadingViewBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(uploadedImage: UploadedImage, ctx: Context) {
            binding.imageName.text = uploadedImage.imageName
            binding.uploadProgressText.text = ctx.getString(R.string.uploading_progress, "${uploadedImage.imageUploadProgress.toString()}%")
            val animator = ObjectAnimator.ofInt(binding.uploadProgressIndicator, "progress", uploadedImage.imageUploadProgress )
            animator.duration = 500
            animator.start()
            binding.executePendingBindings()
        }
    }
}