extern crate jni;
extern crate seshat;

use jni::sys::{jlong};
use jni::JNIEnv;

use jni::objects::{JObject, JString};
use seshat::{Profile};

use crate::utils::*;

/*
 *   PROFILE BINDINGS
 */
#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_profile_Profile_n_1new_1profile(
    env: JNIEnv,
    _: JObject,
    j_display_name: JString,
    j_avatar_url: JString,
) -> jlong {
    let display_name = jstring_to_string(&env, j_display_name);
    let avatar_url = jstring_to_string(&env, j_avatar_url);

    let profile = Profile::new(display_name.as_str(), avatar_url.as_str());

    Box::into_raw(Box::new(profile)) as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_profile_Profile_n_1free_1profile(
    _: JNIEnv,
    _: JObject,
    profile_ptr: jlong,
) {
    Box::from_raw(profile_ptr as *mut Profile);
}
