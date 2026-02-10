package com.marlow.globals.GlobalModel

import kotlinx.serialization.Serializable

@Serializable
data class globalResponse (
    val code: Int,
    val status: String
)