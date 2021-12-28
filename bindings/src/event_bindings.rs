extern crate jni;
extern crate seshat;

use jni::sys::{jlong, jboolean, jstring};
use jni::JNIEnv;

use jni::objects::{JObject, JString, JValue};
use seshat::{Event, EventType};

use crate::utils::*;


/*
 *   EVENT BINDINGS
 */
#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1new_1event(
    env: JNIEnv,
    _: JObject,
    j_event_type: jlong,
    j_content_value: JString,
    j_has_msg_type: jboolean,
    j_msg_type: JString,
    j_event_id: JString,
    j_sender: JString,
    j_server_ts: jlong,
    j_room_id: JString,
) -> jlong {
    let event_type: EventType = match j_event_type {
        1 => EventType::Name,
        2 => EventType::Topic,
        _ => EventType::Message,
    };
    let content_value = jstring_to_string(&env, j_content_value);
    let msg_type_string = jstring_to_string(&env, j_msg_type);
    let msg_type = match j_has_msg_type {
        0 => None,
        _ => Some(msg_type_string.as_str())
    };
    let event_id = jstring_to_string(&env, j_event_id);
    let sender = jstring_to_string(&env, j_sender);
    let server_ts = j_server_ts;
    let room_id = jstring_to_string(&env, j_room_id);

    let proto_event = Event::new(
        event_type.clone(),
        content_value.as_str(),
        msg_type,
        event_id.as_str(),
        sender.as_str(),
        server_ts,
        room_id.as_str(),
        "",
    );
    let event_source = event_to_json(proto_event).unwrap();

    let event = Event::new(
        event_type,
        content_value.as_str(),
        msg_type,
        event_id.as_str(),
        sender.as_str(),
        server_ts,
        room_id.as_str(),
        event_source.as_str(),
    );

    Box::into_raw(Box::new(event)) as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1free_1event(
    _: JNIEnv,
    _: JObject,
    event_ptr: jlong,
) {
    Box::from_raw(event_ptr as *mut Event);
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1event_1from_1json(
    env: JNIEnv,
    _: JObject,
    j_event_source: JString,
    j_result: JObject
) {
    let event_source = jstring_to_string(&env,j_event_source);
    let result = partial_event_from_json(&event_source.as_str());

    match result {
        Ok(event) => {
            let event_pointer = Box::into_raw(Box::new(event)) as jlong;
            let jvm_long_field_id_type = "J";
            let event_ptr_field_name = "resultPtr";

            env.set_field(
                j_result,
                event_ptr_field_name,
                jvm_long_field_id_type,
                JValue::from(event_pointer),
            )
                .unwrap();
        }
        Err(err) => {
            let error_message = err.to_string();
            let io_error = seshat::Error::IOError(err);
            let error_code = seshat_error_code(io_error);
            let jvm_int_field_id_type = "I";
            let error_code_field_name = "errorCode";

            env.set_field(
                j_result,
                error_code_field_name,
                jvm_int_field_id_type,
                JValue::from(error_code),
            )
                .unwrap();

            let jvm_int_field_id_type = "Ljava/lang/String;";
            let error_message_field_name = "errorMessage";

            let j_error_message = env.new_string(error_message).unwrap();
            let jvalue = j_error_message.into_inner();
            env.set_field(
                j_result,
                error_message_field_name,
                jvm_int_field_id_type,
                JValue::from(JObject::from(jvalue)),
            )
                .unwrap();
        }
    };
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1get_1event_1type(
    _: JNIEnv,
    _: JObject,
    event_ptr: jlong,
) -> jlong {
    let event = Box::from_raw(event_ptr as *mut Event);
    let event_type = event.event_type.clone();
    Box::leak(event);
    match event_type {
        EventType::Message => {
            0
        }
        EventType::Name => {
            1
        }
        EventType::Topic => {
            2
        }
    }
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1get_1event_1content_1value(
    env: JNIEnv,
    _: JObject,
    event_ptr: jlong,
) -> jstring {
    let event = Box::from_raw(event_ptr as *mut Event);
    let content_value = event.content_value.clone();
    Box::leak(event);
    let output = env.new_string(content_value).unwrap();
    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1get_1event_1id(
    env: JNIEnv,
    _: JObject,
    event_ptr: jlong,
) -> jstring {
    let event = Box::from_raw(event_ptr as *mut Event);
    let event_id = event.event_id.clone();
    Box::leak(event);
    let output = env.new_string(event_id).unwrap();
    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1get_1event_1sender(
    env: JNIEnv,
    _: JObject,
    event_ptr: jlong,
) -> jstring {
    let event = Box::from_raw(event_ptr as *mut Event);
    let sender = event.sender.clone();
    Box::leak(event);
    let output = env.new_string(sender).unwrap();
    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1get_1event_1server_1ts(
    _: JNIEnv,
    _: JObject,
    event_ptr: jlong,
) -> jlong {
    let event = Box::from_raw(event_ptr as *mut Event);
    let server_ts = event.server_ts.clone();
    Box::leak(event);
    server_ts
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1get_1event_1room_1id(
    env: JNIEnv,
    _: JObject,
    event_ptr: jlong,
) -> jstring {
    let event = Box::from_raw(event_ptr as *mut Event);
    let room_id = event.room_id.clone();
    Box::leak(event);
    let output = env.new_string(room_id).unwrap();
    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_Event_n_1get_1event_1message_1type(
    env: JNIEnv,
    _: JObject,
    event_ptr: jlong,
) -> jstring {
    let event = Box::from_raw(event_ptr as *mut Event);
    let option = event.msgtype.clone();
    //TODO: Return proper option
    let message_type = match option{
        None => {String::from("")}
        Some(msgtype) => {
            msgtype
        }
    };
    Box::leak(event);
    let output = env.new_string(message_type).unwrap();
    output.into_inner()
}