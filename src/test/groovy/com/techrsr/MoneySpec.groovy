package com.techrsr

import spock.lang.Specification
import spock.lang.Unroll

class MoneySpec extends Specification {

    @Unroll
    def "test convert to words #input"() {
        expect:
        Money.convertToWords(input as BigDecimal) == output

        where:
        input          | output
        0              | "Zero"
        2123           | "Two Thousand And One Hundred And Twenty Three"
        10050          | "Ten Thousand And Fifty"
        10000300       | "One Crore And Three Hundred"
        10000000000000 | "Ten Lakhs Crores"
        2.1            | "Two"
        21.5           | "Twenty Two"
    }
}
