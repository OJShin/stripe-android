package com.stripe.android.ui.core.elements

import android.content.Context
import androidx.annotation.RestrictTo
import com.stripe.android.uicore.elements.IdentifierSpec
import com.stripe.android.uicore.elements.SectionFieldErrorController
import com.stripe.android.uicore.elements.SectionMultiFieldElement
import com.stripe.android.uicore.elements.convertTo4DigitDate
import com.stripe.android.uicore.forms.FormFieldEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * This is the element that represent the collection of all the card details:
 * card number, expiration date, and CVC.
 */
internal class CardDetailsElement(
    identifier: IdentifierSpec,
    context: Context,
    initialValues: Map<IdentifierSpec, String?>,
    viewOnlyFields: Set<IdentifierSpec> = emptySet(),
    private val collectName: Boolean = false,
    val controller: CardDetailsController = CardDetailsController(
        context,
        initialValues,
        viewOnlyFields.contains(IdentifierSpec.CardNumber),
        collectName,
    )
) : SectionMultiFieldElement(identifier) {
    val isCardScanEnabled = controller.numberElement.controller.cardScanEnabled

    override fun sectionFieldErrorController(): SectionFieldErrorController =
        controller

    override fun setRawValue(rawValuesMap: Map<IdentifierSpec, String?>) {
        // Nothing from FormArguments to populate
    }

    override fun getTextFieldIdentifiers(): Flow<List<IdentifierSpec>> =
        MutableStateFlow(
            listOfNotNull(
                controller.nameElement?.identifier,
                controller.numberElement.identifier,
                controller.expirationDateElement.identifier,
                controller.cvcElement.identifier
            )
        )

    override fun getFormFieldValueFlow(): Flow<List<Pair<IdentifierSpec, FormFieldEntry>>> {
        val flows = buildList {
            if (controller.nameElement != null) {
                add(
                    controller.nameElement.controller.formFieldValue.map {
                        controller.nameElement.identifier to it
                    }
                )
            }
            add(
                controller.numberElement.controller.formFieldValue.map {
                    controller.numberElement.identifier to it
                }
            )
            add(
                controller.cvcElement.controller.formFieldValue.map {
                    controller.cvcElement.identifier to it
                }
            )
            add(
                controller.numberElement.controller.cardBrandFlow.map {
                    IdentifierSpec.CardBrand to FormFieldEntry(it.code, true)
                }
            )
            add(
                controller.expirationDateElement.controller.formFieldValue.map {
                    IdentifierSpec.CardExpMonth to getExpiryMonthFormFieldEntry(it)
                }
            )
            add(
                controller.expirationDateElement.controller.formFieldValue.map {
                    IdentifierSpec.CardExpYear to getExpiryYearFormFieldEntry(it)
                }
            )
        }
        return combine(flows) { it.toList() }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun createExpiryDateFormFieldValues(
    entry: FormFieldEntry
): Map<IdentifierSpec, FormFieldEntry> {
    return mapOf(
        IdentifierSpec.CardExpMonth to getExpiryMonthFormFieldEntry(entry),
        IdentifierSpec.CardExpYear to getExpiryYearFormFieldEntry(entry),
    )
}

private fun getExpiryMonthFormFieldEntry(entry: FormFieldEntry): FormFieldEntry {
    var month = -1
    entry.value?.let { date ->
        val newString = convertTo4DigitDate(date)
        if (newString.length == 4) {
            month = requireNotNull(newString.take(2).toIntOrNull())
        }
    }

    return entry.copy(
        value = month.toString().padStart(length = 2, padChar = '0')
    )
}

private fun getExpiryYearFormFieldEntry(entry: FormFieldEntry): FormFieldEntry {
    var year = -1
    entry.value?.let { date ->
        val newString = convertTo4DigitDate(date)
        if (newString.length == 4) {
            year = requireNotNull(newString.takeLast(2).toIntOrNull()) + 2000
        }
    }

    return entry.copy(
        value = year.toString()
    )
}
