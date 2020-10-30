package org.eu.wp_corp.maps.network

import org.eu.wp_corp.maps.Entity
import org.json.JSONArray
import org.json.JSONObject


abstract class Message {

    data class Full(val collections: Map<String, MutableList<Entity>>) : Message()
    data class CollectionAdd(val change: Pair<Int, Entity>) : Message()
    data class CollectionRemove(val change: Pair<Int, Entity>) : Message()
    data class CollectionSet(val change: Pair<Int, Entity>) : Message()

    companion object {
        private fun JSONObject.toArrayMap(): HashMap<String, JSONArray> {
            val map = HashMap<String, JSONArray>()
            for (key in keys()) {
                map[key] = getJSONArray(key)
            }
            return map
        }

        private fun indexValueOf(obj: JSONObject): Pair<Int, Entity> {
            return obj.getInt("index") to Entity.from(obj.getJSONObject("value"))
        }

        fun from(msg: String): Message {
            val msg = JSONObject(msg)

            return when (msg.optString("type", "")) {
                "full" -> Full(msg.run {
                    val tabs = getJSONObject("tabs").toArrayMap()
                    tabs.mapValues {
                        val array = it.value
                        val collection = mutableListOf<Entity>()
                        for (i in 0 until array.length()) {
                            collection.add(Entity.from(array.getJSONObject(i)))
                        }
                        collection
                    }
                })
                "collection_add" -> CollectionAdd(indexValueOf(msg))
                "collection_remove" -> CollectionRemove(indexValueOf(msg))
                "collection_set" -> CollectionSet(indexValueOf(msg))
                else -> throw IllegalArgumentException("Badly formatted JSON message")
            }
        }
    }

}