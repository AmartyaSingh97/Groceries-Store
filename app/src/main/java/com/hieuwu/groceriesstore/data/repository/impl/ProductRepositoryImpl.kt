package com.hieuwu.groceriesstore.data.repository.impl

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hieuwu.groceriesstore.data.database.dao.LineItemDao
import com.hieuwu.groceriesstore.data.database.dao.ProductDao
import com.hieuwu.groceriesstore.data.database.entities.Product
import com.hieuwu.groceriesstore.data.database.entities.asDomainModel
import com.hieuwu.groceriesstore.data.repository.ProductRepository
import com.hieuwu.groceriesstore.domain.models.ProductModel
import com.hieuwu.groceriesstore.utilities.CollectionNames
import com.hieuwu.groceriesstore.utilities.convertProductDocumentToEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val lineItemDao: LineItemDao
) : ProductRepository {

    override val products: Flow<List<ProductModel>> =
        productDao.getAll().map {
            it.asDomainModel()
        }

    override suspend fun refreshDatabase() {
        val fireStore = Firebase.firestore
        val productList = mutableListOf<Product>()
        fireStore.collection(CollectionNames.products).get().addOnSuccessListener { result ->
            for (document in result) {
                productList.add(convertProductDocumentToEntity(document))
            }
        }.addOnFailureListener { exception ->
            Timber.w("Error getting documents.$exception")
        }.await()

        withContext(Dispatchers.IO) {
            productDao.insertAll(productList)
        }
    }

    override suspend fun updateLineItemQuantityById(quantity: Int, id: Long) {
        withContext(Dispatchers.IO) {
            lineItemDao.updateQuantityById(quantity, id)
        }
    }

    override suspend fun removeLineItemById(id: Long) {
        withContext(Dispatchers.IO) {
            lineItemDao.removeLineItemById(id)
        }
    }

    override fun searchProductsListByName(name: String?) =
        productDao.searchProductByName(name).map { it.asDomainModel() }

    override fun getAllProductsByCategory(categoryId: String) =
        productDao.getAllByCategory(categoryId).map {
            it.asDomainModel()
        }

    override fun getProductById(productId: String): Flow<ProductModel> {
        val productFlow = productDao.getById(productId)
        return productFlow.map {
            it.asDomainModel()
        }
    }

}
