package dam_mo8_eac4_ex2.conejo_l

import com.badlogic.gdx.Game

// Application entry point: sets up the main screen
// and delegates render/dispose to it.
class HodosAletheias : Game() {

    override fun create() {
        // Set the main game screen
        setScreen(GameScreen(this))
    }

    override fun render() {
        super.render()
    }

    override fun dispose() {
        // The screen manages its own resources, so disposing it here is safe
        screen?.dispose()
    }
}
