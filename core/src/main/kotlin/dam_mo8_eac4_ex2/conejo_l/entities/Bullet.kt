package dam_mo8_eac4_ex2.conejo_l.entities

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import dam_mo8_eac4_ex2.conejo_l.Player

class Bullet(private val regions: Map<Player.Direction, TextureRegion>) : Pool.Poolable {

    val position = Vector2()
    val speed = 300f
    val bounds = Circle(0f, 0f, 4f)

    var direction: Player.Direction = Player.Direction.DOWN
    private var isAlive = false

    private val MAX_RANGE = 48f
    private val initialPosition = Vector2()

    init {
        bounds.radius = 4f
    }

    fun init(startPosition: Vector2, shotDirection: Player.Direction) {
        initialPosition.set(startPosition)
        position.set(startPosition)
        direction = shotDirection
        isAlive = true
        bounds.set(position.x, position.y, bounds.radius)
    }

    fun update(delta: Float) {
        if (!isAlive) return

        when (direction) {
            Player.Direction.UP -> position.y += speed * delta
            Player.Direction.DOWN -> position.y -= speed * delta
            Player.Direction.LEFT -> position.x -= speed * delta
            Player.Direction.RIGHT -> position.x += speed * delta
        }

        bounds.set(position.x, position.y, bounds.radius)

        if (position.dst(initialPosition) >= MAX_RANGE) {
            isAlive = false
        }
    }

    fun draw(batch: SpriteBatch) {
        if (!isAlive) return

        val region = regions[direction] ?: return

        batch.draw(
            region,
            position.x - region.regionWidth / 2f,
            position.y - region.regionHeight / 2f
        )
    }

    fun isOffScreen(): Boolean {
        return !isAlive
    }

    override fun reset() {
        isAlive = false
        position.set(0f,0f)
    }
}
