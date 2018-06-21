#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_ru_vitaliybelyaev_sreader_SensorService_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
