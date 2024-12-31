/**
 * Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

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
