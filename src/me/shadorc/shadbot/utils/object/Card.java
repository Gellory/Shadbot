package me.shadorc.shadbot.utils.object;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import me.shadorc.shadbot.utils.Utils;

public class Card {

	private final int num;
	private final String name;
	private final Sign sign;

	public enum Sign {
		HEART(Color.RED, Emoji.HEARTS),
		TILE(Color.RED, Emoji.DIAMONDS),
		CLOVER(Color.BLACK, Emoji.CLUBS),
		PIKE(Color.BLACK, Emoji.SPADES);

		private final Color color;
		private final Emoji emoji;

		Sign(Color color, Emoji emoji) {
			this.color = color;
			this.emoji = emoji;
		}

		public Color getColor() {
			return this.color;
		}

		public Emoji getEmoji() {
			return this.emoji;
		}
	}

	public Card(int num, Sign sign) {
		this.num = num;
		this.sign = sign;
		switch (num) {
			case 13:
				this.name = "K";
				break;
			case 12:
				this.name = "Q";
				break;
			case 11:
				this.name = "J";
				break;
			default:
				this.name = Integer.toString(num);
				break;
		}
	}

	public int getNum() {
		return this.num;
	}

	public String getName() {
		return this.name;
	}

	public Sign getSign() {
		return this.sign;
	}

	public static Card pick() {
		return new Card(ThreadLocalRandom.current().nextInt(1, 14), Utils.randValue(Sign.values()));
	}

	public static List<Card> pick(int count) {
		final List<Card> cards = new ArrayList<>();
		for(int i = 0; i < count; i++) {
			cards.add(Card.pick());
		}
		return cards;
	}
}