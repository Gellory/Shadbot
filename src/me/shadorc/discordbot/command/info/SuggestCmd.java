package me.shadorc.discordbot.command.info;

import java.time.temporal.ChronoUnit;

import me.shadorc.discordbot.Config;
import me.shadorc.discordbot.Emoji;
import me.shadorc.discordbot.MissingArgumentException;
import me.shadorc.discordbot.RateLimiter;
import me.shadorc.discordbot.Shadbot;
import me.shadorc.discordbot.command.AbstractCommand;
import me.shadorc.discordbot.command.Context;
import me.shadorc.discordbot.utils.BotUtils;
import sx.blah.discord.util.EmbedBuilder;

public class SuggestCmd extends AbstractCommand {

	private final RateLimiter rateLimiter;

	public SuggestCmd() {
		super(Role.USER, "suggest");
		this.rateLimiter = new RateLimiter(5, ChronoUnit.SECONDS);
	}

	@Override
	public void execute(Context context) throws MissingArgumentException {
		if(!context.hasArg()) {
			throw new MissingArgumentException();
		}

		if(rateLimiter.isLimited(context.getGuild(), context.getAuthor())) {
			if(!rateLimiter.isWarned(context.getGuild(), context.getAuthor())) {
				rateLimiter.warn("Take it easy, don't spam :)", context);
			}
			return;
		}

		BotUtils.sendMessage(context.getAuthorName() + " from Guild \"" + context.getGuild().getName() + "\" suggested : " + context.getArg(), Shadbot.getClient().getChannelByID(Config.SUGGEST_CHANNEL_ID));
		BotUtils.sendMessage(Emoji.CHECK_MARK + " Suggestion sent, thank you !", context.getChannel());
	}

	@Override
	public void showHelp(Context context) {
		EmbedBuilder builder = new EmbedBuilder()
				.withAuthorName("Help for " + this.getNames()[0] + " command")
				.withAuthorIcon(Shadbot.getClient().getOurUser().getAvatarURL())
				.withColor(Config.BOT_COLOR)
				.appendDescription("**Send a message to my developer, this can be a suggestion, a bug report, anything.**")
				.appendField("Usage", context.getPrefix() + "suggest <message>", false);
		BotUtils.sendEmbed(builder.build(), context.getChannel());
	}

}