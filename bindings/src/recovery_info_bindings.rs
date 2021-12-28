extern crate jni;
extern crate seshat;

use std::sync::atomic::Ordering;

use jni::sys::{jlong};
use jni::JNIEnv;

use jni::objects::{JObject};
use seshat::{RecoveryInfo};


/*
 *   RECOVERY INFO BINDINGS
 */

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryInfo_n_1recovery_1info_1get_1total_1event_1count(
    _: JNIEnv,
    _: JObject,
    recovery_info_ptr: jlong,
) -> jlong {
    let recovery_info = Box::from_raw(recovery_info_ptr as *mut RecoveryInfo);
    let total_event_count = recovery_info.total_events() as jlong;
    Box::leak(recovery_info);
    total_event_count
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryInfo_n_1recovery_1info_1get_1reindexed_1events(
    _: JNIEnv,
    _: JObject,
    recovery_info_ptr: jlong,
) -> jlong {
    let recovery_info = Box::from_raw(recovery_info_ptr as *mut RecoveryInfo);
    let reindexed_events = recovery_info.clone().reindexed_events().load(Ordering::Relaxed) as jlong;
    Box::leak(recovery_info);
    reindexed_events
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryInfo_n_1free_1recovery_1info(
    _: JNIEnv,
    _: JObject,
    ptr: jlong,
) {
    Box::from_raw(ptr as *mut RecoveryInfo);
}

