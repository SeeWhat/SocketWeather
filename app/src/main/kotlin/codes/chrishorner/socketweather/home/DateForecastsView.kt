package codes.chrishorner.socketweather.home

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.DateForecast
import codes.chrishorner.socketweather.util.formatAsDegrees
import codes.chrishorner.socketweather.util.inflate
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import java.util.Locale

class DateForecastsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  private var currentForecasts: List<DateForecast>? = null

  init {
    orientation = VERTICAL
  }

  fun display(forecasts: List<DateForecast>) {
    if (forecasts == currentForecasts) return

    removeAllViews()

    for (forecast in forecasts) {
      val view: View = inflate(R.layout.date_forecast_item)
      val title: TextView = view.findViewById(R.id.dateForecastItem_title)
      val highTemp: TextView = view.findViewById(R.id.dateForecastItem_highTemp)
      val lowTemp: TextView = view.findViewById(R.id.dateForecastItem_lowTemp)

      // TODO: Move this formatting into some other structure.
      val date: ZonedDateTime = forecast.date.atZone(ZoneId.systemDefault())
      val titleText = if (date.toLocalDate() == LocalDate.now().plusDays(1)) {
        resources.getString(R.string.home_dateForecastTomorrow)
      } else {
        date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
      }

      title.text = titleText
      highTemp.text = forecast.temp_max.formatAsDegrees(context)
      lowTemp.text = forecast.temp_min?.formatAsDegrees(context) ?: "--"
      addView(view)
    }

    currentForecasts = forecasts
  }
}
