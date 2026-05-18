package edu.groupname.compiler.lexer;

/**
 * 源程序字符缓冲区，供词法扫描按索引读取（对应实验指导中的缓冲区设计）。
 */
final class SourceBuffer {
    private final char[] data;
    private final int length;

    SourceBuffer(String source) {
        String normalized = source == null ? "" : source;
        this.data = normalized.toCharArray();
        this.length = data.length;
    }

    int length() {
        return length;
    }

    char charAt(int index) {
        return data[index];
    }

    String substring(int start, int end) {
        return new String(data, start, end - start);
    }
}
