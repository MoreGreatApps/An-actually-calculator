package com.example.calculator.engine

enum class NumberBase(val radix: Int, val prefix: String, val label: String) {
    HEX(16, "HEX", "Hexadecimal"),
    DEC(10, "DEC", "Decimal"),
    OCT(8, "OCT", "Octal"),
    BIN(2, "BIN", "Binary")
}

enum class WordSize(val bits: Int, val label: String, val mask: Long) {
    QWORD(64, "64-bit (QWORD)", -1L),
    DWORD(32, "32-bit (DWORD)", 0xFFFFFFFFL),
    WORD(16, "16-bit (WORD)", 0xFFFFL),
    BYTE(8, "8-bit (BYTE)", 0xFFL)
}

object ProgrammerEngine {

    fun formatInBase(value: Long, base: NumberBase, wordSize: WordSize): String {
        val masked = value and wordSize.mask
        return when (base) {
            NumberBase.HEX -> masked.toULong().toString(16).uppercase()
            NumberBase.DEC -> {
                // If sign bit is set for that word size, interpret as signed or unsigned
                when (wordSize) {
                    WordSize.BYTE -> (masked.toByte()).toLong().toString(10)
                    WordSize.WORD -> (masked.toShort()).toLong().toString(10)
                    WordSize.DWORD -> (masked.toInt()).toLong().toString(10)
                    WordSize.QWORD -> masked.toString(10)
                }
            }
            NumberBase.OCT -> masked.toULong().toString(8)
            NumberBase.BIN -> {
                val bin = masked.toULong().toString(2)
                // Format in groups of 4 bits; pad BYTE to 8 bits and WORD to 16 bits for programmer clarity
                val targetBits = when (wordSize) {
                    WordSize.BYTE -> 8
                    WordSize.WORD -> 16
                    WordSize.DWORD -> {
                        if (bin.length <= 16) maxOf(4, ((bin.length + 3) / 4) * 4) else 32
                    }
                    WordSize.QWORD -> {
                        maxOf(4, ((bin.length + 3) / 4) * 4)
                    }
                }
                val padded = bin.padStart(targetBits, '0')
                padded.chunked(4).joinToString(" ")
            }
        }
    }

    fun parseInput(input: String, base: NumberBase, wordSize: WordSize): Long {
        if (input.isEmpty() || input == "-") return 0L
        val clean = input.replace(" ", "")
        val raw = try {
            when (base) {
                NumberBase.HEX -> clean.toULong(16).toLong()
                NumberBase.DEC -> clean.toLong()
                NumberBase.OCT -> clean.toULong(8).toLong()
                NumberBase.BIN -> clean.toULong(2).toLong()
            }
        } catch (e: Exception) {
            0L
        }
        return raw and wordSize.mask
    }
}
