extern crate jni;
extern crate seshat;

use jni::sys::{jboolean, jint, jlong, jlongArray};
use jni::JNIEnv;

use jni::objects::{JObject, JString, JValue};
use seshat::{
    Config, Database, DatabaseStats, Event,
    Language, Profile, SearchConfig, LoadConfig,
    LoadDirection
};

use crate::utils::*;

/*
 *   DATABASE BINDINGS
 */

/// Opens a database and return it's pointer to be hold on Kotlin code.
/// # Arguments
///
/// * `j_dir_path` - The directory to create/open the database file.
/// * `j_result` - The NativeDatabaseResult object to write the 'databasePointer' or 'errorCode' fields.
#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1new_1database(
    env: JNIEnv,
    _: JObject,
    j_dir_path: JString,
    j_result: JObject,
) {
    let dir_path = jstring_to_string(&env, j_dir_path);
    let result = Database::new(dir_path);

    match result {
        Ok(database) => {
            let database_pointer = Box::into_raw(Box::new(database)) as jlong;
            let jvm_long_field_id_type = "J";
            let database_ptr_field_name = "resultPtr";

            env.set_field(
                j_result,
                database_ptr_field_name,
                jvm_long_field_id_type,
                JValue::from(database_pointer),
            )
                .unwrap();
        }
        Err(error) => {
            let error_message = error.to_string();

            let error_code = seshat_error_code(error);
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



/// Opens a database using a database Config and return it's pointer to be hold on Kotlin code.
/// # Arguments
///
/// * `j_dir_path` - The directory to create/open the database file.
/// * `config_ptr` - A pointer to a Database Config.
/// * `j_result` - The NativeDatabaseResult object to write the 'databasePointer' or 'errorCode' fields.
#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1new_1database_1with_1config(
    env: JNIEnv,
    _: JObject,
    j_dir_path: JString,
    config_ptr: jlong,
    j_result: JObject,
) {
    let dir_path = jstring_to_string(&env, j_dir_path);
    let config = Box::from_raw(config_ptr as *mut Config);

    let result = Database::new_with_config(dir_path, &config);

    match result {
        Ok(database) => {
            let database_pointer = Box::into_raw(Box::new(database)) as jlong;
            let jvm_long_field_id_type = "J";
            let database_ptr_field_name = "resultPtr";

            env.set_field(
                j_result,
                database_ptr_field_name,
                jvm_long_field_id_type,
                JValue::from(database_pointer),
            )
                .unwrap();
        }
        Err(error) => {
            let error_code = seshat_error_code(error);
            let jvm_int_field_id_type = "I";
            let error_code_field_name = "errorCode";

            env.set_field(
                j_result,
                error_code_field_name,
                jvm_int_field_id_type,
                JValue::from(error_code),
            )
                .unwrap();
        }
    };
    Box::leak(config);
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_DatabaseConfig_n_1new_1database_1config(
    env: JNIEnv,
    _: JObject,
    j_language_code: jint,
    j_passphrase: JString,
) -> jlong {
    let passphrase = jstring_to_string(&env, j_passphrase);

    let language = match j_language_code as i32 {
        0 => Language::Arabic,
        1 => Language::Danish,
        2 => Language::Dutch,
        3 => Language::English,
        4 => Language::Finnish,
        5 => Language::French,
        6 => Language::German,
        7 => Language::Greek,
        8 => Language::Hungarian,
        9 => Language::Italian,
        10 => Language::Portuguese,
        11 => Language::Romanian,
        12 => Language::Russian,
        13 => Language::Spanish,
        14 => Language::Swedish,
        15 => Language::Tamil,
        16 => Language::Turkish,
        17 => Language::Japanese,
        _ => Language::Unknown,
    };

    let mut config = Config::new();
    config = config.set_passphrase(passphrase);
    config = config.set_language(&language);
    Box::into_raw(Box::new(config)) as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1add_1event(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    event_ptr: jlong,
    profile_ptr: jlong,
) {
    let db = Box::from_raw(database_ptr as *mut Database);
    let event = Box::from_raw(event_ptr as *mut Event);
    let profile = Box::from_raw(profile_ptr as *mut Profile);

    db.add_event((*event).clone(), (*profile).clone());

    Box::leak(db);
    Box::leak(event);
    Box::leak(profile);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1change_1passphrase(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    j_new_passphrase: JString,
) {
    let db = Box::from_raw(database_ptr as *mut Database);
    let new_passphrase = jstring_to_string(&env, j_new_passphrase);
    db.change_passphrase(new_passphrase.clone().as_str())
        .unwrap();
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1get_1size(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
) -> jlong {
    let db = Box::from_raw(database_ptr as *mut Database);
    let size = db.get_size().unwrap() as jlong;
    Box::leak(db);
    size
}



#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1delete(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
) {
    let db = Box::from_raw(database_ptr as *mut Database);
    db.delete().unwrap();
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1shutdown(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    j_callback: JObject,
) {

    let db = Box::from_raw(database_ptr as *mut Database);
    match db.shutdown().recv(){
        Ok(_) => {
            env.call_method(j_callback, "onResult", "(Z)V", &[JValue::from(true)])
                .unwrap();
        }
        Err(_) => {
            let error_code = recv_error_code();
            env.call_method(j_callback, "onError", "(I)V", &[JValue::from(error_code)])
                .unwrap();
        }
    };
}



#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1is_1empty(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    j_callback: JObject,
) {
    let db = Box::from_raw(database_ptr as *mut Database);
    let is_empty = db.get_connection().unwrap().is_empty();
    Box::leak(db);

    match is_empty{
        Ok(result) => {
            env.call_method(j_callback, "onResult", "(Z)V", &[JValue::from(result)])
                .unwrap();
        }
        Err(_) => {
            let error_code = recv_error_code();
            env.call_method(j_callback, "onError", "(I)V", &[JValue::from(error_code)])
                .unwrap();
        }
    };
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1set_1user_1version(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    user_version: jlong,
) {
    let db = Box::from_raw(database_ptr as *mut Database);
    db.get_connection().unwrap().set_user_version(user_version).unwrap();
    Box::leak(db);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1load_1file_1events(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    j_room_id: JString,
    limit: jint,
    j_direction: jint,
    j_has_event: jboolean,
    j_from_event: JString,
    event_list_result: JObject
) {

    let room_id = jstring_to_string(&env, j_room_id);
    let direction = match j_direction as u32 {
        0 => LoadDirection::Forwards,
        _ => LoadDirection::Backwards,
    };

    let load_config = match j_has_event as u8 {
        0 => {
            LoadConfig::new(
                room_id.clone(),
            ).limit(limit as usize).direction(direction)
        }
        _ => {
            let from_event = jstring_to_string(&env, j_from_event);
            LoadConfig::new(
                room_id.clone(),
            ).limit(limit as usize).direction(direction).from_event(from_event)
        }
    };

    let db = Box::from_raw(database_ptr as *mut Database);
    let events = db.get_connection().unwrap().load_file_events(&load_config).unwrap();

    for event_pair in events.iter() {
        let event = &event_pair.0;
        let profile = &event_pair.1;

        let profile_ptr = Box::into_raw(Box::new(profile)) as jlong;

        let event_str = env.new_string(event).unwrap().into_inner();
        env.call_method(event_list_result, "add", "(Ljava/lang/String;J)V",
                        &[JValue::from(JObject::from(event_str)),JValue::from(profile_ptr)])
            .unwrap();
    }
    Box::leak(db);
}



#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1get_1user_1version(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
) -> jlong{
    let db = Box::from_raw(database_ptr as *mut Database);
    let user_version = db.get_connection().unwrap().get_user_version().unwrap();
    Box::leak(db);
    user_version
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1delete_1event(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    j_event_id: JString,
    j_callback: JObject,
) {
    let db = Box::from_raw(database_ptr as *mut Database);
    let event_id = jstring_to_string(&env, j_event_id);
    let recv_result = db.delete_event(event_id.clone().as_str()).recv();
    match recv_result {
        Ok(delete_result) => match delete_result {
            Ok(result) => {
                env.call_method(j_callback, "onResult", "(Z)V", &[JValue::from(result)])
                    .unwrap();
            }
            Err(error) => {
                let error_code = seshat_error_code(error);
                env.call_method(j_callback, "onError", "(I)V", &[JValue::from(error_code)])
                    .unwrap();
            }
        },
        Err(_) => {
            let recv_error_code = recv_error_code();
            env.call_method(
                j_callback,
                "onError",
                "(I)V",
                &[JValue::from(recv_error_code)],
            )
                .unwrap();
        }
    }
    Box::leak(db);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1commit_1sync(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
) {
    let mut db = Box::from_raw(database_ptr as *mut Database);
    db.commit().unwrap();
    Box::leak(db);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1commit_1no_1wait(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    j_callback: JObject,
) {
    let mut db = Box::from_raw(database_ptr as *mut Database);

    let recv_result = db.commit_no_wait().recv();
    match recv_result {
        Ok(_) => {
            env.call_method(j_callback, "onResult", "(Z)V", &[JValue::from(true)])
                .unwrap();
        }
        Err(_) => {
            let recv_error_code = recv_error_code();
            env.call_method(
                j_callback,
                "onError",
                "(I)V",
                &[JValue::from(recv_error_code)],
            )
                .unwrap();
        }
    }
    Box::leak(db);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1is_1room_1indexed(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    j_room_id: JString,
) -> jboolean {
    let db = Box::from_raw(database_ptr as *mut Database);
    let room_id = jstring_to_string(&env, j_room_id);
    let result = db
        .get_connection()
        .unwrap()
        .is_room_indexed(room_id.as_str())
        .unwrap();
    Box::leak(db);
    match result {
        true => 1 as jboolean,
        false => 0 as jboolean,
    }
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1add_1historic_1events(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    event_list: jlongArray,
    profile_list: jlongArray,
    j_new_checkpoint: JObject,
    j_old_checkpoint: JObject,
    j_callback: JObject,
){

    let db = Box::from_raw(database_ptr as *mut Database);
    let num_events = env.get_array_length(event_list).unwrap();
    let mut event_buff = vec![0; num_events as usize];
    env.get_long_array_region(event_list, 0, &mut event_buff).unwrap();


    let num_profiles = env.get_array_length(profile_list).unwrap();
    let mut profile_buff = vec![0; num_profiles as usize];
    env.get_long_array_region(profile_list, 0, &mut profile_buff).unwrap();

    assert_eq!(num_events,num_profiles);

    let mut events : Vec<(Event,Profile)> = Vec::new();

    for i in 0..num_events {
        let event_ptr = event_buff[i as usize];
        let profile_ptr = profile_buff[i as usize];
        let event = Box::from_raw(event_ptr as *mut Event);
        let profile = Box::from_raw(profile_ptr as *mut Profile);
        events.push(((*event).clone(),(*profile).clone()));
        Box::leak(event);
        Box::leak(profile);
    }

    let new_checkpoint_option = j_object_to_checkpoint_option(&env,j_new_checkpoint);
    let old_checkpoint_option = j_object_to_checkpoint_option(&env,j_old_checkpoint);



    let recv_result = db.add_historic_events(
        events,
        new_checkpoint_option,
        old_checkpoint_option
    ).recv();

    match recv_result {
        Ok(result) => {
            env.call_method(j_callback, "onResult", "(Z)V", &[JValue::from(result.unwrap())])
                .unwrap();
        }
        Err(_) => {
            let recv_error_code = recv_error_code();
            env.call_method(
                j_callback,
                "onError",
                "(I)V",
                &[JValue::from(recv_error_code)],
            )
                .unwrap();
        }
    };
    Box::leak(db);

}




#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1load_1checkpoints(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong
) -> jlongArray{
    let db = Box::from_raw(database_ptr as *mut Database);
    let checkpoints = db.get_connection().unwrap().load_checkpoints().unwrap();
    let count = checkpoints.len();
    let long_array = env.new_long_array(count as i32).unwrap();

    let mut buff = vec![0; count];
    let mut i = 0;
    for checkpoint in checkpoints.iter() {
        let ptr = Box::into_raw(Box::new(checkpoint.clone())) as jlong;
        buff[i] = ptr;
        i = i + 1;
    }

    env.set_long_array_region(
        long_array,
        0,
        &buff
    ).unwrap();

    Box::leak(db);
    long_array
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1force_1commit(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
) {
    let mut db = Box::from_raw(database_ptr as *mut Database);
    db.force_commit().unwrap();
    Box::leak(db);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1reload(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
) {
    let mut db = Box::from_raw(database_ptr as *mut Database);
    db.reload().unwrap();
    Box::leak(db);
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1search(
    env: JNIEnv,
    _: JObject,
    database_ptr: jlong,
    j_search_term: JString,
    search_config_ptr: jlong,
    j_result: JObject,
) {
    let search_term = jstring_to_string(&env, j_search_term);
    let search_config = Box::from_raw(search_config_ptr as *mut SearchConfig);

    let db = Box::from_raw(database_ptr as *mut Database);
    let result = db.search(search_term.as_str(), &search_config);
    Box::leak(search_config);
    Box::leak(db);

    match result {
        Ok(search_result) => {
            let search_result_pointer = Box::into_raw(Box::new(search_result)) as jlong;
            let jvm_long_field_id_type = "J";
            let search_result_ptr_field_name = "resultPtr";
            env.set_field(
                j_result,
                search_result_ptr_field_name,
                jvm_long_field_id_type,
                JValue::from(search_result_pointer),
            )
                .unwrap();
        }
        Err(error) => {
            let error_code = match error {
                _ => 0,
            };
            let jvm_int_field_id_type = "I";
            let search_error_code_field_name = "errorCode";
            env.set_field(
                j_result,
                search_error_code_field_name,
                jvm_int_field_id_type,
                JValue::from(error_code),
            )
                .unwrap();
        }
    };
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_DatabaseStats_n_1database_1stats_1get_1size(
    _: JNIEnv,
    _: JObject,
    database_stats_ptr: jlong,
) -> jlong {
    let database_stats = Box::from_raw(database_stats_ptr as *mut DatabaseStats);
    let size = database_stats.size;
    Box::leak(database_stats);
    size as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_DatabaseStats_n_1database_1stats_1get_1event_1count(
    _: JNIEnv,
    _: JObject,
    database_stats_ptr: jlong,
) -> jlong {
    let database_stats = Box::from_raw(database_stats_ptr as *mut DatabaseStats);
    let event_count = database_stats.event_count;
    Box::leak(database_stats);
    event_count as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_DatabaseStats_n_1database_1stats_1get_1room_1count(
    _: JNIEnv,
    _: JObject,
    database_stats_ptr: jlong,
) -> jlong {
    let database_stats = Box::from_raw(database_stats_ptr as *mut DatabaseStats);
    let room_count = database_stats.room_count;
    Box::leak(database_stats);
    room_count as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1get_1database_1stats(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
) -> jlong {
    let db = Box::from_raw(database_ptr as *mut Database);
    let stats = db.get_connection().unwrap().get_stats().unwrap();
    Box::leak(db);
    Box::into_raw(Box::new(stats)) as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_DatabaseStats_n_1free_1database_1stats(
    _: JNIEnv,
    _: JObject,
    database_stats_ptr: jlong,
) {
    Box::from_raw(database_stats_ptr as *mut DatabaseStats);
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_Database_n_1free_1database(
    _: JNIEnv,
    _: JObject,
    database_ptr: jlong,
) {
    Box::from_raw(database_ptr as *mut Database);
}