package codes.chrishorner.socketweather.switch_location

import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.LocationSelection
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

class SwitchLocationViewModel(private val locationChoices: LocationChoices) {

  private val closeEvents = BroadcastChannel<Unit>(1)
  private val scope = MainScope()

  /**
   * Create a list where the current selection is at the beginning.
   */
  fun getOrderedSelections(): List<LocationSelection> {
    val selections: Set<LocationSelection> = locationChoices.getSavedSelections()
    val currentSelection: LocationSelection = locationChoices.getCurrentSelection()
    return selections.sortedByDescending { it == currentSelection }
  }

  fun observeCloseEvents(): Flow<Unit> = closeEvents.asFlow()

  fun select(selection: LocationSelection) {
    scope.launch {
      locationChoices.select(selection)
      closeEvents.send(Unit)
    }
  }

  fun destroy() {
    scope.cancel()
  }
}
