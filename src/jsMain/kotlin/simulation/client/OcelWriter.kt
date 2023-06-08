package simulation.client

import eventlog.Event
import eventlog.EventLog
import kotlinx.js.Object
import kotlinx.js.jso
import model.ObjectToken

@OptIn(ExperimentalJsExport::class)
@JsExport
actual class OcelWriter(val ocelCallback: (ocel: dynamic) -> Unit) {

    val ocelObject = jso<dynamic> {
        this["ocel:events"] = jso<dynamic>()
        this["ocel:objects"] = jso<dynamic>()
    }

    actual fun writeEvent(event: Event) {
        val vmap = jso<dynamic>()

        val jsEvent = jso<dynamic> {
            this["ocel:activity"] = event.activity
            this["ocel:timestamp"] = event.timestamp
            this["ocel:omap"] = event.oMap.toTypedArray()
            this["ocel:vmap"] = vmap
        }
        ocelObject["ocel:events"][event.eventId] = jsEvent
    }

    actual fun writeObjectToken(objectToken: ObjectToken) {
        val jsObject = jso<dynamic> {
            this["ocel:type"] = objectToken.type.label
            this["ocel:ovmap"] = jso<dynamic>()
        }
        ocelObject["ocel:objects"][objectToken.name] = jsObject
    }

    actual fun write(eventLog: EventLog) {
        for ( event in eventLog.events) {
            writeEvent(event)
        }
        for (obj in eventLog.objects) {
            writeObjectToken(obj)
        }
        ocelCallback(ocelObject)
    }

    actual fun collect(): Any {
        return ocelObject as Object
    }
}
