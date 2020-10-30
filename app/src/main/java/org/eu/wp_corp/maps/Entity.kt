package org.eu.wp_corp.maps

import org.json.JSONObject
import org.osmdroid.util.GeoPoint


data class Entity(
    val location: Pair<GeoPoint, String>?,
    val title: String?,
    val description: String?,
) {
    companion object {
        fun from(obj: JSONObject): Entity {
            return Entity(
                location = obj.optJSONArray("location")?.run {
                    if (length() == 2) {
                        val style = optString(1)!!

                        getJSONArray(0)?.run {
                            if (length() == 2) {
                                GeoPoint(optDouble(0), optDouble(1))
                            } else {
                                null
                            }
                        }?.let { geo ->
                            geo to style
                        }
                    } else {
                        null
                    }
                },
                title = obj.optString("title"),
                description = obj.optString("description"),
            )
        }
    }
}
