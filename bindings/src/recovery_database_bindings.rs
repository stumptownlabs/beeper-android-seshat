extern crate jni;
extern crate seshat;

use jni::sys::{jlong};
use jni::JNIEnv;

use jni::objects::{JObject, JString, JValue};
use seshat::{
    Config, RecoveryDatabase
};

use crate::utils::*;


/*
 *   RECOVERY DATABASE BINDINGS
 */

/// Instantiate a database and return it's pointer to be hold on Kotlin code.
/// # Arguments
///
/// * `j_dir_path` - The directory to create/open the database file.
/// * `j_result` - The NativeDatabaseResult object to write the 'databasePointer' or 'errorCode' fields.
#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryDatabase_n_1new_1database(
    env: JNIEnv,
    _: JObject,
    j_dir_path: JString,
    j_result: JObject,
) {
    let dir_path = jstring_to_string(&env, j_dir_path);
    let result = RecoveryDatabase::new(dir_path);

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

/// Instantiate a database and return it's pointer to be hold on Kotlin code.
/// # Arguments
///
/// * `j_dir_path` - The directory to create/open the database file.
/// * `config_ptr` - A pointer to a Database Config.
/// * `j_result` - The NativeDatabaseResult object to write the 'databasePointer' or 'errorCode' fields.
#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryDatabase_n_1new_1recovery_1database_1with_1config(
    env: JNIEnv,
    _: JObject,
    j_dir_path: JString,
    config_ptr: jlong,
    j_result: JObject,
) {
    let dir_path = jstring_to_string(&env, j_dir_path);
    let config = Box::from_raw(config_ptr as *mut Config);

    let result = RecoveryDatabase::new_with_config(dir_path, &config);

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
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryDatabase_n_1get_1info(
    _: JNIEnv,
    _: JObject,
    recovery_database_ptr: jlong,
) -> jlong {
    let recovery_db = Box::from_raw(recovery_database_ptr as *mut RecoveryDatabase);
    let recovery_info = recovery_db.info().clone();
    Box::leak(recovery_db);
    Box::into_raw(Box::new(recovery_info)) as jlong
}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryDatabase_n_1get_1user_1version(
    _: JNIEnv,
    _: JObject,
    recovery_database_ptr: jlong,
) -> jlong{
    let db = Box::from_raw(recovery_database_ptr as *mut RecoveryDatabase);
    let user_version = db.get_connection().unwrap().get_user_version().unwrap();
    Box::leak(db);
    user_version
}


#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryDatabase_n_1shutdown(
    env: JNIEnv,
    _: JObject,
    recovery_database_ptr: jlong,
    j_callback: JObject,
) {

    let db = Box::from_raw(recovery_database_ptr as *mut RecoveryDatabase);
    match db.shutdown(){
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
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryDatabase_n_1reindex(
    _: JNIEnv,
    _: JObject,
    recovery_database_ptr: jlong,
) {
    let mut recovery_db = Box::from_raw(recovery_database_ptr as *mut RecoveryDatabase);

    recovery_db.delete_the_index().unwrap();
    recovery_db.open_index().unwrap();

    let mut events = recovery_db.load_events_deserialized(500, None).unwrap();
    recovery_db.index_events(&events).unwrap();

    loop {
        events = recovery_db.load_events_deserialized(500, events.last()).unwrap();

        if events.is_empty() {
            break;
        }

        recovery_db.index_events(&events).unwrap();
        recovery_db.commit().unwrap();
    }

    recovery_db.commit_and_close().unwrap();

}

#[no_mangle]
pub unsafe extern "C" fn Java_com_beeper_android_1seshat_database_RecoveryDatabase_n_1free_1database(
    _: JNIEnv,
    _: JObject,
    recovery_database_ptr: jlong,
) {
    Box::from_raw(recovery_database_ptr as *mut RecoveryDatabase);
}