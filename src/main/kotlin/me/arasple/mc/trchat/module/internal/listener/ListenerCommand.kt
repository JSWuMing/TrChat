package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.config.Functions
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.sendLang

/**
 * @author Arasple, wlys
 * @date 2020/1/16 21:41
 */
@PlatformSide([Platform.BUKKIT])
object ListenerCommand {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        val player = e.player
        var command = e.message.removePrefix("/").trimIndent()

        if (!Functions.CONF.getBoolean("General.Command-Controller.Enable", true) && command.isEmpty()) {
            return
        }

        val mCmd = Bukkit.getCommandAliases().entries.firstOrNull { (_, value) ->
            value.any { it.equals(command.split(" ")[0], ignoreCase = true) }
        }
        command = if (mCmd != null) mCmd.key + command.removePrefix(mCmd.key) else command

        val condition = Functions.commandCondition.get().entries.firstOrNull { it.key.matches(command) }?.value
        if (condition != null && !condition.eval(player)) {
            e.isCancelled =  true
            player.sendLang("Command-Controller-Deny")
            return
        }

        val baffle = Functions.commandDelay.get().entries.firstOrNull { it.key.matches(command.trimIndent()) }?.value
        if (baffle != null && !baffle.hasNext(player.name) && !player.hasPermission(Functions.CONF.getString("General.Command-Controller.Cooldown-Bypass-Permission")!!)) {
            e.isCancelled =  true
            player.sendLang("Command-Controller-Cooldown")
        }
    }
}