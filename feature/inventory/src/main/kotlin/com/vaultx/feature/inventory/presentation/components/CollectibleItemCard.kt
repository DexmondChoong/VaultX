package com.vaultx.feature.inventory.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vaultx.core.model.CollectibleCategory
import com.vaultx.core.model.CollectibleItem
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private val PositiveDeltaColor = Color(0xFF16A34A)
private val NegativeDeltaColor = Color(0xFFDC2626)

/**
 * Displays an individual collectible item with its thumbnail artwork monogram/preview,
 * condition grade badge (e.g., PSA 10, CGC 9.8), purchase cost vs current market estimate,
 * and percentage return indicator.
 */
@Composable
fun CollectibleItemCard(
    item: CollectibleItem,
    onClick: (CollectibleItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val profitLoss = item.unrealizedProfitLoss
    val isGain = profitLoss >= 0.0
    val deltaColor = if (isGain) PositiveDeltaColor else NegativeDeltaColor
    val sign = if (profitLoss > 0.0) {
        "+"
    } else if (profitLoss < 0.0) {
        "-"
    } else {
        ""
    }

    OutlinedCard(
        onClick = { onClick(item) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail Preview Box
            CollectibleThumbnail(
                category = item.category,
                name = item.name,
                modifier = Modifier.size(68.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Middle Column: Title, Category & Condition Grade Badge
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Condition Grade Badge (e.g. PSA 10, BGS 9.5)
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = item.conditionGrade,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }

                    Text(
                        text = item.category.displayLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "Cost: ${currencyFormatter.format(item.purchasePrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right Column: Current Market Estimate & Return Delta
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = currencyFormatter.format(item.marketPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = String.format(
                        Locale.US,
                        "%s%s (%s%.1f%%)",
                        sign,
                        currencyFormatter.format(abs(profitLoss)),
                        sign,
                        abs(item.returnPercentage),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = deltaColor,
                )
            }
        }
    }
}

@Composable
private fun CollectibleThumbnail(
    category: CollectibleCategory,
    name: String,
    modifier: Modifier = Modifier,
) {
    val gradientColors = when (category) {
        CollectibleCategory.GradedCard -> listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
        CollectibleCategory.SealedBox -> listOf(Color(0xFF4C1D95), Color(0xFF8B5CF6))
        CollectibleCategory.Comic -> listOf(Color(0xFF7C2D12), Color(0xFFF97316))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush = Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(2).uppercase(Locale.US),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
        )
    }
}

private fun CollectibleCategory.displayLabel(): String = when (this) {
    CollectibleCategory.GradedCard -> "Graded Card"
    CollectibleCategory.SealedBox -> "Sealed Box"
    CollectibleCategory.Comic -> "Comic Book"
}
