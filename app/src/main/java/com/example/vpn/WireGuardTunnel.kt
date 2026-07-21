package com.example.vpn

import com.wireguard.android.backend.Tunnel

class WireGuardTunnel(private val name: String) : Tunnel {
    private var state: Tunnel.State = Tunnel.State.DOWN
    private var onStateChangedListener: ((Tunnel.State) -> Unit)? = null

    override fun getName(): String = name

    override fun onStateChange(newState: Tunnel.State) {
        state = newState
        onStateChangedListener?.invoke(newState)
    }

    fun setOnStateChangedListener(listener: (Tunnel.State) -> Unit) {
        this.onStateChangedListener = listener
        listener(state)
    }

    fun getState(): Tunnel.State = state
}
