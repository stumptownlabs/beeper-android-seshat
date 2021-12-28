use jni::sys::{jboolean, jint, jlong, jstring};
use jni::JNIEnv;

use jni::objects::{JObject, JString};
use seshat::{CheckpointDirection, CrawlerCheckpoint};

use crate::utils::*;

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_CrawlerCheckpoint_n_1new_1checkpoint(
    env: JNIEnv,
    _: JObject,
    j_room_id: JString,
    j_token: JString,
    j_full_crawl: jboolean,
    j_direction: jint,
) -> jlong {
    let room_id = jstring_to_string(&env, j_room_id);
    let token = jstring_to_string(&env, j_token);

    let full_crawl = match j_full_crawl as u8 {
        0 => false,
        _ => true,
    };

    let direction = match j_direction as u32 {
        0 => CheckpointDirection::Forwards,
        _ => CheckpointDirection::Backwards,
    };

    let crawler_checkpoint: CrawlerCheckpoint = CrawlerCheckpoint {
        room_id,
        token,
        full_crawl,
        direction,
    };
    Box::into_raw(Box::new(crawler_checkpoint)) as jlong
}



#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_CrawlerCheckpoint_n_1free_1crawler_1checkpoint(
    _: JNIEnv,
    _: JObject,
    crawler_checkpoint_ptr: jlong,
) {
    Box::from_raw(crawler_checkpoint_ptr as *mut CrawlerCheckpoint);
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_CrawlerCheckpoint_n_1get_1room_1id(
    env: JNIEnv,
    _: JObject,
    crawler_checkpoint_ptr: jlong,
) -> jstring {
    let checkpoint = Box::from_raw(crawler_checkpoint_ptr as *mut CrawlerCheckpoint);
    let room_id = checkpoint.room_id.clone();
    Box::leak(checkpoint);
    let output = env.new_string(room_id).unwrap();
    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_CrawlerCheckpoint_n_1get_1token(
    env: JNIEnv,
    _: JObject,
    crawler_checkpoint_ptr: jlong,
) -> jstring {
    let checkpoint = Box::from_raw(crawler_checkpoint_ptr as *mut CrawlerCheckpoint);
    let token = checkpoint.token.clone();
    Box::leak(checkpoint);
    let output = env.new_string(token).unwrap();
    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_CrawlerCheckpoint_n_1get_1full_1crawl(
    _: JNIEnv,
    _: JObject,
    crawler_checkpoint_ptr: jlong,
) -> jboolean {
    let checkpoint = Box::from_raw(crawler_checkpoint_ptr as *mut CrawlerCheckpoint);
    let full_crawl = checkpoint.full_crawl;
    Box::leak(checkpoint);
    match full_crawl {
        true => 1 as jboolean,
        false => 0 as jboolean,
    }
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_event_CrawlerCheckpoint_n_1get_1direction(
    _: JNIEnv,
    _: JObject,
    crawler_checkpoint_ptr: jlong,
) -> jint {
    let checkpoint = Box::from_raw(crawler_checkpoint_ptr as *mut CrawlerCheckpoint);
    let direction = checkpoint.direction.clone();
    Box::leak(checkpoint);
    match direction {
        CheckpointDirection::Forwards => 0,
        CheckpointDirection::Backwards => 1,
    }
}



