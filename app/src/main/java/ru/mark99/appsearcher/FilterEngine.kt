package ru.mark99.appsearcher

import java.util.regex.Pattern
import kotlin.text.Regex.Companion.escapeReplacement

class FilterEngine {
    private val symbolTableEn = mapOf(
            "a" to "(a|а)",
            "b" to "(b|в)",
            "c" to "(c|с|s)",
            "d" to "(d|д)",
            "e" to "(e|е|ё)",
            "f" to "(f|ф)",
            "g" to "(g|г|ж)",
            "h" to "(h|х|ч)",
            "i" to "(i|и|ай)",
            "j" to "(j|дж|ж)",
            "k" to "(k|к)",
            "l" to "(l|л)",
            "m" to "(m|м)",
            "n" to "(n|н)",
            "o" to "(o|о)",
            "p" to "(p|п)",
            "q" to "(q|к|ку)",
            "r" to "(r|р)",
            "s" to "(s|с|c)",
            "t" to "(t|т)",
            "u" to "(u|у|ю)",
            "v" to "(v|в)",
            "w" to "(w|в|у)",
            "x" to "(x|кс|икс|х)",
            "y" to "(y|у)",
            "z" to "(z|з)",
    )

    private val symbolTableRu = mapOf(
            "а" to "(а|a)",
            "б" to "(б|b)",
            "в" to "(в|v|w)",
            "г" to "(г|g)",
            "д" to "(д|d)",
            "е" to "(е|e)",
            "ё" to "(ё|e)",
            "ж" to "(ж|zh)",
            "з" to "(з|z)",
            "и" to "(и|i)",
            "й" to "(й|y|i|и)",
            "к" to "(к|k)",
            "л" to "(л|l)",
            "м" to "(м|m)",
            "н" to "(н|n)",
            "о" to "(о|o)",
            "п" to "(п|p)",
            "р" to "(р|r)",
            "с" to "(с|s|c)",
            "т" to "(т|t)",
            "у" to "(у|u|y)",
            "ф" to "(ф|f)",
            "х" to "(х|h|kh)",
            "ц" to "(ц|ts)",
            "ч" to "(ч|ch|h)",
            "ш" to "(ш|sh)",
            "щ" to "(щ|sch)",
            "ъ" to "(ъ)",
            "ы" to "(ы|y)",
            "ь" to "(ь)",
            "э" to "(э|e)",
            "ю" to "(ю|u|y|yu)",
            "я" to "(я|ja|ya)"
    )

    private val fullSymbolTable: Map<String, String> = symbolTableEn + symbolTableRu

    fun makeRegexp(uinput: String): Pattern {
        val result = StringBuilder()

        for (symbol in uinput.lowercase()) {
            val conv = fullSymbolTable.getOrDefault(
                    symbol.toString(),
                    escapeReplacement(symbol.toString())
            )
            result.append(conv);
        }

        return Pattern.compile(result.toString())
    }
}