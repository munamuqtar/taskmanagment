package com.example.lifeflow.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifeflow.core.theme.PurplePrimary
import com.example.lifeflow.core.theme.PurpleLight

@Composable
fun LineChart(
    weeklyData: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (weeklyData.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No activity data for this week",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxCount = (weeklyData.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (weeklyData.size - 1).coerceAtLeast(1)

                val points = weeklyData.mapIndexed { index, pair ->
                    val x = index * spacing
                    val y = height - ((pair.second.toFloat() / maxCount) * (height - 30.dp.toPx())) - 15.dp.toPx()
                    Offset(x, y)
                }

                // Draw background grid lines
                for (i in 0..3) {
                    val yLine = height * (i / 3f)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, yLine),
                        end = Offset(width, yLine),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw connecting path
                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                }

                drawPath(
                    path = path,
                    color = PurplePrimary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw point markers
                points.forEach { point ->
                    drawCircle(
                        color = PurplePrimary,
                        radius = 5.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyData.forEach { (day, count) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
