package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Hand
import org.ablx.cardroom.commons.data.HandAction
import org.ablx.cardroom.commons.data.Player
import java.util.ArrayList


class WinamaxCashGameParser(override val cardroom: Cardroom, override val filePath: String) : Parser, WinamaxParser(cardroom, filePath) {


    //TODO override testParse and testTextToHand

    override fun parseNewHandLine(line: String, phase: String, nextPhases: Array<String>, hand: Hand): String {
        if (line.startsWith(phase)) {

            val buy = parseBuyIn(line)
            val fee = parseFee(line)

            val handId = parseHandId(line)
            hand.date = parseHandDate(line)
            hand.cardroomHandId = handId
            hand.buyIn = buy + fee
            hand.fee = fee
            // Pas de level en cash game
            hand.level = 0

            val smallBlind = parseSmallBlind(line)
            val bigBlind = parseBigBlind(line)
            hand.actions = ArrayList<HandAction>()
            //TODO put in the right phase

            hand.preflopActions = ArrayList<HandAction>()
            hand.flopActions = ArrayList<HandAction>()
            hand.turnActions = ArrayList<HandAction>()
            hand.riverActions = ArrayList<HandAction>()
            hand.showdownActions = ArrayList<HandAction>()
            hand.players = HashMap<Int, Player>()

            hand.bigBlind = bigBlind
            hand.smallBlind = smallBlind
        }
        return line
    }


    override fun readSummary(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>,
                             hand: Hand): String {
        var nextLine = currentLine
        if (nextLine.startsWith(SUMMARY)) {

            while (iterator.hasNext()) {
                if (nextLine.startsWith("Total pot ")) {
                    val rake = parseRake(nextLine)
                    hand.totalPot = parseTotalPot(nextLine)
                    hand.rake = rake
                }

                if (nextLine.startsWith(BOARD)) {
                    this.readCards(nextLine)
                }

                if (nextLine.startsWith(SEAT) && nextLine.contains(CLOSING_SQUARE_BRACKET)) {
                    this.readCards(nextLine)
                }
                if (startsWith(nextLine, nextPhases)) {
                    break
                } else {
                    nextLine = iterator.next()
                }
            }
            hand.actions.addAll(hand.preflopActions)
            hand.actions.addAll(hand.flopActions)
            hand.actions.addAll(hand.turnActions)
            hand.actions.addAll(hand.riverActions)
            hand.actions.addAll(hand.showdownActions)

        }
        return nextLine
    }

    override fun parseBuyIn(chaine: String): Double {
        return 0.0
    }

    override fun parseFee(chaine: String): Double {
        return 0.0
    }

    override fun parseSmallBlind(chaine: String): Double {
        var startPosition = chaine.indexOf(LEFT_PARENTHESIS) + 1
        var endPosition = chaine.indexOf(RIGHT_PARENTHESIS)
        val blinds = chaine.substring(startPosition, endPosition)
        var smallBlind = ""


        if (blinds.indexOf(SLASH) != blinds.lastIndexOf(SLASH)) {
            startPosition = blinds.indexOf(SLASH) + 1
            endPosition = blinds.lastIndexOf(SLASH)
        } else {
            startPosition = 0
            endPosition = blinds.indexOf(SLASH)
        }
        smallBlind = blinds.substring(startPosition, endPosition)


        smallBlind = removeCharacter(smallBlind, money.symbol)
        return java.lang.Double.parseDouble(smallBlind)

    }

    private fun removeCharacter(chaine: String, caractere: String): String {
        if (chaine.contains(caractere)) {
            return chaine.replace(caractere, EMPTY)
        }
        return chaine
    }

    override fun parseBigBlind(chaine: String): Double {
        var startPosition = chaine.indexOf(LEFT_PARENTHESIS) + 1
        var endPosition = chaine.indexOf(RIGHT_PARENTHESIS)
        val blinds = chaine.substring(startPosition, endPosition)
        var bigBlind = chaine.substring(chaine.indexOf(SLASH) + 1,
                chaine.indexOf(RIGHT_PARENTHESIS))


        if (blinds.indexOf(SLASH) != blinds.lastIndexOf(SLASH)) {
            startPosition = blinds.lastIndexOf(SLASH) + 1
            endPosition = blinds.length
        } else {
            startPosition = blinds.indexOf(SLASH) + 1
            endPosition = blinds.length
        }

        bigBlind = blinds.substring(startPosition, endPosition)
        bigBlind = removeCharacter(bigBlind, money.symbol)



        return java.lang.Double.parseDouble(bigBlind)
    }

    override fun parseTableId(chaine: String): String {
        val startPosition = chaine.indexOf(APOSTROPHE) + 1
        val endPosition = chaine.lastIndexOf(APOSTROPHE)
        return chaine.substring(startPosition, endPosition)
    }


}