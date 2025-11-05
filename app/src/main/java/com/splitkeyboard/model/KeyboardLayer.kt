package com.splitkeyboard.model

/**
 * Represents a keyboard layer with its own key layout
 */
data class KeyboardLayer(
    val name: String,
    val leftKeys: List<List<Key>>,  // Rows of keys for left panel
    val rightKeys: List<List<Key>>  // Rows of keys for right panel
)

/**
 * Factory for creating default keyboard layers
 */
object KeyboardLayers {

    fun getDefaultLayers(): Map<String, KeyboardLayer> {
        return mapOf(
            "default" to createDefaultLayer(),
            "shift" to createShiftLayer(),
            "numbers" to createNumbersLayer(),
            "symbols" to createSymbolsLayer()
        )
    }

    private fun createDefaultLayer(): KeyboardLayer {
        val leftKeys = listOf(
            listOf(Key("q"), Key("w"), Key("e"), Key("r"), Key("t")),
            listOf(Key("a"), Key("s"), Key("d"), Key("f"), Key("g")),
            listOf(Key("z"), Key("x"), Key("c"), Key("v"), Key("b")),
            listOf(Key(","), Key("."), Key(" ", type = KeyType.SPACE)),
            listOf(
                Key("123", type = KeyType.LAYER_SWITCH),
                Key("⇧", type = KeyType.SHIFT)
            )
        )

        val rightKeys = listOf(
            listOf(Key("y"), Key("u"), Key("i"), Key("o"), Key("p")),
            listOf(Key("h"), Key("j"), Key("k"), Key("l"), Key(";")),
            listOf(Key("n"), Key("m"), Key("!"), Key("?"), Key("'")),
            listOf(Key("-"), Key("_"), Key(" ", type = KeyType.SPACE)),
            listOf(
                Key("⌫", type = KeyType.BACKSPACE),
                Key("↵", type = KeyType.ENTER)
            )
        )

        return KeyboardLayer("default", leftKeys, rightKeys)
    }

    private fun createShiftLayer(): KeyboardLayer {
        val leftKeys = listOf(
            listOf(Key("Q"), Key("W"), Key("E"), Key("R"), Key("T")),
            listOf(Key("A"), Key("S"), Key("D"), Key("F"), Key("G")),
            listOf(Key("Z"), Key("X"), Key("C"), Key("V"), Key("B")),
            listOf(Key(","), Key("."), Key(" ", type = KeyType.SPACE)),
            listOf(
                Key("123", type = KeyType.LAYER_SWITCH),
                Key("⇧", type = KeyType.SHIFT)
            )
        )

        val rightKeys = listOf(
            listOf(Key("Y"), Key("U"), Key("I"), Key("O"), Key("P")),
            listOf(Key("H"), Key("J"), Key("K"), Key("L"), Key(":")),
            listOf(Key("N"), Key("M"), Key("!"), Key("?"), Key("\"")),
            listOf(Key("-"), Key("_"), Key(" ", type = KeyType.SPACE)),
            listOf(
                Key("⌫", type = KeyType.BACKSPACE),
                Key("↵", type = KeyType.ENTER)
            )
        )

        return KeyboardLayer("shift", leftKeys, rightKeys)
    }

    private fun createNumbersLayer(): KeyboardLayer {
        val leftKeys = listOf(
            listOf(Key("1"), Key("2"), Key("3"), Key("4"), Key("5")),
            listOf(Key("@"), Key("#"), Key("$"), Key("%"), Key("&")),
            listOf(Key("-"), Key("+"), Key("("), Key(")"), Key("=")),
            listOf(Key(","), Key("."), Key(" ", type = KeyType.SPACE)),
            listOf(
                Key("ABC", type = KeyType.LAYER_SWITCH),
                Key("#+", type = KeyType.LAYER_SWITCH)
            )
        )

        val rightKeys = listOf(
            listOf(Key("6"), Key("7"), Key("8"), Key("9"), Key("0")),
            listOf(Key("*"), Key("\""), Key("'"), Key(":"), Key(";")),
            listOf(Key("/"), Key("<"), Key(">"), Key("["), Key("]")),
            listOf(Key("!"), Key("?"), Key(" ", type = KeyType.SPACE)),
            listOf(
                Key("⌫", type = KeyType.BACKSPACE),
                Key("↵", type = KeyType.ENTER)
            )
        )

        return KeyboardLayer("numbers", leftKeys, rightKeys)
    }

    private fun createSymbolsLayer(): KeyboardLayer {
        val leftKeys = listOf(
            listOf(Key("~"), Key("`"), Key("|"), Key("•"), Key("√")),
            listOf(Key("π"), Key("÷"), Key("×"), Key("¶"), Key("∆")),
            listOf(Key("£"), Key("¢"), Key("€"), Key("¥"), Key("^")),
            listOf(Key(","), Key("."), Key(" ", type = KeyType.SPACE)),
            listOf(
                Key("123", type = KeyType.LAYER_SWITCH),
                Key("ABC", type = KeyType.LAYER_SWITCH)
            )
        )

        val rightKeys = listOf(
            listOf(Key("©"), Key("®"), Key("™"), Key("✓"), Key("§")),
            listOf(Key("{"), Key("}"), Key("\\"), Key("<"), Key(">")),
            listOf(Key("["), Key("]"), Key("°"), Key("•"), Key("...")),
            listOf(Key("_"), Key("-"), Key(" ", type = KeyType.SPACE)),
            listOf(
                Key("⌫", type = KeyType.BACKSPACE),
                Key("↵", type = KeyType.ENTER)
            )
        )

        return KeyboardLayer("symbols", leftKeys, rightKeys)
    }
}
