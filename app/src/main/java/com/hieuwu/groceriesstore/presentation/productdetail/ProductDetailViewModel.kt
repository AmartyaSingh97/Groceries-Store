package com.hieuwu.groceriesstore.presentation.productdetail

import androidx.databinding.Bindable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hieuwu.groceriesstore.BR
import com.hieuwu.groceriesstore.data.database.entities.LineItem
import com.hieuwu.groceriesstore.data.database.entities.Order
import com.hieuwu.groceriesstore.domain.models.OrderModel
import com.hieuwu.groceriesstore.data.repository.OrderRepository
import com.hieuwu.groceriesstore.domain.usecases.GetProductDetailUseCase
import com.hieuwu.groceriesstore.presentation.utils.ObservableViewModel
import com.hieuwu.groceriesstore.utilities.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getProductDetailUseCase: GetProductDetailUseCase,
    private val orderRepository: OrderRepository
) : ObservableViewModel() {

    private val args = ProductDetailFragmentArgs.fromSavedStateHandle(savedStateHandle)

    val product = getProductDetailUseCase.getProductDetail(args.id).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    var currentCart: StateFlow<OrderModel?> =
        orderRepository.getOneOrderByStatus(OrderStatus.IN_CART)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var _qty: Int = 1
    var qty: Int
        @Bindable
        get() = _qty
        set(value) {
            _qty = value
            notifyPropertyChanged(BR.qty)
        }

    private val _showSnackbarEvent = MutableStateFlow(false)
    val showSnackBarEvent: StateFlow<Boolean>
        get() = _showSnackbarEvent.asStateFlow()

    fun addToCart() {
        viewModelScope.launch {
            val subtotal = product.value?.price?.times(qty) ?: 0.0
            if (currentCart.value != null) {
                // Add to cart
                val cartId = currentCart.value!!.id
                val lineItem = LineItem(
                    productId = product.value!!.id,
                    orderId = cartId,
                    quantity = _qty,
                    subtotal = subtotal
                )
                orderRepository.addLineItem(lineItem)
            } else {
                val id = UUID.randomUUID().toString()
                val newOrder = Order(
                    id = id,
                    status = OrderStatus.IN_CART.value,
                    address = ""
                )
                orderRepository.createOrUpdate(newOrder)
                val lineItem = LineItem(
                    productId = product.value!!.id,
                    orderId = id,
                    quantity = _qty,
                    subtotal = subtotal
                )
                orderRepository.addLineItem(lineItem)
            }
            _showSnackbarEvent.value = true
        }
    }

    fun increaseQty() {
        qty++
    }

    fun decreaseQty() {
        if (qty <= 1) return
        qty--
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }
}
