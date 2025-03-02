package com.stripe.android.paymentsheet.example.samples.ui.server_side_confirm.complete_flow

import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.example.samples.model.CartState
import com.stripe.android.paymentsheet.model.PaymentOption

data class ServerSideConfirmationCompleteFlowViewState(
    val isProcessing: Boolean = false,
    val isError: Boolean = false,
    val confirmedCartState: CartState = CartState.default,
    val dirtyCartState: CartState? = null,
    val status: String? = null,
    val didComplete: Boolean = false,
) {

    val cartState: CartState
        get() = dirtyCartState ?: confirmedCartState

    val paymentSheetConfig: PaymentSheet.Configuration
        get() = PaymentSheet.Configuration(
            merchantDisplayName = "Example, Inc.",
            customer = null,
            googlePay = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                countryCode = "US",
            ),
            // Set `allowsDelayedPaymentMethods` to true if your business can handle payment
            // methods that complete payment after a delay, like SEPA Debit and Sofort.
            allowsDelayedPaymentMethods = true,
        )

    val isBuyButtonEnabled: Boolean
        get() = dirtyCartState == null && !isProcessing && !isError
}
