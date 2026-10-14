package com.rex.batch.skeleton.converter

import com.rex.batch.skeleton.enums.ProductStatus
import com.rex.batch.skeleton.enums.StringEnumUtil

class ProductStatusReadingConverter : AbstractStringEnumReadingConverter<ProductStatus>(StringEnumUtil::fromValue)