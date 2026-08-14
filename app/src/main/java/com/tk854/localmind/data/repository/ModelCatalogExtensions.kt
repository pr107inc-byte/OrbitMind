package com.tk854.localmind.data.repository

import com.tk854.localmind.domain.model.ModelCatalogItem

fun ModelCatalogItem.stopTokensJson(): String {
    if (stopTokens.isEmpty()) return "[]"
    val escaped = stopTokens
        .map { token -> "\"${token.replace("\"", "\\\"")}\"" }
        .joinToString(separator = ",")
    return "[$escaped]"
}
