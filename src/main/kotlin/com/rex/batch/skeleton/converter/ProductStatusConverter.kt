package com.rex.batch.skeleton.converter

import com.rex.batch.skeleton.enums.ProductStatus
import com.rex.batch.skeleton.enums.StringEnumUtil

class ProductStatusConverter : AbstractStringEnumConverter<ProductStatus>(nullable = false, fromValue = StringEnumUtil::fromValue)