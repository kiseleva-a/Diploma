package ru.netology.nework.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.button.MaterialButton
import ru.netology.nework.R

fun ImageView.load(url: String, willCrop: Boolean = false, timeout: Int = 10_000) {
    Glide.with(this)
        .load(url)
        .run { if (willCrop) circleCrop()  else this}
        .error(R.drawable.ic_baseline_error_outline_48)
        .placeholder(R.drawable.ic_baseline_downloading_48)
        .timeout(timeout)
        .into(this)
}

fun MaterialButton.loadAvatar(url: String, timeout: Int = 10_000) {
    Glide.with(this)
        .asDrawable()
        .load(url)
        .circleCrop()
        .error(R.drawable.ic_baseline_error_outline_48)
        .placeholder(R.drawable.ic_baseline_downloading_48)
        .timeout(timeout)
        .into(object: SimpleTarget<Drawable>(){
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                this@loadAvatar.icon = resource
            }
        })
}