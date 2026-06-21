package dam_mo8_eac4_ex2.conejo_l.entities

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array as GdxArray
import kotlin.math.abs

// Wraith enemy: CHASES the player (slower than the player, so it can be dodged),
// with wall collision and per-axis sliding. Visually distinct from the satyr.
class Wraith(
    startX: Float,
    startY: Float,
    private val animations: Map<String, Animation<TextureRegion>>
) {
    val position = Vector2(startX, startY)
    val bounds = Rectangle(startX, startY, 16f, 32f)            // body (collision with player/bullets)
    private val feet = Rectangle(startX + 2f, startY, 12f, 8f)  // feet (collision with walls)
    private val SPEED = 46f
    private var stateTime = 0f
    private var currentAnim: Animation<TextureRegion>? = animations["down"]

    private fun sync() {
        bounds.setPosition(position.x, position.y)
        feet.setPosition(position.x + 2f, position.y)
    }

    fun update(delta: Float, walls: GdxArray<Shape2D>, target: Vector2) {
        stateTime += delta
        val dir = Vector2(target).sub(position)
        if (dir.len2() > 0.0001f) dir.nor()
        val step = SPEED * delta

        val oldX = position.x
        position.x += dir.x * step; sync()
        if (hitsWall(walls)) { position.x = oldX; sync() }

        val oldY = position.y
        position.y += dir.y * step; sync()
        if (hitsWall(walls)) { position.y = oldY; sync() }

        currentAnim = if (abs(dir.x) > abs(dir.y)) {
            if (dir.x >= 0f) animations["right"] else animations["left"]
        } else {
            if (dir.y >= 0f) animations["up"] else animations["down"]
        }
    }

    private fun hitsWall(walls: GdxArray<Shape2D>): Boolean {
        for (w in walls) if (w is Rectangle && Intersector.overlaps(feet, w)) return true
        return false
    }

    fun draw(batch: SpriteBatch) {
        val frame = (currentAnim ?: animations["down"])?.getKeyFrame(stateTime, true) ?: return
        batch.draw(frame, position.x, position.y, 16f, 32f)
    }
}
