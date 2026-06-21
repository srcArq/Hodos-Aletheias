package dam_mo8_eac4_ex2.conejo_l.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.graphics.g2d.TextureRegion
import dam_mo8_eac4_ex2.conejo_l.Collectible

// A teleport: the trigger zone the player steps on, plus its link id (paired with its partner).
class Teleport(val rect: Rectangle, val link: Int)

class GameMapParser(private val map: TiledMap, private val assetManager: GameAssetManager) {

    fun parseCollisions(): Array<Shape2D> {
        val walls = Array<Shape2D>()
        val collisionLayer = map.layers.get("collisions")
        if (collisionLayer != null) {
            for (mapObject in collisionLayer.objects) {
                when (mapObject) {
                    is RectangleMapObject -> walls.add(mapObject.rectangle)
                    is PolygonMapObject -> {
                        val polygon = mapObject.polygon
                        val x = mapObject.properties.get("x", Float::class.java)
                        val y = mapObject.properties.get("y", Float::class.java)
                        if (x != null && y != null) polygon.setPosition(x, y)
                        walls.add(polygon)
                    }
                    is EllipseMapObject -> {
                        val ellipse = mapObject.ellipse
                        val radius = ellipse.width / 2
                        val circle = Circle(ellipse.x + radius, ellipse.y + radius, radius)
                        walls.add(circle)
                    }
                }
            }
        }
        return walls
    }

    fun parseCollectibles(): Array<Collectible> {
        val collectibles = Array<Collectible>()
        val itemsLayer = map.layers.get("collectibles")
        val coinRegion = assetManager.coinRegion
        val lifeRegion = assetManager.lifeRegion

        itemsLayer?.objects?.forEach { mapObject ->
            if (mapObject is RectangleMapObject) {
                val typeProperty = mapObject.properties.get("type", String::class.java)
                val rect = mapObject.rectangle
                val collectibleType = when (typeProperty) {
                    "coin" -> Collectible.Type.COIN
                    "life" -> Collectible.Type.LIFE
                    else -> null
                }
                collectibleType?.let { type ->
                    val region: TextureRegion = if (type == Collectible.Type.COIN) coinRegion else lifeRegion
                    collectibles.add(Collectible(rect.x, rect.y, type, region))
                }
            }
        }
        return collectibles
    }

    fun parseSatyrSpawns(): Array<Vector2> {
        val satyrSpawnPositions = Array<Vector2>()
        val enemiesLayer = map.layers.get("enemies")
        enemiesLayer?.objects?.forEach { mapObject ->
            if (mapObject is RectangleMapObject) {
                val typeProperty = mapObject.properties.get("type", String::class.java)
                val rect = mapObject.rectangle

                if (typeProperty == "satyr") {
                    satyrSpawnPositions.add(Vector2(rect.x, rect.y))
                }
            }
        }
        Gdx.app.log("MAP_PARSER", "Posiciones de spawn leídas de Tiled: ${satyrSpawnPositions.size}")
        return satyrSpawnPositions
    }

    fun parseWraithSpawns(): Array<Vector2> {
        val spawns = Array<Vector2>()
        val enemiesLayer = map.layers.get("enemies")
        enemiesLayer?.objects?.forEach { mapObject ->
            if (mapObject is RectangleMapObject) {
                val typeProperty = mapObject.properties.get("type", String::class.java)
                if (typeProperty == "wraith") {
                    val rect = mapObject.rectangle
                    spawns.add(Vector2(rect.x, rect.y))
                }
            }
        }
        Gdx.app.log("MAP_PARSER", "Espectres (wraith) llegits: ${spawns.size}")
        return spawns
    }

    fun parseTeleports(): Array<Teleport> {
        val teleports = Array<Teleport>()
        val layer = map.layers.get("teleports")
        layer?.objects?.forEach { mapObject ->
            if (mapObject is RectangleMapObject) {
                val link = mapObject.properties.get("link", Integer::class.java)?.toInt() ?: -1
                teleports.add(Teleport(Rectangle(mapObject.rectangle), link))
            }
        }
        Gdx.app.log("MAP_PARSER", "Teletransports llegits: ${teleports.size}")
        return teleports
    }
}
