package dam_mo8_eac4_ex2.conejo_l.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2

// Lightweight pixel particle (square that flies out, falls and fades). Pre-allocated and
// reused via the `active` flag, so spawning bursts never allocates at runtime.
class Particle {
    val pos = Vector2()
    val vel = Vector2()
    val color = Color(1f, 1f, 1f, 1f)
    var life = 0f
    var maxLife = 1f
    var size = 2f
    var gravity = 0f
    var active = false

    fun spawn(x: Float, y: Float, vx: Float, vy: Float, life: Float, size: Float, gravity: Float, r: Float, g: Float, b: Float) {
        pos.set(x, y); vel.set(vx, vy)
        this.life = life; this.maxLife = life; this.size = size; this.gravity = gravity
        color.set(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), 1f)
        active = true
    }

    fun update(delta: Float) {
        if (!active) return
        vel.y -= gravity * delta
        pos.x += vel.x * delta
        pos.y += vel.y * delta
        life -= delta
        if (life <= 0f) active = false
    }

    fun draw(batch: SpriteBatch, pixel: TextureRegion) {
        if (!active) return
        val a = (life / maxLife).coerceIn(0f, 1f)
        batch.setColor(color.r, color.g, color.b, a)
        batch.draw(pixel, pos.x - size / 2f, pos.y - size / 2f, size, size)
    }
}
