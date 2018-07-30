package me.shadorc.shadbot.music;

import java.util.Objects;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import discord4j.voice.AudioProvider;

public class MusicProvider implements AudioProvider {

	private final AudioPlayer audioPlayer;
	private final GuildMusic guildMusic;
	private AudioFrame lastFrame;

	public MusicProvider(AudioPlayer audioPlayer, GuildMusic guildMusic) {
		this.audioPlayer = audioPlayer;
		this.guildMusic = guildMusic;
	}

	@Override
	public boolean isDone() {
		return !guildMusic.isInVoiceChannel();
	}

	@Override
	public byte[] provide() {
		if(lastFrame == null) {
			lastFrame = audioPlayer.provide();
		}

		byte[] data = lastFrame == null ? null : lastFrame.getData();
		lastFrame = null;

		// FIXME: Is this ok to return an empty array ?
		return Objects.requireNonNullElse(data, new byte[0]);
	}

}
