package codes.chrishorner.socketweather.home

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.ForecastState
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.Loading
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.Success
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomePresenter.Event.AboutClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.RefreshClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.SwitchLocationClicked
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.itemClicks

class HomePresenter(view: View) {

  private val toolbar: Toolbar = view.findViewById(R.id.home_toolbar)
  private val locationDropdown: View = view.findViewById(R.id.home_locationDropdown)
  private val toolbarTitle: TextView = view.findViewById(R.id.home_toolbarTitle)
  private val toolbarSubtitle: TextView = view.findViewById(R.id.home_toolbarSubtitle)
  private val forecastContainer: View = view.findViewById(R.id.home_forecastContainer)
  private val loading: View = view.findViewById(R.id.home_loading)
  private val secondaryLoading: View = view.findViewById(R.id.home_secondaryLoading)
  private val error: View = view.findViewById(R.id.home_error)
  private val errorMessage: TextView = view.findViewById(R.id.home_errorMessage)
  private val retryButton: View = view.findViewById(R.id.home_retryButton)
  private val timeForecastsView: TimeForecastView = view.findViewById(R.id.home_timeForecasts)
  private val dateForecastsView: DateForecastsView = view.findViewById(R.id.home_dateForecasts)
  private val observationsPresenter = ObservationsPresenter(view.findViewById(R.id.home_observations))

  private val context: Context = view.context

  val events: Flow<Event>

  private var currentState: ForecastState? = null

  init {
    toolbar.updatePaddingWithInsets(left = true, top = true, right = true)
    val scroller: View = view.findViewById(R.id.home_scroller)
    scroller.updatePaddingWithInsets(bottom = true)

    val menuEvents: Flow<Event> = toolbar.itemClicks().map {
      when (it.itemId) {
        R.id.menu_refresh -> RefreshClicked
        R.id.menu_about -> AboutClicked
        else -> throw IllegalArgumentException("Unknown item selected.")
      }
    }

    events = merge(
        locationDropdown.clicks().map { SwitchLocationClicked },
        retryButton.clicks().map { RefreshClicked },
        menuEvents
    )
  }

  fun display(state: ForecastState) {
    toolbarTitle.text = when (state.selection) {
      is LocationSelection.Static -> state.selection.location.name
      is LocationSelection.FollowMe -> state.location?.name ?: context.getString(R.string.home_findingLocation)
      is LocationSelection.None -> throw IllegalArgumentException("Cannot display LocationSelection.None")
    }

    val forecast: Forecast? = state.forecast

    if (forecast != null && (state.loadingStatus == Loading || state.loadingStatus == Success)) {
      forecastContainer.isVisible = true
      observationsPresenter.display(forecast)
      timeForecastsView.display(forecast)
      dateForecastsView.display(forecast)
    } else {
      forecastContainer.isVisible = false
    }

    error.isVisible = state.loadingStatus == LocationFailed || state.loadingStatus == NetworkFailed
    if (state.loadingStatus == LocationFailed) {
      errorMessage.setText(R.string.home_locationError)
    } else if (state.loadingStatus == NetworkFailed) {
      errorMessage.setText(R.string.home_networkError)
    }

    loading.isVisible = state.loadingStatus == Loading && forecast == null
    secondaryLoading.isVisible = state.loadingStatus == Loading && forecast != null

    currentState = state
    updateRefreshTimeText()
  }

  fun updateRefreshTimeText() {
    val state = currentState

    if (state == null || state.loadingStatus == LocationFailed || state.loadingStatus == NetworkFailed) {
      toolbarSubtitle.isVisible = false
      return
    }

    toolbarSubtitle.isVisible = true

    if (state.loadingStatus == Loading) {
      toolbarSubtitle.setText(R.string.home_updatingNow)
    } else if (state.forecast != null) {

      val updateTime = state.forecast.updateTime
      val now = Instant.now()

      if (Duration.between(updateTime, now).toMinutes() > 0) {
        val timeAgoText = DateUtils.getRelativeTimeSpanString(updateTime.toEpochMilli())
        toolbarSubtitle.text = context.getString(R.string.home_lastUpdated, timeAgoText)
      } else {
        toolbarSubtitle.setText(R.string.home_justUpdated)
      }
    }
  }

  enum class Event { SwitchLocationClicked, RefreshClicked, AboutClicked }
}
