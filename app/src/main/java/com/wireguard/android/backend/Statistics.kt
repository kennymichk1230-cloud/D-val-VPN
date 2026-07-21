package com.wireguard.android.backend

class Statistics {
    private val rx: Long
    private val tx: Long

    constructor(rx: Long, tx: Long) {
        this.rx = rx
        this.tx = tx
    }

    fun getRx(): Long = rx
    fun getTx(): Long = tx
}
