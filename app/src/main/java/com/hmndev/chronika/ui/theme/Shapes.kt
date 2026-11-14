package com.hmndev.chronika.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Shapes
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.abs

val ChronikaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(42.dp)
)

/**
 * Custom [Shape] backed by an AndroidX [Morph] between two [RoundedPolygon]s.
 * Allows smooth M3 Expressive polygon-to-polygon morphing and rotation inside
 * standard `clip(...)`, `background(...)`, and `Surface(shape = ...)` calls.
 */
class MorphPolygonShape(
    private val morph: Morph,
    private val progress: Float,
    private val startAngle: Int = 0,
    private val rotationDegrees: Float = 0f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = morph.toPath(
            progress = progress.coerceIn(0f, 1f),
            startAngle = startAngle
        )
        val matrix = Matrix()
        matrix.scale(size.width, size.height)
        path.transform(matrix)
        val bounds = path.getBounds()
        path.translate(size.center - bounds.center)
        if (rotationDegrees != 0f) {
            matrix.resetToPivotedTransform(
                pivotX = size.center.x,
                pivotY = size.center.y,
                rotationZ = rotationDegrees
            )
            path.transform(matrix)
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MorphPolygonShape) return false
        return morph === other.morph &&
            progress == other.progress &&
            startAngle == other.startAngle &&
            rotationDegrees == other.rotationDegrees
    }

    override fun hashCode(): Int {
        var result = System.identityHashCode(morph)
        result = 31 * result + progress.hashCode()
        result = 31 * result + startAngle
        result = 31 * result + rotationDegrees.hashCode()
        return result
    }
}

@Composable
fun rememberMorphPolygonShape(
    start: RoundedPolygon,
    end: RoundedPolygon,
    progress: Float,
    startAngle: Int = 0,
    rotationDegrees: Float = 0f
): Shape {
    val morph = remember(start, end) { Morph(start, end) }
    return remember(morph, progress, startAngle, rotationDegrees) {
        MorphPolygonShape(
            morph = morph,
            progress = progress,
            startAngle = startAngle,
            rotationDegrees = rotationDegrees
        )
    }
}

/**
 * M3 Expressive grouped-list item shape: outer items carry generous outer corners,
 * inner edges stay tightly tucked to visually connect segmented containers.
 */
fun segmentedItemShape(
    index: Int,
    count: Int,
    outer: Dp = 28.dp,
    inner: Dp = 8.dp
): RoundedCornerShape {
    if (count <= 1) return RoundedCornerShape(outer)
    return when (index) {
        0 -> RoundedCornerShape(
            topStart = outer,
            topEnd = outer,
            bottomStart = inner,
            bottomEnd = inner
        )
        count - 1 -> RoundedCornerShape(
            topStart = inner,
            topEnd = inner,
            bottomStart = outer,
            bottomEnd = outer
        )
        else -> RoundedCornerShape(inner)
    }
}

/**
 * Organic asymmetric corner shapes so adjacent cards never look like cloned rectangles.
 */
fun asymmetricExpressiveShape(
    seed: Int,
    compact: Boolean = false
): RoundedCornerShape {
    val large = if (compact) 26.dp else 34.dp
    val mid = if (compact) 18.dp else 24.dp
    val tight = if (compact) 10.dp else 13.dp
    return when (abs(seed) % 4) {
        0 -> RoundedCornerShape(topStart = large, topEnd = mid, bottomEnd = large, bottomStart = tight)
        1 -> RoundedCornerShape(topStart = mid, topEnd = large, bottomEnd = tight, bottomStart = large)
        2 -> RoundedCornerShape(topStart = large, topEnd = tight, bottomEnd = mid, bottomStart = large)
        else -> RoundedCornerShape(topStart = tight, topEnd = large, bottomEnd = large, bottomStart = mid)
    }
}

private val ExpressiveAppPolygons: List<RoundedPolygon> by lazy {
    listOf(
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Cookie6Sided,
        MaterialShapes.Cookie7Sided,
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Clover4Leaf,
        MaterialShapes.Pentagon,
        MaterialShapes.Sunny,
        MaterialShapes.PuffyDiamond,
        MaterialShapes.Bun,
        MaterialShapes.Fan,
        MaterialShapes.Arch,
        MaterialShapes.Gem
    )
}

fun appPolygon(packageName: String): RoundedPolygon {
    val index = abs(packageName.hashCode()) % ExpressiveAppPolygons.size
    return ExpressiveAppPolygons[index]
}
