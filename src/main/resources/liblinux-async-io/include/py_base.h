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
#ifndef __PY_BASE__H
#define __PY_BASE__H
#include <syslog.h>

#define PY_E_SUC   0
#define PY_E_SYS   1
#define PY_E_MEM   2
#define PY_EINVAL  3

#define PY_TQ_EFULL 4

#define PY_TRUE       1
#define PY_FALSE      0

#define PY_ENABLED     1
#define PY_DISABLED    0

#define PY_DEBUG_TAG      "DEBUG"
#define PY_INFO_TAG       "INFO"
#define PY_NOTICE_TAG     "NOTICE"
#define PY_WARNING_TAG    "WARNING"
#define PY_ERROR_TAG      "ERROR"
extern int py_debug_output
extern int py_debug_level;
#define PY_DEBUG(p, t, a, x...)                                     \
    do                                                           \
    {                                                            \
        if (p <= py_debug_level)                                  \
        {                                                        \
            if(py_debug_output == 1) {                                                 \
                printf("[%s]%s:%d: async-io: " a, t, __func__, __LINE__, ##x);                         \
                printf("\n");                                                                          \
            } else {                                                                   \
                syslog(p, "[%s] %s:%s:%d: async-io: "a, t, __FILE__, __func__, __LINE__, ##x );        \
            }                                                                          \
        }                                                                              \
    } while (0)

#define PY_LOG_ERROR(x,...)             PY_DEBUG(LOG_ERR, PY_ERROR_TAG, x,  ##__VA_ARGS__ )
#define PY_LOG_WARNING(x,...)           PY_DEBUG(LOG_WARNING, PY_WARNING_TAG, x, ##__VA_ARGS__)
#define PY_LOG_NOTICE(x,...)            PY_DEBUG(LOG_NOTICE, PY_NOTICE_TAG, x, ##__VA_ARGS__ )
#define PY_LOG_INFO(x,...)              PY_DEBUG(LOG_INFO, PY_INFO_TAG, x, ##__VA_ARGS__ )
#define PY_LOG_DEBUG(x,...)             PY_DEBUG(LOG_DEBUG, PY_DEBUG_TAG, x, ##__VA_ARGS__ )

#endif