extern crate jni;
extern crate seshat;
extern crate uuid;

use jni::sys::{jboolean, jfloat, jint, jlong, jstring};
use jni::JNIEnv;

use uuid::Uuid;
use jni::objects::{JObject, JValue, JString};
use seshat::{SearchBatch,SearchResult,SearchConfig};
use utils::jstring_to_string;

/*
 *   SEARCH BINDINGS
 */

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchBatch_n_1search_1batch_1get_1count(
    _: JNIEnv,
    _: JObject,
    search_batch_ptr: jlong,
) -> jint {
    let search_batch = Box::from_raw(search_batch_ptr as *mut SearchBatch);
    let count = search_batch.count as jint;
    Box::leak(search_batch);
    count
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchBatch_n_1search_1batch_1get_1next_1batch(
    env: JNIEnv,
    _: JObject,
    search_batch_ptr: jlong,
) -> jstring {
    let search_batch = Box::from_raw(search_batch_ptr as *mut SearchBatch);
    let next_batch = search_batch.next_batch;
    let uuid_str = match next_batch {
        Some(uuid) => uuid.to_hyphenated().to_string(),
        None => "".to_string(),
    };
    let output = env.new_string(uuid_str).unwrap();
    Box::leak(search_batch);
    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchBatch_n_1search_1batch_1get_1results(
    env: JNIEnv,
    _: JObject,
    search_batch_ptr: jlong,
    j_results: JObject,
) {
    let search_batch = Box::from_raw(search_batch_ptr as *mut SearchBatch);
    let results = &search_batch.results;
    for search_result in results {
        let search_result_ptr = Box::into_raw(Box::new(search_result.clone())) as jlong;
        env.call_method(j_results, "add", "(J)V", &[JValue::from(search_result_ptr)])
            .unwrap();
    }
    Box::leak(search_batch);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchResult_n_1search_1result_1get_1score(
    _: JNIEnv,
    _: JObject,
    search_result_ptr: jlong,
) -> jfloat {
    let search_result = Box::from_raw(search_result_ptr as *mut SearchResult);
    let score = search_result.score;
    Box::leak(search_result);
    score
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchResult_n_1search_1result_1get_1events_1before(
    env: JNIEnv,
    _: JObject,
    search_result_ptr: jlong,
    j_results: JObject,
) {
    let search_result = Box::from_raw(search_result_ptr as *mut SearchResult);
    if search_result.events_before.len() > 0 {
        let events_before = &search_result.events_before;
        for event in events_before {
            let output = env.new_string(event).unwrap();
            let jvalue_output = output.into_inner();

            env.call_method(
                j_results,
                "add",
                "(Ljava/lang/String;)V",
                &[JValue::from(JObject::from(jvalue_output))],
            )
                .unwrap();
        }
    }
    Box::leak(search_result);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchResult_n_1search_1result_1get_1events_1after(
    env: JNIEnv,
    _: JObject,
    search_result_ptr: jlong,
    j_results: JObject,
) {
    let search_result = Box::from_raw(search_result_ptr as *mut SearchResult);
    if search_result.events_after.len() > 0 {
        let events_after = &search_result.events_after;
        for event in events_after {
            let output = env.new_string(event).unwrap();
            let jvalue_output = output.into_inner();

            env.call_method(
                j_results,
                "add",
                "(Ljava/lang/String;)V",
                &[JValue::from(JObject::from(jvalue_output))],
            )
                .unwrap();
        }
    }
    Box::leak(search_result);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchResult_n_1search_1result_1get_1event_1source(
    env: JNIEnv,
    _: JObject,
    search_result_ptr: jlong,
) -> jstring {
    let search_result = Box::from_raw(search_result_ptr as *mut SearchResult);
    let event_json = search_result.event_source.clone();

    Box::leak(search_result);
    let output = env.new_string(event_json).unwrap();
    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchResult_n_1search_1result_1get_1profile_1info(
    env: JNIEnv,
    _: JObject,
    search_result_ptr: jlong,
    j_native_profile_info_result: JObject,
){
    let search_result = Box::from_raw(search_result_ptr as *mut SearchResult);
    for (mx_id,profile) in &search_result.profile_info{
        let j_mx_id = env.new_string(mx_id).unwrap().into_inner();
        let profile_ptr = Box::into_raw(Box::new(profile)) as jlong;
        env.call_method(j_native_profile_info_result, "add", "(Ljava/lang/String;J)V",
                        &[JValue::from(JObject::from(j_mx_id)),JValue::from(profile_ptr)])
            .unwrap();
    }

    Box::leak(search_result);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchConfig_n_1new_1search_1config(
    _: JNIEnv,
    _: JObject,
) -> jlong {
    let search_config = SearchConfig::new();
    Box::into_raw(Box::new(search_config)) as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchConfig_n_1search_1config_1set_1limit(
    _: JNIEnv,
    _: JObject,
    search_config_ptr: jlong,
    j_limit: jint,
) {
    let mut search_config = Box::from_raw(search_config_ptr as *mut SearchConfig);
    let limit = j_limit as u32 as usize;
    search_config.limit(limit);
    Box::leak(search_config);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchConfig_n_1search_1config_1set_1before_1limit(
    _: JNIEnv,
    _: JObject,
    search_config_ptr: jlong,
    j_before_limit: jint,
) {
    let mut search_config = Box::from_raw(search_config_ptr as *mut SearchConfig);
    let before_limit = j_before_limit as u32 as usize;
    search_config.before_limit(before_limit);
    Box::leak(search_config);
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchConfig_n_1search_1config_1set_1room_1id(
    env: JNIEnv,
    _: JObject,
    search_config_ptr: jlong,
    j_room_id: JString,
) {
    let room_id = jstring_to_string(&env, j_room_id);

    let mut search_config = Box::from_raw(search_config_ptr as *mut SearchConfig);
    search_config.for_room(room_id.as_str());
    Box::leak(search_config);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchConfig_n_1search_1config_1set_1next_1batch(
    env: JNIEnv,
    _: JObject,
    search_config_ptr: jlong,
    j_next_batch: JString,
) {
    let next_batch = jstring_to_string(&env, j_next_batch);
    let mut search_config = Box::from_raw(search_config_ptr as *mut SearchConfig);
    let uuid = Uuid::parse_str(next_batch.as_str()).unwrap();
    search_config.next_batch(uuid);
    Box::leak(search_config);
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchConfig_n_1search_1config_1set_1after_1limit(
    _: JNIEnv,
    _: JObject,
    search_config_ptr: jlong,
    j_after_limit: jint,
) {
    let mut search_config = Box::from_raw(search_config_ptr as *mut SearchConfig);
    let after_limit = j_after_limit as u32 as usize;
    search_config.after_limit(after_limit);
    Box::leak(search_config);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchConfig_n_1search_1config_1set_1order_1by_1recency(
    _: JNIEnv,
    _: JObject,
    search_config_ptr: jlong,
    j_order_by_recency: jboolean,
) {
    let mut search_config = Box::from_raw(search_config_ptr as *mut SearchConfig);
    let order_by_recency = j_order_by_recency as u8;
    search_config.order_by_recency(match order_by_recency {
        0 => false,
        _ => true,
    });
    Box::leak(search_config);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchConfig_n_1free_1search_1config(
    _: JNIEnv,
    _: JObject,
    ptr: jlong,
) {
    Box::from_raw(ptr as *mut SearchConfig);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchBatch_n_1free_1search_1batch(
    _: JNIEnv,
    _: JObject,
    ptr: jlong,
) {
    Box::from_raw(ptr as *mut SearchBatch);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_search_SearchResult_n_1free_1search_1result(
    _: JNIEnv,
    _: JObject,
    ptr: jlong,
) {
    Box::from_raw(ptr as *mut SearchResult);
}



