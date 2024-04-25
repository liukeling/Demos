#include "cn_lkl_demos_jni_TestNative.h"
#include "stdio.h"
jint i = 4;
JNIEXPORT jint JNICALL Java_cn_lkl_demos_jni_TestNative_helloWord
  (JNIEnv * env, jobject obj){
  i ++;
  return i;
 }