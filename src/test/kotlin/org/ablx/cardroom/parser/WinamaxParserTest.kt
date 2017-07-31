package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.Action
import org.ablx.cardroom.commons.enumeration.Currency
import org.ablx.cardroom.commons.enumeration.Domain
import org.ablx.cardroom.commons.enumeration.Operator
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class WinamaxParserTest {


    private fun createParser(): Parser {
        val cardroom = Cardroom(1, Operator.WINAMAX, Domain.FR, "")

        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("HandTournamentTestFile.txt")!!.file)

        val parser: Parser = WinamaxParser(cardroom, file.absolutePath)
        parser.setCurrency(Currency.EURO)
        return parser
    }

    @Test
    fun testBuyInTicketOnly() {
        val parser: Parser = createParser()

        val buyIn: Double = parser.parseBuyIn("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: Ticket only level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.0, buyIn)

        val fee: Double = parser.parseFee("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: Ticket only level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.0, fee)
    }

    @Test
    fun testRealBuyIn() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        val result: Double = parser.parseBuyIn("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.45, result)

        val fee: Double = parser.parseFee("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.05, fee)
    }


    @Test
    fun testHandId() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: String = parser.parseHandId("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals("236883548206792705-2", result)

        result = parser.parseHandId("Winamax Poker - CashGame - HandId: #9004445-57475-1473728657 - Holdem no limit (0.01€/0.02€) - 2016/09/13 01:04:17 UTC")
        assertEquals("9004445-57475", result)


    }


    @Test
    fun testLevel() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Int = parser.parseLevel("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0, result)

        result = parser.parseLevel("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC")
        assertEquals(6, result)
    }

    @Test
    fun testSmallBlind() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Double = parser.parseSmallBlind("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC")
        assertEquals(50.0, result)

        result = parser.parseSmallBlind("Winamax Poker - Tournament \"Qualif . Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 9 - HandId: #866707662845247494-4-1500772425 - Holdem no limit (25/100/200) - 2017/07/23 01:13:45 UTC")
        assertEquals(100.0, result)
    }

    @Test
    fun testBigBlind() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Double = parser.parseBigBlind("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC")
        assertEquals(100.0, result)

        result = parser.parseBigBlind("Winamax Poker - Tournament \"Qualif . Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 9 - HandId: #866707662845247494-4-1500772425 - Holdem no limit (25/100/200) - 2017/07/23 01:13:45 UTC")
        assertEquals(200.0, result)

    }

    @Test
    fun testCurrency() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Currency = parser.parseCurrency("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC")
        assertEquals(Currency.EURO, result)

        result = parser.parseCurrency("")
        assertEquals(Currency.EURO, result)

    }

    @Test
    fun testGameIdCardroom() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        val result: String = parser.parseGameIdCardroom("20170722_Qualif. Ticket 5€(201796103)_real_holdem_no-limit.txt")
        assertEquals("201796103", result)
    }

    @Test
    fun testNumberOfPalyersByTable() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Int = parser.parseNumberOfPlayerByTable("Table: 'Qualif. Ticket 5€(201796103)#005' 9-max (real money) Seat #9 is the button")
        assertEquals(9, result)

        result = parser.parseNumberOfPlayerByTable(" Table: 'Nice 05' 5-max (real money) Seat #2 is the button")
        assertEquals(5, result)

    }


    @Test
    fun testPlayerSeat() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var player: Player = parser.parsePlayerSeat("Seat 1: Fletan67 (0.89€)")

        assertEquals("Fletan67", player.name)
        assertEquals(true, player.on)
        assertEquals(1, player.seat)
        assertEquals(0.89, player.stack)

        player = parser.parsePlayerSeat("Seat 2: Mikrethor (2€)")

        assertEquals("Mikrethor", player.name)
        assertEquals(true, player.on)
        assertEquals(2, player.seat)
        assertEquals(2.00, player.stack)

        player = parser.parsePlayerSeat("Seat 3: fab3523 (1.99€)")

        assertEquals("fab3523", player.name)
        assertEquals(true, player.on)
        assertEquals(3, player.seat)
        assertEquals(1.99, player.stack)

        player = parser.parsePlayerSeat("Seat 4: frappedemule (2€)")

        assertEquals("frappedemule", player.name)
        assertEquals(true, player.on)
        assertEquals(4, player.seat)
        assertEquals(2.00, player.stack)

        player = parser.parsePlayerSeat("Seat 5: indoaffaire3 (1.05€)")

        assertEquals("indoaffaire3", player.name)
        assertEquals(true, player.on)
        assertEquals(5, player.seat)
        assertEquals(1.05, player.stack)

        player = parser.parsePlayerSeat("Seat 6: Warou (2€)")

        assertEquals("Warou", player.name)
        assertEquals(true, player.on)
        assertEquals(6, player.seat)
        assertEquals(2.00, player.stack)

        player = parser.parsePlayerSeat("Seat 7: SCUMPI (2€)")

        assertEquals("SCUMPI", player.name)
        assertEquals(true, player.on)
        assertEquals(7, player.seat)
        assertEquals(2.00, player.stack)

        player = parser.parsePlayerSeat("Seat 8: mamiejoelle (1.99€)")

        assertEquals("mamiejoelle", player.name)
        assertEquals(true, player.on)
        assertEquals(8, player.seat)
        assertEquals(1.99, player.stack)

        player = parser.parsePlayerSeat("Seat 9: Bystyc (2.01€)")

        assertEquals("Bystyc", player.name)
        assertEquals(true, player.on)
        assertEquals(9, player.seat)
        assertEquals(2.01, player.stack)
    }


    @Test
    fun testButtonSeat() {
        val parser: Parser = createParser()

        assertEquals(3, parser.parseButtonSeat("Table: 'Tokyo 02' 9-max (real money) Seat #3 is the button"))

        assertEquals(5, parser.parseButtonSeat("Table: 'Super Freeroll Stade 2(55153749)#0' 6-max (real money) Seat #5 is the button"))
    }


    @Test
    fun testHandDate() {
        val parser: Parser = createParser()
        val calendar = GregorianCalendar(2017, 6, 23, 0, 54, 3)
        assertEquals(calendar.time, parser.parseHandDate("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC"))
    }

    @Test
    fun testPlayerAccount() {
        val parser: Parser = createParser()
        //TODO parse a player with card
        assertEquals("Mikrethor", parser.parsePlayerAccount("Dealt to Mikrethor [Kc 3s]"))
    }

    @Test
    fun testTableId() {
        val parser: Parser = createParser()
        assertEquals("0", parser.parseTableId("Table: 'Super Freeroll Stade 2(55153749)#0' 6-max (real money) Seat #5 is the button"))
    }

    @Test
    fun testParse() {
        val parser: Parser = createParser()
        parser.parse()
    }

    @Test
    fun testIsAction() {
        val cardroom = Cardroom(1, Operator.WINAMAX, Domain.FR, "")
        val parser= WinamaxParser(cardroom,"")
        assertTrue(parser.isAction("calls"))
        assertTrue(parser.isAction("folds"))
        assertTrue(parser.isAction("raises"))
        assertTrue(parser.isAction("checks"))
        assertTrue(parser.isAction("collected"))
        assertTrue(parser.isAction("bets"))
        assertTrue(parser.isAction("shows"))

        assertFalse(parser.isAction("false"))



    }

}