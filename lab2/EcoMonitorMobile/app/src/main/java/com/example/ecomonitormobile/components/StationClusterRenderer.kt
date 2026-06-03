package com.example.ecomonitormobile.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.example.ecomonitormobile.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class StationClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<StationClusterItem>
) : DefaultClusterRenderer<StationClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(
        item: StationClusterItem,
        markerOptions: MarkerOptions
    ) {
        val station = item.getStation()
        val pinRes = when {
            item.hasAlert() -> R.drawable.ic_pin_red
            station.isActive -> R.drawable.ic_pin_green
            else -> R.drawable.ic_pin_inactive
        }
        markerOptions.title(item.title)
            .snippet(item.snippet)
            .icon(bitmapDescriptorFromVector(context, pinRes))
    }

    private fun bitmapDescriptorFromVector(ctx: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(ctx, vectorResId)
        val scale = 1.3f
        val width = ((vectorDrawable?.intrinsicWidth ?: 36) * scale).toInt()
        val height = ((vectorDrawable?.intrinsicHeight ?: 36) * scale).toInt()
        vectorDrawable?.setBounds(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}