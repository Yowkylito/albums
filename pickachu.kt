import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PikachuCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw Pikachu's body (a rounded shape)
        drawPath(
            path = createPikachuBodyPath(centerX, centerY),
            color = Color.Yellow
        )

        // Draw Pikachu's ears
        drawPath(
            path = createPikachuEarPath(centerX - 150f, centerY - 300f),
            color = Color.Yellow
        )
        drawPath(
            path = createPikachuEarPath(centerX + 150f, centerY - 300f, flipped = true),
            color = Color.Yellow
        )

        // Draw Pikachu's tail
        drawPath(
            path = createPikachuTailPath(centerX + 200f, centerY + 100f),
            color = Color.Yellow
        )

        // Draw Pikachu's arms
        drawPath(
            path = createPikachuArmPath(centerX - 120f, centerY + 50f),
            color = Color.Yellow
        )
        drawPath(
            path = createPikachuArmPath(centerX + 120f, centerY + 50f, flipped = true),
            color = Color.Yellow
        )

        // Draw Pikachu's legs
        drawPath(
            path = createPikachuLegPath(centerX - 80f, centerY + 200f),
            color = Color.Yellow
        )
        drawPath(
            path = createPikachuLegPath(centerX + 80f, centerY + 200f),
            color = Color.Yellow
        )

        // Draw Pikachu's eyes
        drawCircle(
            color = Color.Black,
            radius = 20f,
            center = Offset(centerX - 50f, centerY - 50f)
        )
        drawCircle(
            color = Color.Black,
            radius = 20f,
            center = Offset(centerX + 50f, centerY - 50f)
        )

        // Draw Pikachu's cheeks
        drawCircle(
            color = Color.Red,
            radius = 30f,
            center = Offset(centerX - 100f, centerY + 50f)
        )
        drawCircle(
            color = Color.Red,
            radius = 30f,
            center = Offset(centerX + 100f, centerY + 50f)
        )

        // Draw Pikachu's mouth
        drawArc(
            color = Color.Black,
            startAngle = 30f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(centerX - 50f, centerY + 50f),
            size = Size(100f, 100f),
            style = Stroke(width = 5f)
        )
    }
}

// Helper function to create Pikachu's body path
fun createPikachuBodyPath(centerX: Float, centerY: Float): Path {
    return Path().apply {
        moveTo(centerX, centerY - 150f)
        cubicTo(
            centerX - 100f, centerY - 150f,
            centerX - 150f, centerY,
            centerX - 150f, centerY + 150f
        )
        cubicTo(
            centerX - 150f, centerY + 250f,
            centerX - 50f, centerY + 300f,
            centerX, centerY + 300f
        )
        cubicTo(
            centerX + 50f, centerY + 300f,
            centerX + 150f, centerY + 250f,
            centerX + 150f, centerY + 150f
        )
        cubicTo(
            centerX + 150f, centerY,
            centerX + 100f, centerY - 150f,
            centerX, centerY - 150f
        )
        close()
    }
}

// Helper function to create Pikachu's ear path
fun createPikachuEarPath(startX: Float, startY: Float, flipped: Boolean = false): Path {
    return Path().apply {
        moveTo(startX, startY)
        if (flipped) {
            lineTo(startX + 50f, startY + 100f)
            lineTo(startX - 50f, startY + 100f)
        } else {
            lineTo(startX - 50f, startY + 100f)
            lineTo(startX + 50f, startY + 100f)
        }
        close()
    }
}

// Helper function to create Pikachu's tail path
fun createPikachuTailPath(startX: Float, startY: Float): Path {
    return Path().apply {
        moveTo(startX, startY)
        cubicTo(
            startX + 50f, startY - 100f,
            startX + 150f, startY - 50f,
            startX + 200f, startY
        )
        cubicTo(
            startX + 150f, startY + 50f,
            startX + 50f, startY + 100f,
            startX, startY
        )
        close()
    }
}

// Helper function to create Pikachu's arm path
fun createPikachuArmPath(startX: Float, startY: Float, flipped: Boolean = false): Path {
    return Path().apply {
        moveTo(startX, startY)
        if (flipped) {
            cubicTo(
                startX + 50f, startY + 50f,
                startX + 100f, startY + 50f,
                startX + 100f, startY
            )
        } else {
            cubicTo(
                startX - 50f, startY + 50f,
                startX - 100f, startY + 50f,
                startX - 100f, startY
            )
        }
        close()
    }
}

// Helper function to create Pikachu's leg path
fun createPikachuLegPath(startX: Float, startY: Float): Path {
    return Path().apply {
        moveTo(startX, startY)
        cubicTo(
            startX - 50f, startY + 100f,
            startX + 50f, startY + 100f,
            startX, startY
        )
        close()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPikachuCanvas() {
    PikachuCanvas()
}
