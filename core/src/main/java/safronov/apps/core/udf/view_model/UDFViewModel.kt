package safronov.apps.core.udf.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import safronov.apps.core.udf.contract.UDF
import safronov.apps.core.udf.view_model.dispatchers.DispatchersList
import safronov.apps.core.udf.wrapper.UDFStateWrapper

abstract class UDFViewModel<S: UDF.State, A: UDF.Action, E: UDF.Effect, EV: UDF.Event>(
    private val dispatchers: DispatchersList = DispatchersList.Base(),
    private val udfStateWrapper: UDFStateWrapper<S>,
): ViewModel() {

    private val actions = MutableStateFlow<MutableList<A>>(mutableListOf())
    private val _events = mutableStateOf<EV?>(null)
    val events: EV? by _events

    protected abstract fun doAction(action: A): ActionResult
    protected abstract fun affect(effect: E): EffectResult

    init {
        actionHandler()
    }

    fun dispatch(action: A) {
        actions.value.add(action)
    }

    private fun actionHandler() = viewModelScope.launch(dispatchers.ui()) {
        actions.collect {
            val async: Deferred<Unit> = async {
                if (isActive && it.isNotEmpty()) {
                    it.forEach {
                        val actionResult = doAction(it)
                        udfStateWrapper.updState(action = actionResult.stateAction)
                        actionResult.events.forEach { ev: EV ->
                            _events.value = ev
                        }
                        actionResult.effects.forEach { e: E ->
                            affect(effect = e).actions.forEach { a: A ->
                                dispatch(action = a)
                            }
                        }
                        removeAction(action = it)
                    }
                }
            }
            async.await()
        }
    }

    private fun removeAction(action: A): Boolean = actions.value.remove(action)

    inner class ActionResult(
        val stateAction: UDFStateWrapper.StateAction,
        val effects: List<E> = listOf(),
        val events: List<EV> = listOf()
    )

    inner class EffectResult(
        val actions: List<A> = listOf()
    )

}