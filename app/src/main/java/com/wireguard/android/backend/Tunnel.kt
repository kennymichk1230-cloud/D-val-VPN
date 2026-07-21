package com.wireguard.android.backend

interface Tunnel {
    fun getName(): String
    fun onStateChange(newState: State)

    enum class State {
        DOWN,
        TOGGLE,
        UP;

        companion object {
            @JvmStatic
            fun of(value: String): State = valueOf(value)
        }
    }
}
