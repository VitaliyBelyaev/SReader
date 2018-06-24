#include <jni.h>

extern "C" JNIEXPORT jfloat

JNICALL
Java_ru_vitaliybelyaev_sreader_SensorService_findAverageInC(
        JNIEnv *env,
        jobject,
        jfloatArray jValues) {

    int size = env->GetArrayLength(jValues);
    float *body = env->GetFloatArrayElements(jValues, 0);

    float min = body[0];
    float max = body[0];
    float sum = 0;
    int m = 0;

    //find min and max
    for (int i = 1; i < size ; i++) {
        if (body[i] < min) min = body[i];
        else if (body[i] > max) max = body[i];
    }

    //calculating summ excluding all min and max
    for (int i = 0; i < size ; i++) {
        if (body[i] != min && body[i] != max) sum = sum + body[i];
        else m = m + 1;
    }

    //number of element in array without min and max
    int k = size - m;

    env->ReleaseFloatArrayElements(jValues, body, JNI_ABORT);

    return sum / k;
}
