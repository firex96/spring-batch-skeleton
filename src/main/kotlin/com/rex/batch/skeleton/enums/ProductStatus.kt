package com.rex.batch.skeleton.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ProductStatus(
    @JsonValue
    override val value: String,
) : StringEnum {
    ON_SALE("OnSale"),
    SOLD_OUT("SoldOut"),
    DISCONTINUED("Discontinued"),
    UNKNOWN(""),
}