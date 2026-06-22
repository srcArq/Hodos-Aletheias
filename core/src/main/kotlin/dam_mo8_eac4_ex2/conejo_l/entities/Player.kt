package dam_mo8_eac4_ex2.conejo_l

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array as GdxArray

class Player(startX: Float, startY: Float, private val animations: Map<String, Animation<TextureRegion>>) {

    enum class Direction { UP, DOWN, LEFT, RIGHT }

    // Sprite frame dimensions
    val FRAME_WIDTH = 16
    val FRAME_HEIGHT = 32

    val position = Vector2(startX, startY)

    // Rectangular hitbox (feet: 8x8 px, centered on the sprite)
    val bounds = Rectangle(startX + 4, startY, 8f, 8f)

    val speed = 80f // Movement speed (pixels per second)
    var currentDirection = Direction.DOWN

    // --- HEALTH / DAMAGE ---
    var health: Int = 3
    var lifeScore: Int = 20

    // --- SCORE (only ever increases; it is the run's challenge) ---
    var score: Int = 0

    companion object {
        const val COIN_SCORE = 50
        const val LIFE_SCORE = 25
    }

    // Animation state
    private var stateTime = 0f
    private var isMoving = false

    var isInvulnerable = false
    private var invulnerabilityTimer = 0f
    private val INVULNERABILITY_DURATION = 1.5f

    fun update(delta: Float, inputX: Float, inputY: Float, walls: GdxArray<Shape2D>) {
        stateTime += delta
        var newX = position.x
        var newY = position.y

        val threshold = 0.2f
        isMoving = false

        var velocityX = 0f
        var velocityY = 0f

        // Movement and direction logic
        if (inputX > threshold) {
            velocityX = speed * delta
            currentDirection = Direction.RIGHT
            isMoving = true
        } else if (inputX < -threshold) {
            velocityX = -speed * delta
            currentDirection = Direction.LEFT
            isMoving = true
        }

        if (inputY > threshold) {
            velocityY = speed * delta
            currentDirection = Direction.UP
            isMoving = true
        } else if (inputY < -threshold) {
            velocityY = -speed * delta
            currentDirection = Direction.DOWN
            isMoving = true
        }

        // --- INVULNERABILITY HANDLING ---
        if (isInvulnerable) {
            invulnerabilityTimer -= delta
            if (invulnerabilityTimer <= 0) {
                isInvulnerable = false
            }
        }

        // 1. X-axis collisions
        val oldX = position.x
        newX += velocityX
        // Tentatively move the bounds to test the collision
        bounds.x = newX + 4 // +4: offset from the constructor

        var collisionX = false
        for (wall in walls) {
            if (wall is Rectangle && Intersector.overlaps(bounds, wall)) {
                collisionX = true
                break
            }
            // (add other shape types here if needed)
        }

        if (collisionX) {
            bounds.x = oldX + 4
        } else {
            position.x = newX
        }

        // 2. Y-axis collisions
        val oldY = position.y
        newY += velocityY
        bounds.y = newY

        var collisionY = false
        for (wall in walls) {
            if (wall is Rectangle && Intersector.overlaps(bounds, wall)) {
                collisionY = true
                break
            }
        }

        if (collisionY) {
            bounds.y = oldY
        } else {
            position.y = newY
        }

        // Final bounds update
        bounds.setPosition(position.x + 4, position.y)
    }

    fun applyEffect(type: Collectible.Type) {
        when (type) {
            Collectible.Type.COIN -> {
                score += COIN_SCORE
            }
            Collectible.Type.LIFE -> {
                lifeScore = (lifeScore + 5).coerceAtMost(20)
                score += LIFE_SCORE
            }
        }
    }

    fun takeDamage(damage: Int) {
        if (!isInvulnerable) {
            health -= damage
            isInvulnerable = true
            invulnerabilityTimer = INVULNERABILITY_DURATION
            Gdx.app.log("PLAYER", "Hit! Health: $health")
        }
    }

    // Grants a fixed window of invulnerability (used for the spawn-grace period).
    fun grantInvulnerability(seconds: Float) {
        isInvulnerable = true
        invulnerabilityTimer = seconds
    }

    fun getMuzzlePosition(): Vector2 {
        val centerX = position.x + 16f / 2 // FRAME_WIDTH / 2
        val centerY = position.y + 32f / 2 // FRAME_HEIGHT / 2
        return Vector2(centerX, centerY)
    }

    fun draw(batch: SpriteBatch) {
        // Build the animation key from movement state + direction
        val type = if (isMoving) "walk" else "idle"
        val dir = currentDirection.toString().lowercase()
        val key = "${type}_${dir}" // e.g. "walk_down", "idle_right"

        val animation = animations[key]

        if (animation != null) {
            val currentFrame = animation.getKeyFrame(stateTime, true)
            // Blink while invulnerable: visual feedback for damage taken
            if (isInvulnerable) {
                val visible = ((invulnerabilityTimer * 12f).toInt() % 2) == 0
                batch.setColor(1f, 1f, 1f, if (visible) 1f else 0.25f)
                batch.draw(currentFrame, position.x, position.y)
                batch.setColor(1f, 1f, 1f, 1f)
            } else {
                batch.draw(currentFrame, position.x, position.y)
            }
        }
    }

    fun dispose() {
        // No GPU resources to release here (sprite sheets are owned by GameAssetManager)
    }
}
