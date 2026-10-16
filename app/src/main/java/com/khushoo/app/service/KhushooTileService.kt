package com.khushoo.app.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.khushoo.app.KhushooApplication
import com.khushoo.app.R
import com.khushoo.app.data.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class KhushooTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val appContext = applicationContext
        val dataStoreManager = (appContext as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(appContext)

        scope.launch {
            val isCurrentlyActive = dataStoreManager.getIsActiveSync()
            if (isCurrentlyActive) {
                TimerManager.deactivateKhushoo(appContext, "Tile Click")
            } else {
                val (hours, minutes) = dataStoreManager.getTimerDurationSync()
                val totalMinutes = (hours * 60) + minutes
                TimerManager.activateKhushoo(appContext, totalMinutes)
            }
            updateTileState()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val appContext = applicationContext
        val dataStoreManager = (appContext as? KhushooApplication)?.dataStoreManager
            ?: DataStoreManager(appContext)

        val isActive = dataStoreManager.isKhushooActiveFast()
        val endTimestamp = dataStoreManager.getSessionEndTimestampSync()

        tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_khushoo)
        tile.label = getString(R.string.tile_label)

        if (isActive) {
            tile.state = Tile.STATE_ACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (endTimestamp > 0L) {
                    val remainingSecs = ((endTimestamp - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                    val mins = remainingSecs / 60
                    val secs = remainingSecs % 60
                    tile.subtitle = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                } else {
                    tile.subtitle = getString(R.string.tile_active_desc)
                }
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = getString(R.string.tile_inactive_desc)
            }
        }

        tile.updateTile()
    }
}
