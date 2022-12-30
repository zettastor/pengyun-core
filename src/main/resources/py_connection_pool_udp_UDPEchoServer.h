
#include <jni.h>

#ifndef _Included_py_connection_pool_udp_UDPEchoServer
#define _Included_py_connection_pool_udp_UDPEchoServer
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_py_connection_pool_udp_UDPEchoServer_startEchoServer
  (JNIEnv *, jobject, jint);

JNIEXPORT jint JNICALL Java_py_connection_pool_udp_UDPEchoServer_stopEchoServer
  (JNIEnv *, jobject, jint);

JNIEXPORT void JNICALL Java_py_connection_pool_udp_UDPEchoServer_pauseEchoServer
  (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_py_connection_pool_udp_UDPEchoServer_reviveEchoServer
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
