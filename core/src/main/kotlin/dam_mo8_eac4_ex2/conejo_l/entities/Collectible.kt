package dam_mo8_eac4_ex2.conejo_l

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle

class Collectible(x: Float, y: Float, val type: Type, private val region: TextureRegion) {
    enum class Type { COIN, LIFE }

    val bounds = Rectangle(x, y, 16f, 16f)

    fun draw(batch: SpriteBatch) {
        batch.draw(region, bounds.x, bounds.y, bounds.width, bounds.height)
    }
}
