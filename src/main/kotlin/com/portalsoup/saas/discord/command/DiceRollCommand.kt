package com.portalsoup.saas.discord.command

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Mono
import kotlin.random.Random

object DiceRollCommand: Command {
    override fun execute(event: MessageCreateEvent, truncatedMessage: String): Mono<Void> {
        val regex = "\\D*(?<die>\\d*)[dD](?<faces>\\d*)\\D*".toRegex()
        val match = regex.matchEntire(truncatedMessage)
        val values = match?.groups

        val die = values?.get("die")?.value?.takeIf { it.isNotEmpty() }?.toInt() ?: 1
        val faces = values?.get("faces")?.value?.takeIf { it.isNotEmpty() }?.toInt() ?: 20

        return event.message.channel.flatMap { ch ->
            EmbedCreateSpec.builder()
                .title(buildTitle(event, die, faces))
                .also {
                    for (i in 1..die) {
                        val roll = (Random.nextInt(faces) + 1).toString()
                        println("Adding a roll! $roll")
                        it.addField("", roll, true)
                    }
                }
                .build()
                .let { ch.createMessage(it) }
        }.then()
    }

    private fun buildTitle(event: MessageCreateEvent, die: Int, faces: Int): String {
        val authorPrefixOrCapitalR = event.message.author.orElse(null)?.username?.let { "$it r" } ?: "R"
        return "${authorPrefixOrCapitalR}olled $die D${faces}${die.takeIf { it > 1 }?.let { "s" } ?: ""}"
    }
}