#ifndef __TASK_QEUEUE__H
#define __TASK_QEUEUE__H

#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <inttypes.h>
#include <sys/types.h>
#include <pthread.h>
#include "py_list.h"

#define PY_QUEUE_NAME_SIZE 64
enum task_queue_errno {
    E_TQ_SUCC = 0,
    E_TQ_FULL = 1,
    E_TQ_ERROR = 2,
};

struct task_node_list {
    struct list_head list;
    void *data;
};

struct task_queue_hdr {
    char name[PY_QUEUE_NAME_SIZE];
    uint32_t  size;
    uint32_t capacity;
    pthread_mutex_t queue_lock;
    pthread_cond_t  empty_wait;
    pthread_cond_t  full_wait;
    struct task_node_list task_list;
};

struct task_queue_hdr *task_queue_init(u_int32_t capacity, char *name);

int32_t task_queue_limit_offer(struct task_queue_hdr *tq_hdr, struct list_head *node);

int32_t task_queue_unlimit_offer(struct task_queue_hdr *tq_hdr, struct list_head *node);

int32_t task_queue_take(struct task_queue_hdr *tq_hdr, struct task_node_list *nodes[], int count, u_int32_t abs_second);

void task_queue_destory(struct task_queue_hdr *tq_hdr);

#endif