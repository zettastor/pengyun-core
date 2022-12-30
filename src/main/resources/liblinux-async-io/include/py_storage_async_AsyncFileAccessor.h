
#include <jni.h>

#ifndef _Included_py_storage_async_AsyncFileAccessor
#define _Included_py_storage_async_AsyncFileAccessor
#ifdef __cplusplus
extern "C" {
#endif
#undef py_storage_async_AsyncFileAccessor_ERR_SUCCESS
#define py_storage_async_AsyncFileAccessor_ERR_SUCCESS 0L
#undef py_storage_async_AsyncFileAccessor_ERR_FAIL
#define py_storage_async_AsyncFileAccessor_ERR_FAIL -1L

JNIEXPORT jlong JNICALL Java_py_storage_async_AsyncFileAccessor_open
  (JNIEnv *, jclass, jstring, jint);

JNIEXPORT void JNICALL Java_py_storage_async_AsyncFileAccessor_close
  (JNIEnv *, jclass, jlong);

JNIEXPORT void JNICALL Java_py_storage_async_AsyncFileAccessor_write
  (JNIEnv *, jclass, jlong, jlong, jlong, jint, jobject);

JNIEXPORT void JNICALL Java_py_storage_async_AsyncFileAccessor_read
  (JNIEnv *, jclass, jlong, jlong, jlong, jint, jobject);

JNIEXPORT void JNICALL Java_py_storage_async_AsyncFileAccessor_enableDiskCheck
  (JNIEnv *, jclass, jlong, jint, jint, jdouble, jint, jobject);

#ifdef __cplusplus
}
#endif
#endif
