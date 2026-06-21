package dam_mo8_eac4_ex2.conejo_l

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import dam_mo8_eac4_ex2.conejo_l.entities.Bullet

class BulletPool(private val spearRegions: Map<Player.Direction, TextureRegion>) : Pool<Bullet>() {

    override fun newObject(): Bullet {
        Gdx.app.log("BulletPool", "Creant nova instància de Bullet (Llança)")
        return Bullet(spearRegions)
    }
}
