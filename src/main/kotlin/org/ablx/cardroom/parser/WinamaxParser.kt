package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Hand
import org.ablx.cardroom.commons.data.HandAction
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.*
import org.ablx.cardroom.commons.enumeration.Currency
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


open class WinamaxParser(override val cardroom: Cardroom, override val filePath: String) : Parser, CardroomParser() {
    override val handDateFormat: String = "yyyy/MM/dd HH:mm:ss"

    override var operator: Operator = Operator.WINAMAX
    protected val ANTE_BLIND = "*** ANTE/BLINDS ***"
    protected val PRE_FLOP = "*** PRE-FLOP ***"
    protected val FLOP = "*** FLOP ***"
    protected val TURN = "*** TURN ***"
    protected val RIVER = "*** RIVER ***"
    protected val SHOW_DOWN = "*** SHOW DOWN ***"
    protected val SUMMARY = "*** SUMMARY ***"
    protected val NEW_HAND = "Winamax Poker"
    protected val SEAT = "Seat"
    protected val BOARD = "Board"
    protected val ENCODING = "UTF8"
    protected val TABLE = "Table: "
    protected val NO_RAKE = "No rake"
    protected val RAKE = "Rake "
    protected val TOTAL_POT = "Total pot "
    protected val DEALT_TO = "Dealt to "
    protected val DEALT = "Dealt"
    protected val DENIES = "denies"
    protected val POSTS = "posts"
    protected val SMALL = "small"
    protected val BUY_IN = "buyIn: "
    protected val LEVEL = "level:"
    protected val LEVEL_SPACE = LEVEL + SPACE
    protected val PLUS_SPACE = PLUS + SPACE
    protected val IS_THE_BUTTON = "is the button"
    protected val HANDID_HASHTAG = "HandId: #"
    protected val MINUS_HANDID = " - HandId:"
    protected val MAX = "max"


    override fun fileToMap(): Map<String, String> {

        val map = HashMap<String, String>()
        val parts = readHandFile().split(NEW_HAND)

        parts
                .asSequence()
                .filter { it != "" }
                .forEach { map.put(parseHandId(it), NEW_HAND + it) }

        return map
    }


    override fun getGameTypeFromFilename(fileName: String): GameType {
        if (fileName.contains(LEFT_PARENTHESIS) && fileName.contains(RIGHT_PARENTHESIS)) {
            return GameType.TOURNAMENT
        } else {
            return GameType.CASH
        }
    }


    override fun isHandFile(filePath: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isUselessLine(line: String): Boolean {
        return false
    }


    override fun parse(): MutableMap<String, Hand> {
        val mapHands: MutableMap<String, Hand> = HashMap()
        val mapFilePart: Map<String, String> = fileToMap()

        for (key in mapFilePart.keys) {

            mapHands.put(key, textToHand(mapFilePart[key]!!))
        }
        return mapHands
    }


    override fun parseBigBlind(line: String): Double {
        var startPosition = line.indexOf(LEFT_PARENTHESIS) + 1
        var endPosition = line.indexOf(RIGHT_PARENTHESIS)
        val blinds = line.substring(startPosition, endPosition)

        if (blinds.indexOf(SLASH) != blinds.lastIndexOf(SLASH)) {
            startPosition = blinds.lastIndexOf(SLASH) + 1
            endPosition = blinds.length
        } else {
            startPosition = blinds.indexOf(SLASH) + 1
            endPosition = blinds.length
        }

        return blinds.substring(startPosition, endPosition).toDouble()
    }

    override fun parseButtonSeat(line: String): Int {
        val startPosition = line.lastIndexOf(HASHTAG) + 1
        val endPosition = line.indexOf(IS_THE_BUTTON) - 1
        return line.substring(startPosition, endPosition).toInt()
    }

    override fun parseBuyIn(line: String): Double {
        var startPosition = line.indexOf(BUY_IN) + BUY_IN.length
        var endPosition = line.indexOf(LEVEL)

        var buyIn = line.substring(startPosition, endPosition)

        // Cas 1: 0,90eee+ 0,10e
        // Cas 2: Ticket only
        if (buyIn.contains(PLUS)) {
            startPosition = 0
            if (buyIn.contains(money.symbol)) {
                endPosition = buyIn.indexOf(money.symbol)
            } else {
                endPosition = buyIn.indexOf(PLUS)
            }
            buyIn = buyIn.substring(startPosition, endPosition)
            buyIn = buyIn.replace(VIRGULE, POINT)
            buyIn = buyIn.replace(money.symbol, EMPTY)
            return java.lang.Double.parseDouble(buyIn)
        }
        return 0.0
    }

    override fun parseCurrency(line: String): org.ablx.cardroom.commons.enumeration.Currency {
        //The Winamax currency is only euro
        return Currency.EURO
    }


    override fun parseFee(line: String): Double {
        val tab = line.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var startPosition = tab[1].indexOf(BUY_IN) + BUY_IN.length
        var endPosition = tab[1].indexOf(LEVEL)

        var fee = tab[1].substring(startPosition, endPosition)
        // Cas 1: 0,90eee+ 0,10e
        // Cas 2: Ticket only
        if (fee.contains(PLUS)) {
            startPosition = fee.indexOf(PLUS_SPACE) + PLUS_SPACE.length
            if (fee.contains(money.symbol)) {
                endPosition = fee.lastIndexOf(money.symbol)

            } else {
                endPosition = fee.length

            }
            fee = fee.substring(startPosition, endPosition)
            fee = fee.replace(VIRGULE, POINT)
            return java.lang.Double.parseDouble(fee)
        }
        return 0.0
    }

    override fun parseGameIdCardroom(fileName: String): String {
        val startPosition = fileName.indexOf(LEFT_PARENTHESIS) + 1
        val endPosition = fileName.indexOf(RIGHT_PARENTHESIS, startPosition)
        return fileName.substring(startPosition, endPosition)
    }

    override fun parseHandDate(line: String): Date {
        val startPosition = line.lastIndexOf(DASH) + 2
        val endPosition = line.lastIndexOf(SPACE)

        try {
            return convertHandDate(line.substring(startPosition, endPosition))
        } catch (e: ParseException) {
            return Date()
        }

    }

    override fun parseHandId(line: String): String {
        val startPosition = line.indexOf(HANDID_HASHTAG) + HANDID_HASHTAG.length
        val endPosition = line.indexOf(DASH, line.indexOf(DASH,
                line.indexOf(HANDID_HASHTAG) + HANDID_HASHTAG.length) + 1)
        return line.substring(startPosition, endPosition)
    }

    override fun parseLevel(line: String): Int {
        val startPosition = line.indexOf(LEVEL_SPACE) + LEVEL_SPACE.length
        val endPosition = line.indexOf(MINUS_HANDID)
        return Integer.parseInt(line.substring(startPosition, endPosition))
    }

    override fun parseNewHandLine(line: String, phase: String, nextPhases: Array<String>, hand: Hand): String {
        if (line.startsWith(phase)) {


            hand.players = HashMap<Int, Player>()

            hand.bigBlind = parseBigBlind(line)
            hand.smallBlind = parseSmallBlind(line)
            hand.handDate = parseHandDate(line)
            hand.currency = parseCurrency(line)
            hand.level = parseLevel(line)
            hand.fee = parseFee(line)
            hand.buyIn = parseBuyIn(line)
        }
        return line
    }

    override fun parseNumberOfPlayerByTable(line: String): Int {
        val startPosition = line.lastIndexOf(APOSTROPHE) + 2
        val endPosition = line.lastIndexOf(MAX) - 1
        return line.substring(startPosition, endPosition).toInt()
    }

    override fun parsePlayerAccount(line: String): String {
        val startPosition: Int = DEALT_TO.length
        val endPosition: Int = line.lastIndexOf(OPENNING_SQUARE_BRACKET) - 1
        return line.substring(startPosition, endPosition)
    }

    override fun parsePlayerSeat(line: String): Player {
        val space = line.indexOf(SPACE)
        val colon = line.indexOf(COLON)
        val LEFT_PARENTHESIS = line.indexOf(LEFT_PARENTHESIS)
        val RIGHT_PARENTHESIS = line.indexOf(RIGHT_PARENTHESIS)

        val seat = line.substring(space + 1, colon)
        val playerName = line.substring(colon + 2,
                LEFT_PARENTHESIS - 1)
        var stack = line.substring(LEFT_PARENTHESIS + 1, RIGHT_PARENTHESIS)
        stack = stack.replace(money.symbol, EMPTY)
        val player = Player(null, playerName, cardroom)

        player.seat = Integer.parseInt(seat)
        player.on = true
        player.stack = java.lang.Double.parseDouble(stack)
        return player
    }

    override fun parseRake(line: String): Double {
        //No rake because not cashgame
        return 0.0
    }


    override fun parseSmallBlind(line: String): Double {
        var startPosition = line.indexOf(LEFT_PARENTHESIS) + 1
        var endPosition = line.indexOf(RIGHT_PARENTHESIS)
        val blinds = line.substring(startPosition, endPosition)

        if (blinds.indexOf(SLASH) != blinds.lastIndexOf(SLASH)) {
            startPosition = blinds.indexOf(SLASH) + 1
            endPosition = blinds.lastIndexOf(SLASH)
        } else {
            startPosition = 0
            endPosition = blinds.indexOf(SLASH)
        }

        return blinds.substring(startPosition, endPosition).toDouble()

    }

    override fun parseTableId(line: String): String {
        val startPosition = line.indexOf(HASHTAG) + 1
        val endPosition = line.lastIndexOf(APOSTROPHE)

        return line.substring(startPosition, endPosition)
    }


    override fun parseTotalPot(line: String): Double {
        var currentLine = line
        val startPosition = TOTAL_POT.length
        val endPosition = currentLine.indexOf(PIPE) - 1
        currentLine = currentLine.substring(startPosition, endPosition)
        currentLine = currentLine.replace(money.symbol, EMPTY)
        return java.lang.Double.parseDouble(currentLine)
    }

    override fun readHandFile(): String {
        val encoded: ByteArray = Files.readAllBytes(Paths.get(filePath))
        return String(encoded, Charsets.UTF_8)
    }

    override fun readAction(line: String, players: Map<String, Player>): HandAction {
        val tab = line.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var action = ""
        var playerName = ""
        var between: String
        var amount = "0"
        var playerCards: Array<Card?>? = null

        for (i in tab.indices) {
            if (isAction(tab[i])) {
                playerName = ""
                action = tab[i]
                if (Action.CALLS.action == tab[i] || Action.RAISES.action == tab[i]
                        || Action.COLLECTED.action == tab[i] || Action.BETS.action == tab[i]) {
                    amount = tab[i + 1]
                    amount = amount.replace(money.symbol, EMPTY)
                }

                for (j in 0..i - 1) {
                    if (j == 0) {
                        between = ""
                    } else {
                        between = SPACE
                    }
                    playerName = playerName + between + tab[j]

                }
                if (Action.SHOWS.action == tab[i]) {
                    playerCards = readCards(line)

                }

            }
        }

        return HandAction(players[playerName], Action.valueOfCode(action), java.lang.Double.parseDouble(amount), playerCards)
    }


    override fun textToHand(text: String): Hand {
        var currentLine: String = ""
        var firstIteration = true
        val iter = text.lines().asIterable().iterator()
        var hand = Hand("1")


        while (iter.hasNext()) {

            if (firstIteration) {
                currentLine = iter.next()
                firstIteration = false
            }

            //Check each New Hand Line
            if (currentLine.startsWith(NEW_HAND)) {

                hand = Hand(parseHandId(currentLine))
                parseNewHandLine(currentLine, NEW_HAND, arrayOf(EMPTY), hand)
                currentLine = iter.next()
            }

            if (currentLine.startsWith(TABLE)) {
                hand.numberOfPlayerByTable = parseNumberOfPlayerByTable(currentLine)
                var buttonSeat = parseButtonSeat(currentLine)
                var tableId = parseTableId(currentLine)

                currentLine = iter.next()
            }

            currentLine = parseSeatLine(currentLine, iter, SEAT,
                    arrayOf(ANTE_BLIND), hand)

            currentLine = parseAntesAndBlinds(currentLine, iter, ANTE_BLIND,
                    arrayOf(PRE_FLOP, SUMMARY), hand)

            currentLine = readPreflop(currentLine, iter, hand)

            currentLine = readFlop(currentLine, iter, hand)

            currentLine = readTurn(currentLine, iter, hand)

            currentLine = readRiver(currentLine, iter, hand)

            currentLine = readShowdown(currentLine, iter, hand)

            currentLine = readSummary(currentLine, iter, SUMMARY,
                    arrayOf(NEW_HAND), hand)


        }
        return hand
    }


    override fun parseAntesAndBlinds(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        var nextL = currentLine
        if (nextL.startsWith(phase)) {

            while (iterator.hasNext()) {
                nextL = iterator.next()
                if (nextL.startsWith(DEALT)) {
                    val nameAccountPlayer = parsePlayerAccount(nextL)
                    val playerDealt = hand.players[hand.playersSeatByName[nameAccountPlayer]]

                    if (playerDealt != null) {
                        playerDealt.cards = this.readCards(nextL)
                    }

                    hand.accountPlayer = playerDealt
                } else {
                    if (startsWith(nextL, nextPhases)) {
                        break
                    }
                    val tab = nextL.split(SPACE.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val blind = tab[tab.size - 3]


                    // cas ou le joueur ne paie pas la big blind
                    if (DENIES == blind) {
                        continue
                    }
                    val joueur: String
                    if (POSTS == blind) {
                        joueur = tab[0]
                    } else {
                        joueur = this.getPlayerBlind(tab)
                        if (SMALL == blind) {
                            hand.smallBlindPlayer = hand.players[hand.playersSeatByName[joueur]]
                        } else {
                            hand.bigBlindPlayer = hand.players[hand.playersSeatByName[joueur]]
                        }
                    }
                }
            }
        }
        return nextL
    }

    override fun parseDealer(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseSeatLine(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        hand.playersSeatByName = HashMap<kotlin.String, kotlin.Int>()

        var nextL = currentLine
        if (nextL.startsWith(phase)) {

            var i = 0
            while (iterator.hasNext()) {
                i++


                val playerInGame = parsePlayerSeat(nextL)

                hand.addPlayer(playerInGame)

                //game.getPlayers().put(playerInGame.getLibelle(), playerInGame)


                /**
                 * @TODO mieux gerer le cas ou le button a ete eliminer au tour
                 * *       d'avant.
                 */
                if (hand.buttonSeat == playerInGame.seat) {

                    hand.dealerPlayer = playerInGame
                }

                nextL = iterator.next()
                if (startsWith(nextL, nextPhases)) {
                    break
                }
            }
        }
        return nextL
    }

    override fun parseTableLine(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        hand.numberOfPlayerByTable = parseNumberOfPlayerByTable(currentLine)
        var buttonSeat = parseButtonSeat(currentLine)
        hand.cardroomTableId = parseTableId(currentLine)

        return iterator.next()
    }

    override fun readActionsByPhase(currentLine: String, iterator: Iterator<String>, hand: Hand, phase: String, nextPhases: Array<String>, actions: MutableList<HandAction>?): String {

        var curL = currentLine

        if (curL.startsWith(phase)) {
            // Demarrage de la lecture de la phase
            while (iterator.hasNext()) {
                curL = iterator.next()
                // Check si on tombe sur la prochaine phase
                if (startsWith(curL, nextPhases)) {
                    break
                } else {
                    // Ajout des actions ela phase dans le HanDTO
                    val action = this.readAction(curL,
                            hand.playersByName)

                    var round: Round?

                    when (phase) {
                        PRE_FLOP -> round = Round.PRE_FLOP
                        FLOP -> round = Round.FLOP
                        TURN -> round = Round.TURN
                        RIVER -> round = Round.RIVER
                        SHOW_DOWN -> round = Round.SHOWDOWN
                        else -> round = null
                    }
                    action.round = round
                    actions?.add(action)
                }
            }

        }
        // Retourne le nextLine pour pouvoir continuer l'itteration du scanner
        // comme il faut.
        return curL
    }


    override fun readPreflop(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        hand.actions = ArrayList<HandAction>()
        hand.preflopActions = ArrayList<HandAction>()
        return readActionsByPhase(currentLine, iterator, hand, PRE_FLOP, arrayOf(FLOP, SUMMARY),
                hand.preflopActions)
    }

    override fun readFlop(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        hand.flopActions = ArrayList<HandAction>()
        return readActionsByPhase(currentLine, iterator, hand, FLOP, arrayOf(TURN, SUMMARY), hand.flopActions)
    }

    override fun readTurn(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        hand.turnActions = ArrayList<HandAction>()
        return readActionsByPhase(currentLine, iterator, hand, TURN, arrayOf(RIVER, SUMMARY), hand.turnActions)
    }

    override fun readRiver(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        hand.riverActions = ArrayList<HandAction>()
        return readActionsByPhase(currentLine, iterator, hand, RIVER, arrayOf(SHOW_DOWN, SUMMARY),
                hand.riverActions)
    }

    override fun readShowdown(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        hand.showdownActions = ArrayList<HandAction>()
        return readActionsByPhase(currentLine, iterator, hand, SHOW_DOWN, arrayOf(SUMMARY),
                hand.showdownActions)
    }

    override fun readSummary(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>,
                             hand: Hand): String {
        var curLine = currentLine
        if (curLine.startsWith(phase)) {

            while (iterator.hasNext()) {
                // Total pot 180 | No rake
                if (curLine.startsWith(TOTAL_POT)) {
                    val rake = parseRake(curLine)
                    hand.totalPot = parseTotalPot(curLine)
                    hand.rake = rake
                }
                if (curLine.startsWith(BOARD)) {
                    this.readCards(curLine)
                }
                if (curLine.startsWith(SEAT) && curLine.contains(CLOSING_SQUARE_BRACKET)) {
                    this.readCards(curLine)
                }
                if (startsWith(curLine, nextPhases)) {
                    break
                } else {
                    curLine = iterator.next()
                }
            }

            hand.actions.addAll(hand.preflopActions)
            hand.actions.addAll(hand.flopActions)
            hand.actions.addAll(hand.turnActions)
            hand.actions.addAll(hand.riverActions)
            hand.actions.addAll(hand.showdownActions)
        }
        return curLine
    }


    fun isAction(parsedAction: String): Boolean {
        return Action.FOLDS.action == parsedAction || Action.CALLS.action == parsedAction
                || Action.RAISES.action == parsedAction || Action.CHECKS.action == parsedAction
                || Action.COLLECTED.action == parsedAction || Action.BETS.action == parsedAction
                || Action.SHOWS.action == parsedAction
    }

    override fun getNextUseFulLine(iterator: Iterator<String>): String {
        return iterator.next()
    }
}