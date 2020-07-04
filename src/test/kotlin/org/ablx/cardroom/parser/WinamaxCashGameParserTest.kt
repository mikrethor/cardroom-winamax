package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.enumeration.Currency
import org.ablx.cardroom.commons.enumeration.Domain
import org.ablx.cardroom.commons.enumeration.Operator
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals


class WinamaxCashGameParserTest : WinamaxParserTest() {


    override fun createParser(): Parser {
        val cardroom = Cardroom(1, Operator.WINAMAX, Domain.FR, "")

        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("HandCashGameTestFile.txt")!!.file)

        val parser: Parser = WinamaxCashGameParser(cardroom, file.absolutePath)
        parser.setCurrency(Currency.EURO)
        return parser
    }


    @Test
    override fun testTextToHand() {
        val parser: Parser = createParser()
        val handText = "Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: Ticket only level: 0 - HandId: #236883548206792705-1-1377708812 - Holdem no limit (10/20) - 2013/08/28 16:53:32 UTC\n" +
                "Table: 'Super Freeroll Stade 2(55153749)#0' 6-max (real money) Seat #6 is the button\n" +
                "Seat 1: Mikrethor (1500)\n" +
                "Seat 2: Boubinho25 (1500)\n" +
                "Seat 3: jibecmoa84 (1500)\n" +
                "Seat 4: SMOUZE (1500)\n" +
                "Seat 5: phillippo (1500)\n" +
                "Seat 6: pachou83 (1500)\n" +
                "*** ANTE/BLINDS ***\n" +
                "Mikrethor posts small blind 10\n" +
                "Boubinho25 posts big blind 20\n" +
                "Dealt to Mikrethor [Td 5d]\n" +
                "*** PRE-FLOP *** \n" +
                "jibecmoa84 calls 20\n" +
                "SMOUZE folds\n" +
                "phillippo raises 40 to 60\n" +
                "pachou83 folds\n" +
                "Mikrethor folds\n" +
                "Boubinho25 calls 40\n" +
                "jibecmoa84 calls 40\n" +
                "*** FLOP *** [9s 4d Tc]\n" +
                "Boubinho25 checks\n" +
                "jibecmoa84 checks\n" +
                "phillippo bets 190\n" +
                "Boubinho25 calls 190\n" +
                "jibecmoa84 folds\n" +
                "*** TURN *** [9s 4d Tc][2d]\n" +
                "Boubinho25 checks\n" +
                "phillippo bets 570\n" +
                "Boubinho25 folds\n" +
                "phillippo collected 1140 from pot\n" +
                "*** SUMMARY ***\n" +
                "Total pot 1140 | No rake\n" +
                "Board: [9s 4d Tc 2d]\n" +
                "Seat 5: phillippo won 1140"
        val hand = parser.textToHand(handText)

        assertEquals("236883548206792705-1", hand.cardroomHandId)
        assertEquals("Mikrethor", hand.accountPlayer!!.name)
    }

    @Test
    override fun testRealBuyIn() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        val result: Double = parser.parseBuyIn("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.0, result)

        val fee: Double = parser.parseFee("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.0, fee)
    }

    @Test
    override fun testTableId() {
        val parser: Parser = createParser()
        assertEquals("Tokyo 02", parser.parseTableId("Table: 'Tokyo 02' 9-max (real money) Seat #3 is the button"))
    }

    @Test
    override fun testParse() {
        val parser: Parser = createParser()
        val hands = parser.parse()
        assertEquals(12, hands.values.size)
    }
}
