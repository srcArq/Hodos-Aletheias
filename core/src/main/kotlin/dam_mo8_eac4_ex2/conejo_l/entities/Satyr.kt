package dam_mo8_eac4_ex2.conejo_l.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array as GdxArray
import java.lang.Math.signum
import java.util.Random
import kotlin.math.abs

class Satyr(
    startX: Float,
    startY: Float,
    private var isHorizontal: Boolean,
    private val animations: Map<String, Animation<TextureRegion>>,
    speedMultiplier: Float = 1f
) {

    private val r = Random()

    val position = Vector2(startX, startY)
    private val startPosition = Vector2(startX, startY)
    private val velocity = Vector2()
    private var stateTime = 0f

    // Speed scales with the difficulty level (multiplier injected from GameScreen)
    private val SPEED = 40f * speedMultiplier
    private val MAX_PATROL_RANGE = 64f + r.nextFloat() * 64f

    // Foot-level collision box (fakes a sense of depth)
    private val COLLISION_HEIGHT = 8f
    val bounds = Rectangle(startX, startY, 16f, 32f)
    val collisionBounds = Rectangle()

    private var currentAnim: Animation<TextureRegion>? = null

    init {
        if (isHorizontal) {
            velocity.set(SPEED, 0f)
            currentAnim = animations["right"]
        } else {
            velocity.set(0f, SPEED)
            currentAnim = animations["up"]
        }
        updateBounds(position.x, position.y)
    }

    private fun updateBounds(x: Float, y: Float) {
        bounds.setPosition(x, y).setSize(16f, 32f)
        collisionBounds.set(x, y, 16f, COLLISION_HEIGHT)
    }

    fun update(delta: Float, walls: GdxArray<Shape2D>) {
        stateTime += delta
        val oldPosition = position.cpy()

        val futurePosition = oldPosition.cpy().mulAdd(velocity, delta)

        updateBounds(futurePosition.x, futurePosition.y)
        var collisionDetected = false

        for (wall in walls) {
            if (wall is Rectangle && Intersector.overlaps(collisionBounds, wall)) {
                collisionDetected = true
                break
            }
        }

        if (collisionDetected) {
            val separationDistance = 0.5f

            position.set(oldPosition)
            velocity.scl(-1f)

            if (velocity.x != 0f) {
                position.x += separationDistance * signum(velocity.x)
            } else if (velocity.y != 0f) {
                position.y += separationDistance * signum(velocity.y)
            }

        } else {
            position.set(futurePosition)
        }

        val currentOffset = if (isHorizontal) abs(position.x - startPosition.x) else abs(position.y - startPosition.y)

        if (currentOffset > MAX_PATROL_RANGE) {
            val overshoot = currentOffset - MAX_PATROL_RANGE

            val directionFactor = if (isHorizontal) signum(velocity.x) else signum(velocity.y)

            if (isHorizontal) {
                position.x -= overshoot * directionFactor
            } else {
                position.y -= overshoot * directionFactor
            }

            velocity.scl(-1f)
        }

        updateAnimation()
    }

    private fun updateAnimation() {
        if (velocity.x > 0) currentAnim = animations["right"]
        else if (velocity.x < 0) currentAnim = animations["left"]
        else if (velocity.y > 0) currentAnim = animations["up"]
        else if (velocity.y < 0) currentAnim = animations["down"]
        else if (currentAnim == null) currentAnim = animations["down"]
    }

    fun draw(batch: SpriteBatch) {
        currentAnim?.let {
            val frame = it.getKeyFrame(stateTime, true)
            batch.draw(frame, position.x, position.y, 16f, 32f)
        }
    }
}
