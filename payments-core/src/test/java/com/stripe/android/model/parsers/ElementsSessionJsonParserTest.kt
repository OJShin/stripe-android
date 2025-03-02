package com.stripe.android.model.parsers

import com.google.common.truth.Truth.assertThat
import com.stripe.android.core.model.parsers.ModelJsonParser
import com.stripe.android.model.DeferredIntentParams
import com.stripe.android.model.ElementsSessionFixtures
import com.stripe.android.model.ElementsSessionParams
import com.stripe.android.model.PaymentIntent
import com.stripe.android.model.SetupIntent
import org.json.JSONObject
import org.junit.Test

class ElementsSessionJsonParserTest {
    @Test
    fun parsePaymentIntent_shouldCreateObjectWithOrderedPaymentMethods() {
        val elementsSession = ElementsSessionJsonParser(
            ElementsSessionParams.PaymentIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            ElementsSessionFixtures.EXPANDED_PAYMENT_INTENT_JSON
        )
        val orderedPaymentMethods =
            ModelJsonParser.jsonArrayToList(
                ElementsSessionFixtures.EXPANDED_PAYMENT_INTENT_JSON
                    .optJSONObject("payment_method_preference")!!
                    .optJSONArray("ordered_payment_method_types")
            )

        assertThat(elementsSession?.stripeIntent?.id)
            .isEqualTo("pi_3JTDhYIyGgrkZxL71IDUGKps")
        assertThat(elementsSession?.stripeIntent?.paymentMethodTypes)
            .containsExactlyElementsIn(orderedPaymentMethods)
            .inOrder()
    }

    @Test
    fun parseSetupIntent_shouldCreateObjectWithOrderedPaymentMethods() {
        val elementsSession = ElementsSessionJsonParser(
            ElementsSessionParams.SetupIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            ElementsSessionFixtures.EXPANDED_SETUP_INTENT_JSON
        )
        val orderedPaymentMethods =
            ModelJsonParser.jsonArrayToList(
                ElementsSessionFixtures.EXPANDED_SETUP_INTENT_JSON
                    .optJSONObject("payment_method_preference")!!
                    .optJSONArray("ordered_payment_method_types")
            )

        assertThat(elementsSession?.stripeIntent?.id)
            .isEqualTo("seti_1JTDqGIyGgrkZxL7reCXkpr5")
        assertThat(elementsSession?.stripeIntent?.paymentMethodTypes)
            .containsExactlyElementsIn(orderedPaymentMethods)
            .inOrder()
    }

    @Test
    fun parsePaymentIntent_shouldCreateObjectLinkFundingSources() {
        val elementsSession = ElementsSessionJsonParser(
            ElementsSessionParams.PaymentIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            ElementsSessionFixtures.EXPANDED_PAYMENT_INTENT_WITH_LINK_FUNDING_SOURCES_JSON
        )!!

        assertThat(elementsSession.stripeIntent?.linkFundingSources)
            .containsExactly("card", "bank_account")
    }

    @Test
    fun parseSetupIntent_shouldCreateObjectLinkFundingSources() {
        val elementsSession = ElementsSessionJsonParser(
            ElementsSessionParams.SetupIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            ElementsSessionFixtures.EXPANDED_SETUP_INTENT_WITH_LINK_FUNDING_SOURCES_JSON
        )!!

        assertThat(elementsSession.stripeIntent?.linkFundingSources)
            .containsExactly("card", "bank_account")
    }

    @Test
    fun `Test ordered payment methods returned in PI payment_method_type variable`() {
        val parsedData = ElementsSessionJsonParser(
            ElementsSessionParams.PaymentIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            JSONObject(
                ElementsSessionFixtures.PI_WITH_CARD_AFTERPAY_AU_BECS
            )
        )
        assertThat(parsedData?.stripeIntent?.paymentMethodTypes).isEqualTo(
            listOf(
                "au_becs_debit",
                "afterpay_clearpay",
                "card"
            )
        )
    }

    @Test
    fun `Test ordered payment methods not required in response`() {
        val parsedData = ElementsSessionJsonParser(
            ElementsSessionParams.PaymentIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            JSONObject(
                ElementsSessionFixtures.PI_WITH_CARD_AFTERPAY_AU_BECS_NO_ORDERED_LPMS
            )
        )
        // This is the order in the original payment intent
        assertThat(parsedData?.stripeIntent?.paymentMethodTypes).isEqualTo(
            listOf(
                "card",
                "afterpay_clearpay",
                "au_becs_debit"
            )
        )
    }

    @Test
    fun `Test ordered payment methods is not required`() {
        val parsedData = ElementsSessionJsonParser(
            ElementsSessionParams.PaymentIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            JSONObject(
                """
                    {
                      "payment_method_preference": {
                         "object": "payment_method_preference",
                         "payment_intent": {
                         }
                      }
                    }
                """.trimIndent()
            )
        )
        assertThat(parsedData?.stripeIntent).isNull()
    }

    @Test
    fun `Test fail to parse the payment intent`() {
        val parsedData = ElementsSessionJsonParser(
            ElementsSessionParams.PaymentIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            JSONObject(
                """
                    {
                      "payment_method_preference": {
                         "object": "payment_method_preference",
                         "payment_intent": {
                         }
                      }
                    }
                """.trimIndent()
            )
        )
        assertThat(parsedData?.stripeIntent).isNull()
    }

    @Test
    fun `Test fail to find the payment intent`() {
        val parsedData = ElementsSessionJsonParser(
            ElementsSessionParams.PaymentIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            JSONObject(
                """
                    {
                      "payment_method_preference": {
                         "object": "payment_method_preference"
                      }
                    }
                """.trimIndent()
            )
        )
        assertThat(parsedData?.stripeIntent).isNull()
    }

    @Test
    fun `Test PI with country code`() {
        val parsedData = ElementsSessionJsonParser(
            ElementsSessionParams.PaymentIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            ElementsSessionFixtures.EXPANDED_PAYMENT_INTENT_JSON
        )

        val countryCode = when (val intent = parsedData?.stripeIntent) {
            is PaymentIntent -> intent.countryCode
            is SetupIntent -> intent.countryCode
            null -> null
        }

        assertThat(countryCode).isEqualTo("US")
    }

    @Test
    fun `Test SI with country code`() {
        val parsedData = ElementsSessionJsonParser(
            ElementsSessionParams.SetupIntentType(
                clientSecret = "secret"
            ),
            apiKey = "test"
        ).parse(
            ElementsSessionFixtures.EXPANDED_SETUP_INTENT_JSON
        )

        val countryCode = when (val intent = parsedData?.stripeIntent) {
            is PaymentIntent -> intent.countryCode
            is SetupIntent -> intent.countryCode
            null -> null
        }

        assertThat(countryCode).isEqualTo("US")
    }

    @Test
    fun `Test deferred PaymentIntent`() {
        val data = ElementsSessionJsonParser(
            ElementsSessionParams.DeferredIntentType(
                deferredIntentParams = DeferredIntentParams(
                    mode = DeferredIntentParams.Mode.Payment(
                        amount = 2000,
                        currency = "usd"
                    )
                )
            ),
            apiKey = "test",
            timeProvider = { 1 }
        ).parse(
            ElementsSessionFixtures.DEFERRED_INTENT_JSON
        )

        val deferredIntent = data?.stripeIntent

        assertThat(deferredIntent).isNotNull()
        assertThat(deferredIntent).isEqualTo(
            PaymentIntent(
                id = "elements_session_1t6ejApXCS5",
                clientSecret = null,
                amount = 2000L,
                currency = "usd",
                captureMethod = PaymentIntent.CaptureMethod.Automatic,
                countryCode = "CA",
                created = 1,
                isLiveMode = false,
                setupFutureUsage = null,
                unactivatedPaymentMethods = listOf(),
                paymentMethodTypes = listOf("card", "link", "cashapp"),
                linkFundingSources = listOf("card")
            )
        )
    }

    @Test
    fun `Test deferred SetupIntent`() {
        val data = ElementsSessionJsonParser(
            ElementsSessionParams.DeferredIntentType(
                deferredIntentParams = DeferredIntentParams(
                    mode = DeferredIntentParams.Mode.Setup(
                        currency = "usd"
                    )
                )
            ),
            apiKey = "test",
            timeProvider = { 1 }
        ).parse(
            ElementsSessionFixtures.DEFERRED_INTENT_JSON
        )

        val deferredIntent = data?.stripeIntent

        assertThat(deferredIntent).isNotNull()
        assertThat(deferredIntent).isEqualTo(
            SetupIntent(
                id = "elements_session_1t6ejApXCS5",
                clientSecret = null,
                cancellationReason = null,
                description = null,
                nextActionData = null,
                paymentMethodId = null,
                paymentMethod = null,
                status = null,
                countryCode = "CA",
                created = 1,
                isLiveMode = false,
                usage = null,
                unactivatedPaymentMethods = listOf(),
                paymentMethodTypes = listOf("card", "link", "cashapp"),
                linkFundingSources = listOf("card")
            )
        )
    }
}
