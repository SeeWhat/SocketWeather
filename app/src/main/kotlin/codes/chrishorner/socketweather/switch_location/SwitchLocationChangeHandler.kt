package codes.chrishorner.socketweather.switch_location

import android.transition.Fade
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.util.addTransition
import codes.chrishorner.socketweather.util.transitionSet
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.Scale

class SwitchLocationChangeHandler : TransitionChangeHandler() {

  override fun getTransition(container: ViewGroup, from: View?, to: View?, isPush: Boolean) = transitionSet {

    val cardScale = Scale()
    cardScale.incomingStartScale = 0.98f
    val cardFadeAndScale = MaterialFade.create(container.context, isPush)
    cardFadeAndScale.secondaryTransition = cardScale

    addTransition(cardFadeAndScale) {
      addTarget(R.id.switchLocation_card)
    }

    addTransition(Fade()) {
      addTarget(R.id.switchLocation_scrim)
      duration = cardFadeAndScale.duration
    }
  }

  override fun removesFromViewOnPush() = false
}
