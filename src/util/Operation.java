package util;

public class Operation {

    static {
        System.loadLibrary("src/util/utilOperation");
    }

    static <T> T get(T[] array, byte index) {
        return array[index];
    }

    static <T> void set(T[] array, byte index, T value) {
        array[index] = value;
    }

    static long get(long[] arr, byte index) {
        return arr[index];
    }

    static void set(long[] arr, byte index, long value) {
        arr[index] = value;
    }

    public static long rotateLeft(long i, byte distance) {
        return (i << distance) | (i >>> -distance);
    }

    public static native byte and(byte a, byte b);

    public static native byte or(byte a, byte b);

    public static native byte xor(byte a, byte b);

    public static native byte not(byte a);

    public static native byte shl(byte arg, byte shift);

    public static native byte shl(byte arg, int shift);

    public static native byte shr(byte arg, byte shift);

    public static native byte shr(byte arg, int shift);

    public static native byte ushr(byte arg, byte shift);

    public static native byte ushr(byte arg, int shift);

}
