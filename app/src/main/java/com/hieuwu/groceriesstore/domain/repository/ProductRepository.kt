package com.hieuwu.groceriesstore.domain.repository

import com.hieuwu.groceriesstore.domain.entities.LineItem
import com.hieuwu.groceriesstore.domain.entities.Product
import com.hieuwu.groceriesstore.domain.entities.ProductAndLineItem
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getById(id: String): Flow<Product>
    suspend fun getAllProducts(): Flow<List<Product>>
    suspend fun getAllProductsByCategory(categoryId: String): Flow<List<Product>>
    suspend fun getFromServer()
    suspend fun hasProduct(): Boolean
    suspend fun updateProductAndLineItem(lineItem: ProductAndLineItem)
    suspend fun removeProductAndLineItem(lineItem: LineItem)
    suspend fun getAllLineItem(): Flow<List<ProductAndLineItem>>
    suspend fun getLineItemInOrder(orderId:String): Flow<List<ProductAndLineItem>>
}