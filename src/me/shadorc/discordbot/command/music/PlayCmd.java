package me.shadorc.discordbot.command.music;

import java.time.temporal.ChronoUnit;

import me.shadorc.discordbot.Shadbot;
import me.shadorc.discordbot.command.AbstractCommand;
import me.shadorc.discordbot.command.CommandCategory;
import me.shadorc.discordbot.command.Context;
import me.shadorc.discordbot.command.Role;
import me.shadorc.discordbot.data.Config;
import me.shadorc.discordbot.events.music.AudioLoadResultListener;
import me.shadorc.discordbot.music.GuildMusicManager;
import me.shadorc.discordbot.utils.BotUtils;
import me.shadorc.discordbot.utils.LogUtils;
import me.shadorc.discordbot.utils.NetUtils;
import me.shadorc.discordbot.utils.Utils;
import me.shadorc.discordbot.utils.command.Emoji;
import me.shadorc.discordbot.utils.command.MissingArgumentException;
import me.shadorc.discordbot.utils.command.RateLimiter;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

public class PlayCmd extends AbstractCommand {

	private final RateLimiter rateLimiter;

	public PlayCmd() {
		super(CommandCategory.MUSIC, Role.USER, "play", "add", "queue");
		this.rateLimiter = new RateLimiter(RateLimiter.COMMON_COOLDOWN, ChronoUnit.SECONDS);
	}

	@Override
	public void execute(Context context) throws MissingArgumentException {
		if(rateLimiter.isSpamming(context)) {
			return;
		}

		if(!context.hasArg()) {
			throw new MissingArgumentException();
		}

		IVoiceChannel botVoiceChannel = Shadbot.getClient().getOurUser().getVoiceStateForGuild(context.getGuild()).getChannel();
		IVoiceChannel userVoiceChannel = context.getAuthor().getVoiceStateForGuild(context.getGuild()).getChannel();

		if(botVoiceChannel != null && (userVoiceChannel == null || !userVoiceChannel.equals(botVoiceChannel))) {
			BotUtils.sendMessage(Emoji.GREY_EXCLAMATION + " I'm currently playing music in voice channel " + botVoiceChannel.mention()
					+ ", join me before using this command.", context.getChannel());
			return;
		}

		if(userVoiceChannel == null) {
			BotUtils.sendMessage(Emoji.GREY_EXCLAMATION + " Join a voice channel before using this command.", context.getChannel());
			return;
		}

		if(botVoiceChannel == null && !BotUtils.hasPermission(userVoiceChannel, Permissions.VOICE_CONNECT, Permissions.VOICE_SPEAK)) {
			BotUtils.sendMessage(Emoji.ACCESS_DENIED + " I cannot connect/speak in this voice channel due to the lack of permission."
					+ "\nPlease, check my permissions and channel-specific ones to verify that **Voice connect** and **Voice speak** "
					+ "are checked.", context.getChannel());
			LogUtils.info("{Guild ID: " + context.getGuild().getLongID() + "} Shadbot wasn't allowed to connect/speak in a voice channel.");
			return;
		}

		String identifier;
		if(context.getArg().startsWith("soundcloud ")) {
			identifier = AudioLoadResultListener.SC_SEARCH + context.getArg().replace("soundcloud ", "");
		} else if(NetUtils.isValidURL(context.getArg())) {
			identifier = context.getArg();
		} else {
			identifier = AudioLoadResultListener.YT_SEARCH + context.getArg();
		}

		GuildMusicManager musicManager = GuildMusicManager.getGuildMusicManager(context.getGuild());

		if(musicManager == null) {
			musicManager = GuildMusicManager.createGuildMusicManager(context.getGuild());
		}

		if(musicManager.getScheduler().getPlaylist().size() >= Config.MAX_PLAYLIST_SIZE) {
			BotUtils.sendMessage(Emoji.GREY_EXCLAMATION + " You've reached the maximum number of tracks in the playlist (Max: "
					+ Config.MAX_PLAYLIST_SIZE + ").", context.getChannel());
			return;
		}

		musicManager.setChannel(context.getChannel());

		AudioLoadResultListener resultListener = new AudioLoadResultListener(musicManager, context.getAuthor(), userVoiceChannel, identifier);
		GuildMusicManager.PLAYER_MANAGER.loadItemOrdered(musicManager, identifier, resultListener);
	}

	@Override
	public void showHelp(Context context) {
		EmbedBuilder builder = Utils.getDefaultEmbed(this)
				.appendDescription("**Play the music(s) from the url, search terms or playlist.**")
				.appendField("Usage", "`" + context.getPrefix() + "play [soundcloud] <url>`", false)
				.appendField("Argument", "**soundcloud** - [OPTIONAL] search on SoundCloud instead of YouTube", false);
		BotUtils.sendMessage(builder.build(), context.getChannel());
	}
}
