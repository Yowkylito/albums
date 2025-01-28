import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PikachuCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw Pikachu's body (a yellow circle)
        drawCircle(
            color = Color.Yellow,
            radius = size.minDimension / 4,
            center = Offset(size.width / 2, size.height / 2)
        )

        // Draw Pikachu's ears (two triangles)
        drawPath(
            path = createTrianglePath(
                Offset(size.width / 2 - 100f, size.height / 2 - 200f),
                Offset(size.width / 2 - 50f, size.height / 2 - 100f),
                Offset(size.width / 2 - 150f, size.height / 2 - 100f)
            ),
            color = Color.Yellow
        )
        drawPath(
            path = createTrianglePath(
                Offset(size.width / 2 + 100f, size.height / 2 - 200f),
                Offset(size.width / 2 + 50f, size.height / 2 - 100f),
                Offset(size.width / 2 + 150f, size.height / 2 - 100f)
            ),
            color = Color.Yellow
        )

        // Draw Pikachu's eyes (two small black circles)
        drawCircle(
            color = Color.Black,
            radius = 20f,
            center = Offset(size.width / 2 - 50f, size.height / 2 - 50f)
        )
        drawCircle(
            color = Color.Black,
            radius = 20f,
            center = Offset(size.width / 2 + 50f, size.height / 2 - 50f)
        )

        // Draw Pikachu's cheeks (two small red circles)
        drawCircle(
            color = Color.Red,
            radius = 30f,
            center = Offset(size.width / 2 - 100f, size.height / 2 + 50f)
        )
        drawCircle(
            color = Color.Red,
            radius = 30f,
            center = Offset(size.width / 2 + 100f, size.height / 2 + 50f)
        )

        // Draw Pikachu's mouth (a curved line)
        drawArc(
            color = Color.Black,
            startAngle = 30f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(size.width / 2 - 50f, size.height / 2 + 50f),
            size = Size(100f, 100f),
            style = Stroke(width = 5f)
        )
    }
}

// Helper function to create a triangle path
fun createTrianglePath(p1: Offset, p2: Offset, p3: Offset): androidx.compose.ui.graphics.Path {
    return androidx.compose.ui.graphics.Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPikachuCanvas() {
    PikachuCanvas()
}
