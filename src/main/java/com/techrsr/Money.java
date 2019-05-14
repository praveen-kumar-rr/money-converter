package com.techrsr;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

import java.math.BigDecimal;
import java.math.RoundingMode;

final public class Money {

    // REPETITION and THRESHOLD go hand in hand
    // Until 99.. crore is one repetition

    private static final Long THRESHOLD = 1000000000000000000L;
    private static final Integer REPETITION = 3;

    private static final Map<Integer, String> UNITS = HashMap.of(
            0, "",
            1, " One",
            2, " Two",
            3, " Three",
            4, " Four",
            5, " Five",
            6, " Six",
            7, " Seven",
            8, " Eight",
            9, " Nine").merge(HashMap.of(
            10, " Ten",
            11, " Eleven",
            12, " Twelve",
            13, " Thirteen",
            14, " Fourteen",
            15, " Fifteen",
            16, " Sixteen",
            17, " Seventeen",
            18, " Eighteen",
            19, " Nineteen"));

    private static final Map<Integer, String> TENS = HashMap.of(
            0, "",
            1, " Ten",
            2, " Twenty",
            3, " Thirty",
            4, " Forty",
            5, " Fifty",
            6, " Sixty",
            7, " Seventy",
            8, " Eighty",
            9, " Ninety");

    // Since from second the repetition starts from hundred
    private static final List<String> PLACE = Stream.concat(Stream.of(""),
            Stream.of(
                    " Hundred",
                    " Thousand",
                    " Lakh",
                    " Crore").cycle(REPETITION)).toList();

    private static final List<String> PLURALIZABLES = List.of("Lakh", "Crore");


    public static String convertToWords(BigDecimal value) {
        final Long roundedValue = value.setScale(0, RoundingMode.HALF_UP).longValue();

        if (roundedValue >= THRESHOLD) {
            throw new RuntimeException("Too huge value. Currently not supported");
        }

        if (roundedValue.equals(0L)) {
            return "Zero";
        }

        final String valueAsString = roundedValue.toString();

        // Main Logic
        return indexes(valueAsString.length())
                .map(t -> valueAsString.substring(t._1, t._2))
                .filter(s -> !s.isEmpty())
                .map(Money::words)
                .zip(PLACE) // zipping with place creating (word, place) to be appended
                .filter(t -> t._2.trim().equals("Crore") || !t._1.isEmpty()) // 0 is ignored until crores is touched
                .reverse()
                .map(Money::getPlural)
                .map(t -> (t._1.isEmpty() ? "" : " And") + join(t))// Decides And is needed or not
                .mkString()
                .replaceFirst("And ", "")
                .trim();
    }

    // Collecting the list of indexes to substring the number
    // Eg: 23123 -> 23 1 23 -> Twenty Three Thousand And One Hundred And Twenty Three
    private static List<Tuple2<Integer, Integer>> indexes(Integer totalLength) {
        // This stream describes the jumps needed to split like the eg above
        Stream<Integer> jumps = Stream.concat(Stream.of(2), Stream.of(1, 2, 2, 2).cycle(REPETITION))
                .scanLeft(0, Integer::sum);

        return jumps.drop(1)
                .zip(jumps)
                .map(t -> tup(totalLength - t._1, totalLength - t._2))
                .map(t -> tup(zeroOnMinus(t._1), zeroOnMinus(t._2)))
                .toList();
    }

    private static <T, U> Tuple2<T, U> tup(T t, U u) {
        return new Tuple2<>(t, u);
    }

    private static String join(Tuple2<String, String> tup) {
        return tup._1 + tup._2;
    }

    private static Integer zeroOnMinus(Integer value) {
        return value < 0 ? 0 : value;
    }

    private static Tuple2<String, String> getPlural(Tuple2<String, String> tuple) {
        return tup(tuple._1, isPluralizable(tuple) ? tuple._2 + "s" : tuple._2);
    }

    private static Boolean isPluralizable(Tuple2<String, String> tuple) {
        return !tuple._1.trim().equals("One") && PLURALIZABLES.contains(tuple._2.trim());
    }

    private static String words(String number) {
        Integer numberInteger = Integer.valueOf(number);
        return numberInteger < 20 ? UNITS.getOrElse(numberInteger, "") : splitConvert(number);
    }

    private static String splitConvert(String number) {
        return List.of(number.split(""))
                .reduce((l, r) -> TENS.getOrElse(Integer.parseInt(l), "") +
                        UNITS.getOrElse(Integer.parseInt(r), ""));
    }
}
