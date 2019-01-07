package com.koryakin.rc6.controller;

public class RC6 {

    private RC6() {
    }

    private static final int W = 32;
    private static final int R = 20;

    private static final int P32 = 0xB7E15163;
    private static final int Q32 = 0x9E3779B9;

    public static byte[] encrypt(byte[] plainText, byte[] key) {
        int[] subKeys = generateSubKeys(key);

        int[] dataBlock = new int[plainText.length / 4];
        int index = 0;
        for (int i = 0; i < dataBlock.length; i++) {
            dataBlock[i] = (plainText[index++] & 0xFF)
                    | ((plainText[index++] & 0xFF) << 8)
                    | ((plainText[index++] & 0xFF) << 16)
                    | ((plainText[index++] & 0xFF) << 24);
        }

        int A, B, C, D;
        A = dataBlock[0];
        B = dataBlock[1];
        C = dataBlock[2];
        D = dataBlock[3];

        B += subKeys[0];
        D += subKeys[1];
        for (int i = 1; i <= R; i++) {
            int lr_B = leftRotate(B * (2 * B + 1), 5);
            int lr_D = leftRotate(D * (2 * D + 1), 5);
            A = leftRotate(A ^ lr_B, lr_D) + subKeys[2 * i];
            C = leftRotate(C ^ lr_D, lr_B) + subKeys[2 * i + 1];

            int swap = A;
            A = B;
            B = C;
            C = D;
            D = swap;
        }
        A = A + subKeys[2 * R + 2];
        C = C + subKeys[2 * R + 3];

        dataBlock[0] = A;
        dataBlock[1] = B;
        dataBlock[2] = C;
        dataBlock[3] = D;

        byte[] completeBlock = new byte[16];
        for (int i = 0; i < completeBlock.length; i++) {
            completeBlock[i] = (byte) ((dataBlock[i / 4] >>> (i % 4) * 8) & 0xFF);
        }

        return completeBlock;
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key) {
        int[] subKeys = generateSubKeys(key);

        int[] dataBlock = new int[cipherText.length / 4];
        int index = 0;
        for (int i = 0; i < dataBlock.length; i++) {
            dataBlock[i] = (cipherText[index++] & 0xFF)
                    | ((cipherText[index++] & 0xFF) << 8)
                    | ((cipherText[index++] & 0xFF) << 16)
                    | ((cipherText[index++] & 0xFF) << 24);
        }

        int A, B, C, D;
        A = dataBlock[0];
        B = dataBlock[1];
        C = dataBlock[2];
        D = dataBlock[3];

        C -= subKeys[2 * R + 3];
        A -= subKeys[2 * R + 2];
        for (int i = R; i >= 1; i--) {
            int swap = D;
            D = C;
            C = B;
            B = A;
            A = swap;

            int lr_D = leftRotate(D * (2 * D + 1), 5);
            int lr_B = leftRotate(B * (2 * B + 1), 5);
            C = rightRotate(C - subKeys[2 * i + 1], lr_B) ^ lr_D;
            A = rightRotate(A - subKeys[2 * i], lr_D) ^ lr_B;
        }
        D -= subKeys[1];
        B -= subKeys[0];

        dataBlock[0] = A;
        dataBlock[1] = B;
        dataBlock[2] = C;
        dataBlock[3] = D;

        byte[] completeBlock = new byte[16];
        for (int i = 0; i < completeBlock.length; i++) {
            completeBlock[i] = (byte) ((dataBlock[i / 4] >>> (i % 4) * 8) & 0xFF);
        }

        return completeBlock;
    }

    private static int[] generateSubKeys(byte[] key) {
        int c = key.length / 4;

        int[] keysL = new int[c];
        int index = 0;
        for (int i = 0; i < keysL.length; i++) {
            keysL[i] = (key[index++] & 0xFF)
                    | ((key[index++] & 0xFF) << 8)
                    | ((key[index++] & 0xFF) << 16)
                    | ((key[index++] & 0xFF) << 24);
        }

        int[] keys = new int[2 * R + 4];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = P32 + i * Q32;
        }

        int A = 0, B = 0, I = 0, J = 0;
        int v = 3 * Math.max(c, 2 * R + 4);
        for (int i = 0; i < v; i++) {
            A = keys[I] = leftRotate(keys[I] + A + B, 3);
            B = keysL[J] = leftRotate(keysL[J] + A + B, A + B);
            I = (I + 1) % (2 * R + 4);
            J = (J + 1) % c;
        }

        return keys;
    }

    private static int leftRotate(int var, int offset) {
        int temp1 = var >>> (W - offset);
        int temp2 = var << offset;
        return temp2 | temp1;
    }

    private static int rightRotate(int var, int offset) {
        int temp1 = var << (W - offset);
        int temp2 = var >>> offset;
        return temp2 | temp1;
    }

}
