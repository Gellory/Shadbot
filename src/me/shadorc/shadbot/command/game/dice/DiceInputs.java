package me.shadorc.shadbot.command.game.dice;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import me.shadorc.shadbot.listener.interceptor.Inputs;
import reactor.core.publisher.Mono;

public class DiceInputs extends Inputs {

	private final DiceManager manager;

	public DiceInputs(DiscordClient client, DiceManager manager) {
		super(client, manager.getDuration());
		this.manager = manager;
	}

	@Override
	public Mono<Boolean> isValidEvent(MessageCreateEvent event) {
		if(event.getMessage().getContent().isEmpty() || event.getMember().isEmpty()) {
			return Mono.just(false);
		}

		if(!event.getMessage().getChannelId().equals(this.manager.getContext().getChannelId())) {
			return Mono.just(false);
		}

		final Member member = event.getMember().get();
		return this.manager.isCancelMessage(event.getMessage())
				.map(isCancelCmd -> isCancelCmd || this.manager.getPlayers().containsKey(member.getId()));
	}

	@Override
	public boolean takeEventWile(MessageCreateEvent ignored) {
		return this.manager.isScheduled();
	}

	@Override
	public Mono<Void> processEvent(MessageCreateEvent event) {
		return Mono.empty();
	}

}
