package com.stripe.android.ui.core.elements

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.stripe.android.ui.core.R
import com.stripe.android.uicore.elements.DateConfig
import com.stripe.android.uicore.elements.IdentifierSpec
import com.stripe.android.uicore.elements.RowController
import com.stripe.android.uicore.elements.RowElement
import com.stripe.android.uicore.elements.SectionFieldComposable
import com.stripe.android.uicore.elements.SectionFieldElement
import com.stripe.android.uicore.elements.SectionFieldErrorController
import com.stripe.android.uicore.elements.SimpleTextElement
import com.stripe.android.uicore.elements.SimpleTextFieldConfig
import com.stripe.android.uicore.elements.SimpleTextFieldController
import kotlinx.coroutines.flow.combine
import java.util.UUID

internal class CardDetailsController constructor(
    context: Context,
    initialValues: Map<IdentifierSpec, String?>,
    cardNumberReadOnly: Boolean = false,
    collectName: Boolean = false,
) : SectionFieldErrorController, SectionFieldComposable {

    val nameElement = if (collectName) {
        SimpleTextElement(
            controller = SimpleTextFieldController(
                textFieldConfig = SimpleTextFieldConfig(
                    label = R.string.name_on_card,
                    capitalization = KeyboardCapitalization.Words,
                    keyboard = androidx.compose.ui.text.input.KeyboardType.Text
                ),
                initialValue = initialValues[IdentifierSpec.Name],
            ),
            identifier = IdentifierSpec.Name,
        )
    } else {
        null
    }

    val label: Int? = null
    val numberElement = CardNumberElement(
        IdentifierSpec.CardNumber,
        if (cardNumberReadOnly) {
            CardNumberViewOnlyController(
                CardNumberConfig(),
                initialValues
            )
        } else {
            CardNumberEditableController(
                CardNumberConfig(),
                context,
                initialValues[IdentifierSpec.CardNumber]
            )
        }
    )

    val cvcElement = CvcElement(
        IdentifierSpec.CardCvc,
        CvcController(
            CvcConfig(),
            numberElement.controller.cardBrandFlow,
            initialValue = initialValues[IdentifierSpec.CardCvc]
        )
    )

    val expirationDateElement = SimpleTextElement(
        IdentifierSpec.Generic("date"),
        SimpleTextFieldController(
            DateConfig(),
            initialValue = initialValues[IdentifierSpec.CardExpMonth] +
                initialValues[IdentifierSpec.CardExpYear]?.takeLast(2)
        )
    )

    private val rowFields = listOf(expirationDateElement, cvcElement)
    val fields = listOfNotNull(
        nameElement,
        numberElement,
        RowElement(
            IdentifierSpec.Generic("row_" + UUID.randomUUID().leastSignificantBits),
            rowFields,
            RowController(rowFields)
        )
    )

    override val error = combine(
        listOfNotNull(
            nameElement,
            numberElement,
            expirationDateElement,
            cvcElement
        )
            .map { it.controller }
            .map { it.error }
    ) {
        it.filterNotNull().firstOrNull()
    }

    @Composable
    override fun ComposeUI(
        enabled: Boolean,
        field: SectionFieldElement,
        modifier: Modifier,
        hiddenIdentifiers: Set<IdentifierSpec>,
        lastTextFieldIdentifier: IdentifierSpec?,
        nextFocusDirection: FocusDirection,
        previousFocusDirection: FocusDirection
    ) {
        CardDetailsElementUI(
            enabled,
            this,
            hiddenIdentifiers,
            lastTextFieldIdentifier
        )
    }
}
