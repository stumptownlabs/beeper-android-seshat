package com.beeper.android_seshat.event

import com.beeper.android_seshat.LibraryLoader.ensureNativeLibIsLoaded
import com.beeper.android_seshat.database.DatabaseErrorType
import com.beeper.android_seshat.util.NativeResult
import com.beeper.android_seshat.util.Success


class Event internal constructor(ptr: Long){
    internal val ptr:Long

    init{
        ensureNativeLibIsLoaded()
        this.ptr = ptr
    }

    constructor(
        eventType : EventType, contentValue : String, msgType: String?, eventId: String,
        sender: String, serverTs: Long, roomId: String
    ) : this(
        n_new_event(
            eventType.code.toLong(),
            contentValue,
            //TODO: Create NativeOptional type to pass optionals
            msgType != null,
            msgType ?: String(),
            eventId,
            sender,
            serverTs,
            roomId,
        )
    )

    fun getEventType():EventType{
        val typeCode = n_get_event_type(ptr)
        return EventType.fromCode(typeCode)
    }

    fun getContentValue(): String{
        return n_get_event_content_value(ptr)
    }

    fun getMessageType(): String?{
        val messageType = n_get_event_message_type(ptr)
        return if(messageType.isNotEmpty()){
            messageType
        }else{
            null
        }
    }

    fun getEventId(): String{
        return n_get_event_id(ptr)
    }

    fun getSender(): String{
        return n_get_event_sender(ptr)
    }

    fun getServerTs() : Long{
        return n_get_event_server_ts(ptr)
    }

    fun getRoomId() : String{
        return n_get_event_room_id(ptr)
    }

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_event(ptr)
    }

    internal fun testFinalize(){
        finalize()
    }

    private external fun n_free_event(eventPointer: Long)

    private external fun n_get_event_type(eventPointer: Long) : Int
    private external fun n_get_event_content_value(eventPointer: Long) : String
    private external fun n_get_event_id(eventPointer: Long) : String
    private external fun n_get_event_sender(eventPointer: Long) : String
    private external fun n_get_event_server_ts(eventPointer: Long) : Long
    private external fun n_get_event_room_id(eventPointer: Long) : String
    private external fun n_get_event_message_type(eventPointer: Long) : String


    companion object{
        @JvmStatic
        private external fun n_new_event(
            eventType: Long,
            contentValue: String,
            hasMsgType: Boolean,
            msgType: String,
            eventId: String,
            sender: String,
            serverTs: Long,
            roomId: String,
        ): Long

        fun eventFromSource(eventSource:String) : com.beeper.android_seshat.util.Result<Event,DatabaseErrorType>{
            val nativeResult = NativeResult()
            n_event_from_json(eventSource,nativeResult)
            return if(nativeResult.errorCode < 0){
                Success(Event(nativeResult.resultPtr))
            }else{
                com.beeper.android_seshat.util.Error(DatabaseErrorType.fromCode(nativeResult.errorCode, nativeResult.errorMessage))
            }
        }

        @JvmStatic
        private external fun n_event_from_json(
            eventSource: String,
            nativeResult: NativeResult
        )

    }
}




