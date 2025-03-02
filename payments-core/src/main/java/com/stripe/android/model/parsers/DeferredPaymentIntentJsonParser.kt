package com.stripe.android.model.parsers

import androidx.annotation.RestrictTo
import com.stripe.android.core.model.StripeJsonUtils.optString
import com.stripe.android.core.model.parsers.ModelJsonParser
import com.stripe.android.core.model.parsers.ModelJsonParser.Companion.jsonArrayToList
import com.stripe.android.model.DeferredIntentParams
import com.stripe.android.model.PaymentIntent
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DeferredPaymentIntentJsonParser(
    private val elementsSessionId: String?,
    private val params: DeferredIntentParams,
    private val apiKey: String,
    private val timeProvider: () -> Long
) : ModelJsonParser<PaymentIntent> {
    override fun parse(json: JSONObject): PaymentIntent {
        val paymentMethodTypes = jsonArrayToList(
            json.optJSONArray(FIELD_PAYMENT_METHOD_TYPES)
        )

        val unactivatedPaymentMethods = jsonArrayToList(
            json.optJSONArray(FIELD_UNACTIVATED_PAYMENT_METHOD_TYPES)
        )

        val linkFundingSources = jsonArrayToList(json.optJSONArray(FIELD_LINK_FUNDING_SOURCES))
            .map { it.lowercase() }

        val countryCode = optString(json, FIELD_COUNTRY_CODE)

        val captureMethod = when (params.captureMethod) {
            DeferredIntentParams.CaptureMethod.Manual -> PaymentIntent.CaptureMethod.Manual
            DeferredIntentParams.CaptureMethod.Automatic -> PaymentIntent.CaptureMethod.Automatic
            null -> PaymentIntent.CaptureMethod.Automatic
        }

        return PaymentIntent(
            id = elementsSessionId,
            clientSecret = null,
            paymentMethodTypes = paymentMethodTypes,
            captureMethod = captureMethod,
            countryCode = countryCode,
            linkFundingSources = linkFundingSources,
            unactivatedPaymentMethods = unactivatedPaymentMethods,
            isLiveMode = apiKey.contains("live"),
            created = timeProvider(),
            setupFutureUsage = params.setupFutureUsage,
            amount = (params.mode as DeferredIntentParams.Mode.Payment).amount,
            currency = params.mode.currency
        )
    }

    private companion object {
        private const val FIELD_COUNTRY_CODE = "country_code"
        private const val FIELD_PAYMENT_METHOD_TYPES = "payment_method_types"
        private const val FIELD_UNACTIVATED_PAYMENT_METHOD_TYPES = "unactivated_payment_method_types"
        private const val FIELD_LINK_FUNDING_SOURCES = "link_funding_sources"
    }
}
