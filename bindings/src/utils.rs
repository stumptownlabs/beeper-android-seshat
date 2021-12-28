use std::ffi::{CStr, CString};
use jni::JNIEnv;

use jni::objects::{JObject, JString};
use seshat::{Error, CrawlerCheckpoint, Event, EventType};

use serde_json::Value;

use std::{
    convert::TryInto,
    io::{Error as IoError, ErrorKind},
};

/*
 *   UTILITIES
 */
pub unsafe fn jstring_to_string(env: &JNIEnv, j_string: JString) -> String {
    let c_str = CString::from(CStr::from_ptr(env.get_string(j_string).unwrap().as_ptr()));
    c_str.to_str().unwrap().to_string()
}

pub fn seshat_error_code(error: Error) -> i32 {
    match error {
        Error::PoolError(_) => 0,
        Error::DatabaseError(_) => 1,
        Error::IndexError(_) => 2,
        Error::FsError(_) => 3,
        Error::IOError(_) => 4,
        Error::DatabaseUnlockError(_) => 5,
        Error::DatabaseVersionError => 6,
        Error::DatabaseOpenError(_) => 7,
        Error::SqlCipherError(_) => 8,
        Error::ReindexError => 9,
    }
}

pub fn recv_error_code() -> i32 {
    10
}

pub unsafe fn j_object_to_checkpoint_option(env: &JNIEnv, j_native_option_checkpoint: JObject) -> Option<CrawlerCheckpoint> {
    let has_new_checkpoint_j_value = env.call_method(j_native_option_checkpoint, "hasSome", "()Z", &[]).unwrap();
    let has_new_checkpoint = has_new_checkpoint_j_value.z().unwrap();
    match has_new_checkpoint {
        true => {
            let checkpoint_ptr_j_value = env.call_method(j_native_option_checkpoint, "getValue", "()J", &[]).unwrap();
            let checkpoint_ptr = checkpoint_ptr_j_value.j().unwrap();
            let checkpoint = Box::from_raw(checkpoint_ptr as *mut CrawlerCheckpoint);
            let option = Some((*checkpoint).clone());
            Box::leak(checkpoint);
            option
        },
        false => None,
    }
}

pub fn event_to_json(event: Event) -> Result<String,serde_json::error::Error> {
    match serde_json::to_string(&event){
        Ok(json) => {
            Ok(json)
        }
        Err(error) => {
            Err(error)
        }
    }
}

pub fn event_from_json(event_source: &str) -> std::io::Result<Event> {
    let object: Value = serde_json::from_str(event_source)?;
    let content = &object["content"];
    let event_type = &object["type"];

    let event_type = match event_type.as_str().unwrap_or_default() {
        "m.room.message" => EventType::Message,
        "m.room.name" => EventType::Name,
        "m.room.topic" => EventType::Topic,
        _ => return Err(IoError::new(ErrorKind::Other, "Invalid event type.")),
    };

    let (content_value, msgtype) = match event_type {
        EventType::Message => (
            content["body"]
                .as_str()
                .ok_or_else(|| IoError::new(ErrorKind::Other, "No content value found"))?,
            Some("m.text"),
        ),
        EventType::Topic => (
            content["topic"]
                .as_str()
                .ok_or_else(|| IoError::new(ErrorKind::Other, "No content value found"))?,
            None,
        ),
        EventType::Name => (
            content["name"]
                .as_str()
                .ok_or_else(|| IoError::new(ErrorKind::Other, "No content value found"))?,
            None,
        ),
    };

    let event_id = object["event_id"]
        .as_str()
        .ok_or_else(|| IoError::new(ErrorKind::Other, "No event id found"))?;
    let sender = object["sender"]
        .as_str()
        .ok_or_else(|| IoError::new(ErrorKind::Other, "No sender found"))?;
    let server_ts = object["origin_server_ts"]
        .as_u64()
        .ok_or_else(|| IoError::new(ErrorKind::Other, "No server timestamp found"))?;
    let room_id = object["room_id"]
        .as_str()
        .ok_or_else(|| IoError::new(ErrorKind::Other, "No room id found"))?;

    Ok(Event::new(
        event_type,
        content_value,
        msgtype,
        event_id,
        sender,
        server_ts.try_into().map_err(|_e| {
            IoError::new(ErrorKind::Other, "Server timestamp out of valid range")
        })?,
        room_id,
        &event_source,
    ))
}


pub fn partial_event_from_json(event_source: &str) -> std::io::Result<Event> {
    let object: Value = serde_json::from_str(event_source)?;
    let content = &object["content_value"];
    let event_type = &object["event_type"];

    let event_type = match event_type.as_str().unwrap_or_default() {
        "Message" => EventType::Message,
        "Name" => EventType::Name,
        "Topic" => EventType::Topic,
        _ => return Err(IoError::new(ErrorKind::Other, "Invalid event type.")),
    };

    let (content_value, msgtype) = match event_type {
        EventType::Message => (
            content.as_str()
                .ok_or_else(|| IoError::new(ErrorKind::Other, "No content value found"))?,
            Some("m.text"),
        ),
        EventType::Topic => (
            content.as_str()
                .ok_or_else(|| IoError::new(ErrorKind::Other, "No content value found"))?,
            None,
        ),
        EventType::Name => (
            content.as_str()
                .ok_or_else(|| IoError::new(ErrorKind::Other, "No content value found"))?,
            None,
        ),
    };

    let event_id = object["event_id"]
        .as_str()
        .ok_or_else(|| IoError::new(ErrorKind::Other, "No event id found"))?;
    let sender = object["sender"]
        .as_str()
        .ok_or_else(|| IoError::new(ErrorKind::Other, "No sender found"))?;
    let server_ts = object["server_ts"]
        .as_u64()
        .ok_or_else(|| IoError::new(ErrorKind::Other, "No server timestamp found"))?;
    let room_id = object["room_id"]
        .as_str()
        .ok_or_else(|| IoError::new(ErrorKind::Other, "No room id found"))?;

    Ok(Event::new(
        event_type,
        content_value,
        msgtype,
        event_id,
        sender,
        server_ts.try_into().map_err(|_e| {
            IoError::new(ErrorKind::Other, "Server timestamp out of valid range")
        })?,
        room_id,
        &event_source,
    ))
}
