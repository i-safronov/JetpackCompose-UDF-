package safronov.apps.core.udf.wrapper

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import safronov.apps.core.udf.contract.UDF

abstract class UDFStateWrapper<S: UDF.State>(
    private val initState: S
) {

    private val _state = mutableStateOf(initState)
    val state: S by _state

    fun updState(action: StateAction) {
        val newState = updateState(action = action)
        _state.value = newState
    }

    abstract fun updateState(action: StateAction): S

    sealed class StateAction

}